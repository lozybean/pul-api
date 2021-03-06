package me.lyon.pul.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class PulInfo implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("pul_id")
    private String pulId;
    @JsonProperty("pul_type")
    private String pulType;
    @JsonProperty("contig_name")
    private String contigName;
    @JsonProperty("pul_start")
    private Integer pulStart;
    @JsonProperty("pul_end")
    private Integer pulEnd;

    @JsonProperty("assembly_accession")
    private String assemblyAccession;
    @JsonProperty("assembly_level")
    private String assemblyLevel;

    @JsonProperty("taxonomy_id")
    private Integer taxonomyId;
    @JsonProperty("kingdom")
    private String spKingdom;
    @JsonProperty("phylum")
    private String spPhylum;
    @JsonProperty("class")
    private String spClass;
    @JsonProperty("order")
    private String spOrder;
    @JsonProperty("family")
    private String spFamily;
    @JsonProperty("species")
    private String spSpecies;

    @JsonIgnore
    @JsonProperty("content")
    private List<PulContent> content;
}
