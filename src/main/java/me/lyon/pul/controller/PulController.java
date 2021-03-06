package me.lyon.pul.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.model.entity.*;
import me.lyon.pul.model.vo.PulListVO;
import me.lyon.pul.service.GggeneService;
import me.lyon.pul.service.PulService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@RestController
@RequestMapping(value = "api")
public class PulController {
    @Resource
    PulService pulService;
    @Resource
    GggeneService gggeneService;

    @Data
    static class PulQuery {
        private String option;
        @JsonProperty("page_no")
        private Integer pageNo;
        @JsonProperty("sort_col")
        private String sortCol;
        @JsonProperty("sort_order")
        private String sortOrder;
        @JsonProperty("page_len")
        private Integer pageLen;
        @JsonProperty("val_pul_type")
        private String valPulType;
        @JsonProperty("val_taxonomy_id")
        private Integer valTaxonomyId;
        @JsonProperty("val_assembly_accession")
        private String valAssemblyAccession;
        @JsonProperty("val_species")
        private String valSpecies;
        @JsonProperty("val_phylum")
        private String valPhylum;
        @JsonProperty("val_domain_name")
        private String valDomainName;
    }

    @PostMapping("query")
    @ResponseBody
    public WebResponse<PageData<PulListVO>> queryWithPageable(
            @RequestBody PulQuery query
    ) {
        Pageable pageable;
        if (Objects.nonNull(query.getSortCol()) && !query.getSortCol().isBlank()) {
            Sort sort;
            if ("ascending".equals(query.getSortOrder())) {
                sort = Sort.by(query.getSortCol()).ascending();
            } else {
                sort = Sort.by(query.getSortCol()).descending();
            }
            pageable = PageRequest.of(query.getPageNo() - 1, query.getPageLen(), sort);
        } else {
            pageable = PageRequest.of(query.getPageNo() - 1, query.getPageLen());
        }

        switch (query.getOption()) {
            case "search_by_pul_type":
                return WebResponse.ok(pulService.queryPulByType(query.getValPulType(), pageable));
            case "search_by_linage":
                return WebResponse.ok(pulService.queryPulByLinage(
                        query.getValTaxonomyId(),
                        query.getValAssemblyAccession(),
                        query.getValSpecies(),
                        query.getValPhylum(),
                        pageable));
            case "search_by_domain_name":
                return WebResponse.ok(pulService.queryPulByDomainName(
                        query.getValDomainName(), pageable));
            default:
                return WebResponse.warn("???????????????????????????" + query.getOption());
        }
    }

    @PostMapping("query/all")
    @ResponseBody
    public WebResponse<List<PulListVO>> queryAll(
            @RequestBody PulQuery query
    ) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean searchWithType = !query.getValPulType().isBlank();
        List<PulListVO> pulInfosByType = pulService.queryPulByType(query.getValPulType());
        Set<String> pulIdsByType = pulInfosByType.stream()
                .parallel()
                .map(PulListVO::getId)
                .collect(Collectors.toSet());
        stopWatch.stop();
        log.info("query/all: searchWithType cost: {}ms", stopWatch.getLastTaskTimeMillis());

        stopWatch.start();
        boolean searchWithLinage = Objects.nonNull(query.getValTaxonomyId()) ||
                !query.getValAssemblyAccession().isBlank() ||
                !query.getValSpecies().isBlank() ||
                !query.getValPhylum().isBlank();
        List<PulListVO> pulInfosByLinage = pulService.queryPulByLinage(
                query.getValTaxonomyId(),
                query.getValAssemblyAccession(),
                query.getValSpecies(),
                query.getValPhylum());
        Set<String> pulIdsByLinage = pulInfosByLinage.stream()
                .parallel()
                .map(PulListVO::getId)
                .collect(Collectors.toSet());
        stopWatch.stop();
        log.info("query/all: searchWithLinage cost: {}ms", stopWatch.getLastTaskTimeMillis());


        stopWatch.start();
        boolean searchWithDomain = !query.getValDomainName().isBlank();
        List<PulListVO> pulInfosByDomain = pulService.queryPulByDomainName(query.getValDomainName());
        Set<String> pulIdsByDomain = pulInfosByDomain.stream()
                .parallel()
                .map(PulListVO::getId)
                .collect(Collectors.toSet());
        stopWatch.stop();
        log.info("query/all: searchWithDomain cost: {}ms", stopWatch.getLastTaskTimeMillis());

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // A = a | b | c
        // ??? a is empty
        // ??? A = b | c
        // ??? A & B & C = (b|c) & b & c = b & c
        // ????????? a ?????????????????????
        stopWatch.start();
        if (!searchWithType) {
            pulIdsByType.addAll(pulIdsByLinage);
            pulIdsByType.addAll(pulIdsByDomain);
        }
        if (!searchWithLinage) {
            pulIdsByLinage.addAll(pulIdsByType);
            pulIdsByLinage.addAll(pulIdsByDomain);
        }
        if (!searchWithDomain) {
            pulIdsByDomain.addAll(pulIdsByType);
            pulIdsByDomain.addAll(pulIdsByLinage);
        }

        Set<String> retainIds = new HashSet<>(pulIdsByType);
        retainIds.retainAll(pulIdsByLinage);
        retainIds.retainAll(pulIdsByDomain);
        stopWatch.stop();
        log.info("query/all: retail id set cost: {}ms", stopWatch.getLastTaskTimeMillis());

        stopWatch.start();
        List<PulListVO> pulInfos = Stream.of(pulInfosByType, pulInfosByLinage, pulInfosByDomain)
                .flatMap(Collection::stream)
                .parallel()
                .filter(pulInfo -> retainIds.contains(pulInfo.getId()))
                .distinct()
                .sorted(Comparator.comparing(PulListVO::getId))
                .collect(Collectors.toList());
        stopWatch.stop();
        log.info("query/all: construct list cost: {}ms", stopWatch.getLastTaskTimeMillis());

        return WebResponse.ok(pulInfos);
    }

    @GetMapping("{id}")
    @ResponseBody
    public WebResponse<PulInfo> get(
            @PathVariable String id
    ) {
        Optional<PulInfo> pulInfo = pulService.queryById(id);
        if (pulInfo.isPresent()) {
            return WebResponse.ok(pulInfo.get());
        } else {
            return WebResponse.warn("??????????????????PUL?????????");
        }
    }


    @GetMapping("{id}/gggenes")
    @ResponseBody
    public WebResponse<Gggenes> plotGggenes(
            @PathVariable String id
    ) {
        Optional<PulInfo> pulInfo = pulService.queryById(id);
        if (pulInfo.isPresent()) {
            try {
                Gggenes gggenes = gggeneService.plotWithBase64(pulInfo.get());
                return WebResponse.ok(gggenes);
            } catch (Exception e) {
                log.warn("Ggenes???????????????{}", pulInfo.get().getPulId(), e);
                return WebResponse.warn("Gggenes???????????????????????????");
            }
        } else {
            return WebResponse.warn("??????????????????PUL?????????");
        }
    }

    @GetMapping("browse")
    @ResponseBody
    public WebResponse<BrowseData> getBrowseData() {
        List<NameCount> aggregateByType = pulService.aggregateByType();
        List<NameCount> aggregateByPhylum = pulService.aggregateByPhylum();
        BrowseData browseData = BrowseData.builder()
                .polysaccharide(aggregateByType)
                .phylum(aggregateByPhylum)
                .build();
        return WebResponse.ok(browseData);
    }

}
