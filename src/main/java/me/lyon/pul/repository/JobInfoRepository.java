package me.lyon.pul.repository;

import me.lyon.pul.constant.JobStatus;
import me.lyon.pul.model.po.JobInfoPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface JobInfoRepository extends JpaRepository<JobInfoPO, Integer> {
    Optional<JobInfoPO> findFirstByStatusInOrderByIdAsc(Collection<JobStatus> status);

    Optional<JobInfoPO> findByToken(String token);
}
