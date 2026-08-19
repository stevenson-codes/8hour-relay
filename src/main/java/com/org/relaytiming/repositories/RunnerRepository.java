package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.RunnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RunnerRepository extends JpaRepository<RunnerEntity, Long> {
    Optional<RunnerEntity> findByRunnerID(String runnerID);
}
