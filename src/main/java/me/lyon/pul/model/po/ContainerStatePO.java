package me.lyon.pul.model.po;

import lombok.*;
import me.lyon.pul.constant.ContainerStatus;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Immutable
@Table(name = "container_state")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class ContainerStatePO {
    @Id
    @Column(name = "id")
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContainerStatus status;
    @Column(name = "running")
    private Boolean running;
    @Column(name = "paused")
    private Boolean paused;
    @Column(name = "restarting")
    private Boolean restarting;
    @Column(name = "oomkilled")
    private Boolean oomKilled;
    @Column(name = "dead")
    private Boolean dead;
    @Column(name = "pid")
    private Long pid;
    @Column(name = "exitcode")
    private Long exitCode;
    @Column(name = "error")
    private String error;
    @Column(name = "startedat")
    private String startedAt;
    @Column(name = "finishedat")
    private String finishedAt;

    public static ContainerStatePO ofId(String containerId) {
        return ContainerStatePO.builder()
                .id(containerId)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ContainerStatePO that = (ContainerStatePO) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 394261028;
    }
}
