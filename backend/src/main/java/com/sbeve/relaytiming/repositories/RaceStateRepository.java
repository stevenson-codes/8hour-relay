package com.sbeve.relaytiming.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.RaceStateEntity;

public interface RaceStateRepository extends JpaRepository<RaceStateEntity, Long> {
}
