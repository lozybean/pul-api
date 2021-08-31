package me.lyon.pul.repository;

import me.lyon.pul.model.po.ContainerStatePO;
import me.lyon.pul.model.po.JobInfoPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobInfoRepository extends JpaRepository<JobInfoPO, Integer> {
    Optional<JobInfoPO> findByToken(String token);

    Optional<JobInfoPO> findByContainerState(ContainerStatePO state);
}
