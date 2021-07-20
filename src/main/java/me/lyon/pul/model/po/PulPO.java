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
@Table(name = "pul")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PulPO {
    @Id
    private String id;
    @Column(name = "pul_id")
    private String pulId;
    @Column(name = "type")
    private String type;
    @Column(name = "gcf_id")
    private Integer gcfId;
    @Column(name = "gcf_number")
    private String gcfNumber;
    @Column(name = "taxid")
    private Integer taxid;
    @Column(name = "contig_name")
    private String contigName;
    @Column(name = "locus_start")
    private Integer locusStart;
    @Column(name = "locus_end")
    private Integer locusEnd;
}
