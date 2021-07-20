package me.lyon.pul.repository;

import me.lyon.pul.model.po.GenePO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface GeneRepository extends JpaRepository<GenePO, String> {
    List<GenePO> findAllByPulId(String pulId);
}
