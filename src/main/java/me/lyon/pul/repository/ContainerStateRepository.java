package me.lyon.pul.repository;

import me.lyon.pul.model.po.ContainerStatePO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerStateRepository extends JpaRepository<ContainerStatePO, String> {
}
