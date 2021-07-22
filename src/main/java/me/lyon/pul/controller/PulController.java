package me.lyon.pul.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import me.lyon.pul.model.vo.PageData;
import me.lyon.pul.model.vo.PulInfo;
import me.lyon.pul.model.vo.WebResponse;
import me.lyon.pul.service.PulService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(value = "api")
public class PulController {
    @Resource
    PulService pulService;

    static Map<String, String> sortFieldMap = Map.of(
            "pul_id", "pulId",
            "pul_type", "type",
            "assembly_accession", "species.gcfNumber",
            "species", "species.spSpecies",
            "phylum", "species.spPhylum"
    );

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
            @RequestBody
                    PulQuery query
    ) {
        Pageable pageable;
        if (!query.getSortCol().isEmpty()) {
            Sort sort;
            if ("ascending".equals(query.getSortOrder())) {
                sort = Sort.by(sortFieldMap.get(query.getSortCol())).ascending();
            } else {
                sort = Sort.by(sortFieldMap.get(query.getSortCol())).descending();
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
}
