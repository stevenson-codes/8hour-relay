package com.sbeve.relaytiming.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.RunnerEntity;

public interface RunnerRepository extends JpaRepository<RunnerEntity, Long> {
}
