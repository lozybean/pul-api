package me.lyon.pul.model.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "species")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesPO {
    @Id
    private Integer taxid;
    @Column(name = "kingdom")
    private String spKingdom;
    @Column(name = "class")
    private String spClass;
    @Column(name = "order")
    private String spOrder;
    @Column(name = "family")
    private String spFamily;
    @Column(name = "genus")
    private String spGenus;
    @Column(name = "species")
    private String spSpecies;
    @Column(name = "assemble_level")
    private String assembleLevel;
    @Column(name = "phyla_information")
    private String phylaInformation;
}
