package me.lyon.pul.repository;

import me.lyon.pul.model.po.JobInfoPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobInfoRepository extends JpaRepository<Integer, JobInfoPO> {
}
