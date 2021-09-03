package me.lyon.pul.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.config.PredictConfig;
import me.lyon.pul.constant.JobStatus;
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
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
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
    public Optional<JobInfo> findFirstInitJob() {
        return repository.findFirstByStatusOrderByIdAsc(JobStatus.INIT)
                .map(JobInfoMapper.INSTANCE::entity);
    }

    @Override
    public JobInfo findByToken(String token) {
        return repository.findByToken(token)
                .map(JobInfoMapper.INSTANCE::entity)
                .orElseThrow(() -> new NotFoundException("can not find related job of token: {}" + token));
    }

    @Override
    public JobInfo findByContainerId(String containerId) {
        return repository.findByContainerState(ContainerStatePO.ofId(containerId))
                .map(JobInfoMapper.INSTANCE::entity)
                .orElseThrow(() -> new NotFoundException("can not find related job of container: " + containerId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createPulPredictContainer(MultipartFile file) {
        String token = TokenUtils.generateNewToken();
        Path outputPath = createOutputDir(token);
        Path inputFile = createInputFile(token, file);

        try (CreateContainerCmd cmd = dockerClient.createContainerCmd(config.getDockerImage())
                .withHostConfig(HostConfig.newHostConfig()
                        .withCpuCount(1L)
                        .withMemory(100_000_000L)
                        .withBinds(
                                Bind.parse(String.format("%s:/home/tao/Documents", config.getReferencePath())),
                                Bind.parse(String.format("%s:/home/tao/Documents/PUL_prediction_online_analysis/Output_file", outputPath)),
                                Bind.parse(String.format("%s:/home/tao/Documents/PUL_prediction_online_analysis/Genomes/GCF_000013665.1_ASM1366v1_genomic.gbff", inputFile))
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
            return token;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void startContainer(String id) {
        try (StartContainerCmd cmd = dockerClient.startContainerCmd(id)) {
            cmd.exec();
        }
        inspectContainer(id, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void waitContainer(String id) {
        try (WaitContainerCmd cmd = dockerClient.waitContainerCmd(id)) {
            WaitContainerResultCallback callback = cmd.start();
            callback.awaitStatusCode();
        }
        inspectContainer(id, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ContainerState inspectContainer(String id, boolean update) {
        try (InspectContainerCmd cmd = dockerClient.inspectContainerCmd(id)) {
            InspectContainerResponse response = cmd.exec();

            if (update) {
                ContainerStatePO statePO = JobInfoMapper.INSTANCE.po(id, response.getState());
                JobInfoPO po = repository.findByContainerState(statePO)
                        .orElseThrow(() -> new NotFoundException("can not find related job of container: " + id));
                po.setStatus(JobStatus.fromContainerState(statePO));
                po.setContainerState(statePO);
                log.info("update job status: {}", po.getStatus());
                log.info("update container status: {}", statePO.getStatus());
                repository.save(po);
            }

            return JobInfoMapper.INSTANCE.entity(id, response.getState());
        } catch (com.github.dockerjava.api.exception.NotFoundException e) {
            log.error("can not find container: {} , maybe has been removed", id);
            return null;
        }
    }

    @Override
    public void removeContainer(String id) {
        try (RemoveContainerCmd cmd = dockerClient.removeContainerCmd(id)) {
            cmd.exec();
        }
    }
}
