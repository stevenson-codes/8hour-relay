package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.RunnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunnerRepository extends JpaRepository<RunnerEntity, Long> {
}
