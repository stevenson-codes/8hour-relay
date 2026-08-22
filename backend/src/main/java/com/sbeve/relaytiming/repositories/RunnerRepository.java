package com.sbeve.relaytiming.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.TagEntity;

public interface RunnerRepository extends JpaRepository<RunnerEntity, Long> {
    Optional<RunnerEntity> findByTag(TagEntity tag);
}
