package me.lyon.pul.model.po;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "gene")
@TypeDef(
        name = "string-array",
        typeClass = ListArrayType.class
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenePO {
    @Id
    private String id;
    @Column(name = "gene_name")
    private String geneName;
    @Column(name = "pul_id")
    private String pulId;
    @Type(type = "string-array")
    @Column(name = "domain", columnDefinition = "varchar(64)[]")
    private List<String> domain;
    @Column(name = "classification")
    private String classification;
    @Column(name = "locus_start")
    private Integer locusStart;
    @Column(name = "locus_end")
    private Integer locusEnd;
    @Column(name = "score")
    private Double score;
}
