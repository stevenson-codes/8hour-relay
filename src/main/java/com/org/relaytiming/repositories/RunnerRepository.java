package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.Runners;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunnerRepository extends JpaRepository<Runners, Long> {
}
