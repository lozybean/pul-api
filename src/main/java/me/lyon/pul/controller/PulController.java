package me.lyon.pul.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.model.entity.*;
import me.lyon.pul.service.GggeneService;
import me.lyon.pul.service.PulService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
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
    public WebResponse<PageData<PulInfo>> queryWithPageable(
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
                return WebResponse.warn("不支持该检索方式：" + query.getOption());
        }
    }

    @PostMapping("query/all")
    @ResponseBody
    public WebResponse<List<PulInfo>> queryAll(
            @RequestBody PulQuery query
    ) {
        boolean searchWithType = !query.getValPulType().isBlank();
        List<PulInfo> pulInfosByType = pulService.queryPulByType(query.getValPulType());
        Set<String> pulIdsByType = pulInfosByType.stream()
                .parallel()
                .map(PulInfo::getId)
                .collect(Collectors.toSet());

        boolean searchWithLinage = Objects.nonNull(query.getValTaxonomyId()) &&
                !query.getValAssemblyAccession().isBlank() &&
                !query.getValSpecies().isBlank() &&
                !query.getValPhylum().isBlank();
        List<PulInfo> pulInfosByLinage = pulService.queryPulByLinage(
                query.getValTaxonomyId(),
                query.getValAssemblyAccession(),
                query.getValSpecies(),
                query.getValPhylum());
        Set<String> pulIdsByLinage = pulInfosByLinage.stream()
                .parallel()
                .map(PulInfo::getId)
                .collect(Collectors.toSet());

        boolean searchWithDomain = !query.getValDomainName().isBlank();
        List<PulInfo> pulInfosByDomain = pulService.queryPulByDomainName(query.getValDomainName());
        Set<String> pulIdsByDomain = pulInfosByDomain.stream()
                .parallel()
                .map(PulInfo::getId)
                .collect(Collectors.toSet());

        // 当未使用某个维度检索时，该维度不参与交集运算，为了统一逻辑，将该集合设置为并集
        // A = a | b | c
        // ∵ a is empty
        // ∴ A = b | c
        // ∴ A & B & C = (b|c) & b & c = b & c
        // 相当于 a 不参与交集运算
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

        List<PulInfo> pulInfos = Stream.of(pulInfosByType, pulInfosByLinage, pulInfosByDomain)
                .flatMap(Collection::stream)
                .filter(pulInfo -> retainIds.contains(pulInfo.getId()))
                .distinct()
                .sorted(Comparator.comparing(PulInfo::getId))
                .collect(Collectors.toList());

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
            return WebResponse.warn("未找到对应的PUL信息！");
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
                log.warn("Ggenes绘制失败！{}", pulInfo.get().getPulId(), e);
                return WebResponse.warn("Gggenes绘制失败，请检查！");
            }
        } else {
            return WebResponse.warn("未找到对应的PUL信息！");
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
