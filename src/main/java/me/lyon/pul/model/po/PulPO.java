package me.lyon.pul.model.po;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Immutable
@Table(name = "pul")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PulPO implements Serializable {
    @Id
    private String id;
    @Column(name = "pul_id")
    private String pulId;
    @Column(name = "type")
    private String type;
    @Column(name = "contig_name")
    private String contigName;
    @Column(name = "locus_start")
    private Integer locusStart;
    @Column(name = "locus_end")
    private Integer locusEnd;

    @JoinColumn(name = "gcf_id", referencedColumnName = "id")
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private SpeciesPO species;

    @OneToMany(mappedBy = "pul",
            cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<GenePO> contents;
}
