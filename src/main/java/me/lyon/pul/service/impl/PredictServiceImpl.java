package me.lyon.pul.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.config.PredictConfig;
import me.lyon.pul.constant.ContainerStatus;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.exception.BizException;
import me.lyon.pul.exception.NotFoundException;
import me.lyon.pul.exception.RuntimeIOException;
import me.lyon.pul.model.entity.ContainerState;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.mapper.JobInfoMapper;
import me.lyon.pul.model.po.ContainerStatePO;
import me.lyon.pul.model.po.JobInfoPO;
import me.lyon.pul.repository.JobInfoRepository;
import me.lyon.pul.service.PredictService;
import me.lyon.pul.util.TokenUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@CacheConfig(cacheNames = {"predictJob", "predictResult"})
public class PredictServiceImpl implements PredictService {
    @Resource(name = "predictConfig")
    PredictConfig config;
    @Resource(name = "dockerClient")
    DockerClient dockerClient;
    @Resource
    JobInfoRepository repository;

    private Path createOutputDir(String token) {
        Path outputDir = Path.of(config.getOutputPath(), token);
        boolean mkdirResult = outputDir.toFile().mkdirs();
        if (!mkdirResult) {
            log.error("create dir failed! {}", outputDir);
            throw new RuntimeIOException("create dir failed!");
        }
        return outputDir;
    }

    private Path createInputFile(String token, MultipartFile file) {
        Path inputFile = Path.of(config.getInputPath(), token + ".gbff");
        try (FileOutputStream os = new FileOutputStream(inputFile.toFile())) {
            IOUtils.write(file.getBytes(), os);
        } catch (IOException e) {
            log.error("create input file failed! {}", inputFile);
            throw new RuntimeIOException("create input file failed!");
        }
        return inputFile;
    }

    @Override
    public List<JobInfo> listJobs() {
        return repository.findAll()
                .stream()
                .map(JobInfoMapper.INSTANCE::entity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<JobInfo> findFirstRunnableJob() {
        return repository.findFirstByStatusInOrderByIdAsc(Set.of(JobStatus.INIT, JobStatus.RETRYING))
                .map(JobInfoMapper.INSTANCE::entity);
    }

    @Cacheable(cacheNames = "predictJob", key = "#token")
    @Override
    public JobInfo findByToken(String token) {
        return repository.findByToken(token)
                .map(JobInfoMapper.INSTANCE::entity)
                .orElseThrow(() -> new NotFoundException("can not find related job of token: {}" + token));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JobInfo createPredictJob(MultipartFile file) {
        String token = TokenUtils.generateNewToken();
        Path outputPath = createOutputDir(token);
        Path inputFile = createInputFile(token, file);

        try (CreateContainerCmd cmd = dockerClient.createContainerCmd(config.getDockerImage())
                .withHostConfig(HostConfig.newHostConfig()
                        .withCpuCount(config.getDockerCpu())
                        .withMemory(config.getDockerMemory())
                        .withBinds(
                                Bind.parse(String.format("%s:/home/tao/Documents", config.getReferencePath())),
                                Bind.parse(String.format("%s:/home/tao/Documents/PUL_prediction_online_analysis/Output_file:rw", outputPath)),
                                Bind.parse(String.format("%s:/home/tao/Documents/PUL_prediction_online_analysis/Genomes/protein.fas:rw", inputFile))
                        ))) {

            CreateContainerResponse response = cmd.exec();

            String containerId = response.getId();

            ContainerState state = inspectContainer(containerId);
            ContainerStatePO statePO = JobInfoMapper.INSTANCE.po(state);

            JobInfoPO jobInfoPO = JobInfoPO.builder()
                    .token(token)
                    .containerState(statePO)
                    .status(JobStatus.INIT)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            repository.save(jobInfoPO);
            return JobInfoMapper.INSTANCE.entity(jobInfoPO);
        }
    }

    private void increaseRetryTime(JobInfo jobInfo) {
        if (Objects.isNull(jobInfo.getRetryTimes())) {
            jobInfo.setRetryTimes(0);
        } else {
            jobInfo.setRetryTimes(jobInfo.getRetryTimes() + 1);
        }
        JobInfoPO po = JobInfoMapper.INSTANCE.po(jobInfo);
        repository.save(po);
    }

    @CachePut(cacheNames = "predictJob", key = "#token")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized JobInfo startPredictJob(String token) {
        JobInfo jobInfo = findByToken(token);
        String containerId = jobInfo.getContainerState().getId();
        ContainerState containerState = inspectContainer(containerId);
        if (ContainerStatus.RUNNING != containerState.getStatus()) {
            try (StartContainerCmd cmd = dockerClient.startContainerCmd(containerId)) {
                cmd.exec();
                increaseRetryTime(jobInfo);
            }
        }
        return updatePredictJobStatus(token);
    }

    @CachePut(cacheNames = "predictJob", key = "#token")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized JobInfo waitPredictJobFinish(String token) {
        JobInfo jobInfo = findByToken(token);
        if (!JobStatus.RUNNING.equals(jobInfo.getStatus())) {
            log.warn("job not in RUNNING status: {}", token);
            return jobInfo;
        }
        String containerId = jobInfo.getContainerState().getId();
        try (WaitContainerCmd cmd = dockerClient.waitContainerCmd(containerId)) {
            WaitContainerResultCallback callback = cmd.start();
            callback.awaitStatusCode();
        }
        return updatePredictJobStatus(token);
    }

    @CachePut(cacheNames = "predictJob", key = "#token")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public JobInfo updatePredictJobStatus(String token) {
        JobInfo jobInfo = findByToken(token);
        String containerId = jobInfo.getContainerState().getId();
        ContainerState containerState = inspectContainer(containerId);
        ContainerStatePO containerStatePO = JobInfoMapper.INSTANCE.po(containerState);
        JobInfoPO po = JobInfoMapper.INSTANCE.po(jobInfo);
        boolean maxRetried = po.getRetryTimes() >= config.getMaxRetryTimes();
        po.setStatus(JobStatus.fromContainerState(containerStatePO, maxRetried));
        po.setContainerState(containerStatePO);
        po.setUpdateTime(new Date());
        repository.save(po);
        return JobInfoMapper.INSTANCE.entity(po);
    }

    private ContainerState inspectContainer(String id) {
        try (InspectContainerCmd cmd = dockerClient.inspectContainerCmd(id)) {
            InspectContainerResponse response = cmd.exec();
            return JobInfoMapper.INSTANCE.entity(id, response.getState());
        } catch (com.github.dockerjava.api.exception.NotFoundException e) {
            log.error("can not find container: {} , maybe has been removed", id);
            throw new NotFoundException("no such container: " + id);
        }
    }

    @CacheEvict(cacheNames = "predictJob", key = "#token")
    @Override
    public void cleanPredictJob(String token) {
        JobInfo jobInfo = findByToken(token);
        String containerId = jobInfo.getContainerState().getId();
        try (RemoveContainerCmd cmd = dockerClient.removeContainerCmd(containerId)) {
            cmd.exec();
        } catch (com.github.dockerjava.api.exception.NotFoundException e) {
            log.warn("container already deleted, {}", containerId);
        }
        try {
            File outputDir = Path.of(config.getOutputPath(), token).toFile();
            File inputFile = Path.of(config.getInputPath(), token + ".fasta").toFile();
            FileUtils.deleteDirectory(outputDir);
            FileUtils.deleteQuietly(inputFile);
            JobInfoPO po = JobInfoMapper.INSTANCE.po(jobInfo);
            repository.delete(po);
        } catch (IOException e) {
            log.warn("clean job result failed!", e);
        }
    }


    /*============================= read predict result ==================================*/

    @Cacheable(cacheNames = "predictResult", key = "#token")
    @Override
    public String readPredictResult(String token) throws IOException {
        JobInfo jobInfo = findByToken(token);
        if (!JobStatus.SUCCESS.equals(jobInfo.getStatus())) {
            log.warn("job not success: {}", token);
            throw new BizException("job still not success");
        }
        Path outputFilePath = Path.of(config.getOutputPath(), token, "output_file");

        return Files.readString(outputFilePath);
    }
}
