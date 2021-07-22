package me.lyon.pul.model.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Immutable
@Table(name = "species")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesPO implements Serializable {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "gcf_number")
    private String gcfNumber;
    @Column(name = "taxid")
    private Integer taxid;
    @Column(name = "kingdom")
    private String spKingdom;
    @Column(name = "phylum")
    private String spPhylum;
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
