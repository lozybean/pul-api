package me.lyon.pul.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PulContent implements Serializable {
    @JsonProperty("gene_id")
    private String geneId;
    @JsonProperty("gene_name")
    private String geneName;
    @JsonProperty("gene_type")
    private String geneType;
    @JsonProperty("gene_start")
    private Integer geneStart;
    @JsonProperty("gene_end")
    private Integer geneEnd;
    @JsonProperty("domains")
    private List<String> domains;
}
