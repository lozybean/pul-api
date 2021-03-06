package me.lyon.pul.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PulContent implements Serializable, Comparable<PulContent> {
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
    @JsonProperty("strand")
    private Integer strand;
    @JsonProperty("domains")
    private List<String> domains;

    @Override
    public int compareTo(PulContent pulContent) {
        if (this.geneStart > pulContent.getGeneStart()) {
            return 1;
        } else if (this.geneStart < pulContent.getGeneStart()) {
            return -1;
        } else if (this.geneEnd > pulContent.getGeneEnd()) {
            return 1;
        } else if (this.geneEnd < pulContent.getGeneEnd()) {
            return -1;
        }
        return 0;
    }
}
