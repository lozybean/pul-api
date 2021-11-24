package me.lyon.pul.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PulListVO implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("pul_type")
    private String pulType;

    @JsonProperty("assembly_accession")
    private String assemblyAccession;

    @JsonProperty("phylum")
    private String spPhylum;
    @JsonProperty("species")
    private String spSpecies;
}
