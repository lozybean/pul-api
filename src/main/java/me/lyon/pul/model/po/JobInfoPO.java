package me.lyon.pul.model.po;

import lombok.*;
import me.lyon.pul.constant.JobStatus;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "job_info")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class JobInfoPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "token")
    private String token;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "container_id", referencedColumnName = "id")
    private ContainerStatePO containerState;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status;
    @Column(name = "create_time", updatable = false)
    private Date createTime;
    @Column(name = "update_time")
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        JobInfoPO jobInfoPO = (JobInfoPO) o;

        return Objects.equals(id, jobInfoPO.id);
    }

    @Override
    public int hashCode() {
        return 1816459055;
    }
}
