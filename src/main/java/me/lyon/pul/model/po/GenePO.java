package me.lyon.pul.model.po;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Immutable
@Table(name = "gene")
@TypeDef(
        name = "string-array",
        typeClass = ListArrayType.class
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenePO implements Serializable {
    @Id
    private String id;
    @Column(name = "gene_name")
    private String geneName;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pul_id", referencedColumnName = "id", nullable = false)
    private PulPO pul;
    @Type(type = "string-array")
    @Column(name = "domain", columnDefinition = "varchar(64)[]")
    private List<String> domain;
    @Column(name = "classification")
    private String classification;
    @Column(name = "locus_start")
    private Integer locusStart;
    @Column(name = "locus_end")
    private Integer locusEnd;
    @Column(name = "strand")
    private Integer strand;
    @Column(name = "score")
    private Double score;
}
