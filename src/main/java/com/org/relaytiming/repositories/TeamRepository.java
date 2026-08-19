package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
	Optional<TeamEntity> findByName(String name);

	Optional<RunnerEntity> findByEpcHex(String epcHex);
}
