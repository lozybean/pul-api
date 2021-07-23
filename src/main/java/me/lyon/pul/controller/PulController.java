package me.lyon.pul.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.lyon.pul.model.vo.*;
import me.lyon.pul.service.GggeneService;
import me.lyon.pul.service.PulService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


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
    public WebResponse<PageData<PulInfo>> listAll(
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Gggenes {
        private String base64;
    }

    @GetMapping("{id}/gggenes")
    @ResponseBody
    public WebResponse<Gggenes> plotGggenes(
            @PathVariable String id
    ) {
        Optional<PulInfo> pulInfo = pulService.queryById(id);
        if (pulInfo.isPresent()) {
            try {
                String base64 = gggeneService.plotWithBase64(pulInfo.get());
                return WebResponse.ok(new Gggenes(base64));
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
