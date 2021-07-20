package me.lyon.pul.repository;

import me.lyon.pul.model.po.PulPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PulRepository extends JpaRepository<PulPO, String> {
}
