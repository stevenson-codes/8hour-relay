package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.RaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceRepository extends JpaRepository<RaceEntity, Long> {
}
