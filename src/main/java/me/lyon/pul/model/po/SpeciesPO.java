package me.lyon.pul.model.po;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Immutable
@Table(name = "species")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SpeciesPO speciesPO = (SpeciesPO) o;

        return Objects.equals(id, speciesPO.id);
    }

    @Override
    public int hashCode() {
        return 2137681347;
    }
}
