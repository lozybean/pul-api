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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
@CacheConfig(cacheNames = {"predictResult"})
public class PredictServiceImpl implements PredictService {
    @Resource(name = "predictConfig")
    PredictConfig config;
    @Resource(name = "dockerClient")
    DockerClient dockerClient;
    @Resource
    JobInfoRepository repository;
    @Resource
    SpeciesRepository speciesRepository;

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


    /*============================= read predict result ==================================*/

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
            log.warn("can not parse any domain: {}", domainString);
            return List.of();
        }
        log.info("parse domains: {}", domainString);
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
        return PulInfo.builder()
                .id(pulId)
                .pulType(pulType)
                .contigName(contigName)
                .pulStart(pulContents.get(0).getGeneStart())
                .pulEnd(pulContents.get(pulContents.size() - 1).getGeneEnd())
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
                .collect(Collectors.toList());
    }
}
