package me.lyon.pul.repository;

import me.lyon.pul.model.po.SpeciesPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesRepository extends JpaRepository<SpeciesPO, Integer> {
}
