package me.lyon.pul.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.config.PredictConfig;
import me.lyon.pul.constant.ContainerStatus;
import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.exception.BizException;
import me.lyon.pul.exception.NotFoundException;
import me.lyon.pul.exception.RuntimeIOException;
import me.lyon.pul.model.entity.ContainerState;
import me.lyon.pul.model.entity.JobInfo;
import me.lyon.pul.model.entity.PulContent;
import me.lyon.pul.model.entity.PulInfo;
import me.lyon.pul.model.mapper.JobInfoMapper;
import me.lyon.pul.model.po.ContainerStatePO;
import me.lyon.pul.model.po.JobInfoPO;
import me.lyon.pul.model.po.SpeciesPO;
import me.lyon.pul.repository.JobInfoRepository;
import me.lyon.pul.repository.SpeciesRepository;
import me.lyon.pul.service.PredictService;
import me.lyon.pul.util.TokenUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public Optional<JobInfo> findFirstInitJob() {
        return repository.findFirstByStatusOrderByIdAsc(JobStatus.INIT)
                .map(JobInfoMapper.INSTANCE::entity);
    }

    @Override
    public Optional<JobInfo> findFirstRetryJob() {
        return repository.findFirstByStatusOrderByIdAsc(JobStatus.RETRYING)
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
    public synchronized JobInfo createPredictJob(MultipartFile file) {
        String token = TokenUtils.generateNewToken();
        Path outputPath = createOutputDir(token);
        Path inputFile = createInputFile(token, file);

        try (CreateContainerCmd cmd = dockerClient.createContainerCmd(config.getDockerImage())
                .withHostConfig(HostConfig.newHostConfig()
                        .withCpuCount(1L)
                        .withMemory(100_000_000L)
                        .withBinds(
                                Bind.parse(String.format("%s:/home/tao/Documents:ro", config.getReferencePath())),
                                Bind.parse(String.format("%s:/home/tao/Documents/PUL_prediction_online_analysis/Output_file:rw", outputPath)),
                                Bind.parse(String.format("%s:/home/tao/Documents/PUL_prediction_online_analysis/Genomes/GCF_000013665.1_ASM1366v1_genomic.gbff:rw", inputFile))
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
        jobInfo.setRetryTimes(jobInfo.getRetryTimes() + 1);
        JobInfoPO po = JobInfoMapper.INSTANCE.po(jobInfo);
        repository.save(po);
    }

    @CachePut(cacheNames = "predictJob", key = "#token")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized JobInfo startPredictJob(String token) {
        JobInfo jobInfo = findByToken(token);
        if (!JobStatus.INIT.equals(jobInfo.getStatus())) {
            log.warn("job not in INIT status: {}", token);
            return jobInfo;
        }
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
    public synchronized JobInfo updatePredictJobStatus(String token) {
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

    private synchronized ContainerState inspectContainer(String id) {
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
    public synchronized void cleanPredictJob(String token) {
        JobInfo jobInfo = findByToken(token);
        String containerId = jobInfo.getContainerState().getId();
        try (RemoveContainerCmd cmd = dockerClient.removeContainerCmd(containerId)) {
            cmd.exec();
        } catch (com.github.dockerjava.api.exception.NotFoundException e) {
            log.warn("container already deleted, {}", containerId);
        }
        try {
            File outputDir = Path.of(config.getOutputPath(), token).toFile();
            File inputFile = Path.of(config.getInputPath(), token + ".gbff").toFile();
            FileUtils.deleteDirectory(outputDir);
            FileUtils.deleteQuietly(inputFile);
            JobInfoPO po = JobInfoMapper.INSTANCE.po(jobInfo);
            repository.delete(po);
        } catch (IOException e) {
            log.warn("clean job result failed!", e);
        }
    }


    /*============================= read predict result ==================================*/
    @Resource
    SpeciesRepository speciesRepository;

    private final Pattern gcfPattern = Pattern.compile(".(\\d+)\\.?.*");

    private Integer parseGcfNumber(String gcfString) {
        Matcher matcher = gcfPattern.matcher(gcfString);
        if (matcher.matches()) {
            return Integer.valueOf(matcher.group(1));
        } else {
            log.error("parse gcf: {} failed!", gcfString);
            throw new BizException("parse gcf failed!");
        }
    }

    private List<String> parseDomains(String domainString) {
        if (StringUtils.isEmpty(domainString) || "unknown".equals(domainString)) {
            return List.of();
        }
        return Arrays.stream(domainString.substring(1, domainString.length() - 1)
                        .split(","))
                .map(s -> StringUtils.strip(s, " "))
                .map(s -> s.substring(1, s.length() - 1))
                .collect(Collectors.toList());
    }

    private PulContent parsePulContent(String[] line) {
        String pulId = line[0];
        String contigName = line[2];
        Integer serialNumber = Integer.valueOf(line[3]);
        Integer start = Integer.valueOf(line[4]);
        Integer end = Integer.valueOf(line[5]);
        Integer direction = Objects.equals(line[7], "+") ? 1 : -1;
        List<String> domains = parseDomains(line[8]);
        String classification = line[10];
        return PulContent.builder()
                .geneId(String.format("%s_%d", pulId, serialNumber))
                .geneName(String.format("%s_%d_%d.%s", contigName, start, end, serialNumber))
                .geneType(classification)
                .geneStart(start)
                .geneEnd(end)
                .strand(direction)
                .domains(domains)
                .build();
    }

    private PulInfo parsePulInfo(String[] line, List<PulContent> contents) {
        String pulId = line[0];
        String pulType = line[1];
        Integer gcfNumber = parseGcfNumber(line[2]);
        SpeciesPO species = speciesRepository.findById(gcfNumber)
                .orElseThrow(() -> new BizException(String.format("no such species of gcf: %d", gcfNumber)));
        String contigName = line[3];
        List<PulContent> pulContents = contents.stream()
                .filter(c -> c.getGeneId().startsWith(String.format("%s_", pulId)))
                .sorted()
                .collect(Collectors.toList());
        if (pulContents.isEmpty()) {
            log.error("pul contents is empty!");
            throw new BizException("pul contents should not be empty!");
        }
        Integer start = pulContents.get(0).getGeneStart();
        Integer end = pulContents.get(pulContents.size() - 1).getGeneEnd();
        return PulInfo.builder()
                .id(pulId)
                .pulId(String.format("%s_%d_%d", contigName, start, end))
                .pulType(pulType)
                .contigName(contigName)
                .pulStart(start)
                .pulEnd(end)
                .assemblyAccession(species.getGcfNumber())
                .assemblyLevel(species.getAssembleLevel())
                .taxonomyId(species.getTaxid())
                .spKingdom(species.getSpKingdom())
                .spPhylum(species.getSpPhylum())
                .spClass(species.getSpClass())
                .spOrder(species.getSpOrder())
                .spFamily(species.getSpFamily())
                .spSpecies(species.getSpSpecies())
                .content(contents.stream()
                        .filter(c -> c.getGeneId().startsWith(String.format("%s_", pulId)))
                        .collect(Collectors.toList()))
                .build();
    }

    private List<String[]> readResultFile(Path filePath) {
        try {
            CSVReader pulContentCsvReader = new CSVReaderBuilder(new FileReader(filePath.toString()))
                    .withSkipLines(1)
                    .withCSVParser(new CSVParserBuilder().withSeparator('\t').build())
                    .build();
            return pulContentCsvReader.readAll();
        } catch (IOException e) {
            log.error("read result file: {} failed! ", filePath, e);
            throw new RuntimeIOException("read result file failed!");
        }
    }

    @Cacheable(cacheNames = "predictResult", key = "#token")
    @Override
    public List<PulInfo> readPredictResult(String token) {
        JobInfo jobInfo = findByToken(token);
        if (!JobStatus.SUCCESS.equals(jobInfo.getStatus())) {
            log.warn("job not success: {}", token);
            throw new BizException("job still not success");
        }

        Path pulInfoResult = Path.of(config.getOutputPath(), "PUL_information");
        Path pulContentResult = Path.of(config.getOutputPath(), "PUL_protein_information");
        if (!pulInfoResult.toFile().exists()) {
            log.error(String.format("result file: %s not found!", pulInfoResult));
            throw new BizException("result file not found!");
        }
        if (!pulContentResult.toFile().exists()) {
            log.error(String.format("result file: %s not found!", pulContentResult));
            throw new BizException("result file not found!");
        }

        List<String[]> pulContentsLines = readResultFile(pulContentResult);
        List<String[]> pulInfoLines = readResultFile(pulInfoResult);

        final List<PulContent> pulContents = pulContentsLines
                .stream()
                .map(this::parsePulContent)
                .collect(Collectors.toList());
        return pulInfoLines
                .stream()
                .map(line -> parsePulInfo(line, pulContents))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
