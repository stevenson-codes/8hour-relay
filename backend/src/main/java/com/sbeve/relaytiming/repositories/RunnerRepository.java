package com.sbeve.relaytiming.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.RunnerStatus;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TeamEntity;

public interface RunnerRepository extends JpaRepository<RunnerEntity, Long> {
    Optional<RunnerEntity> findByTag(TagEntity tag);
    Optional<RunnerEntity> findByTeamAndLeg(TeamEntity team, Integer leg);
    Optional<RunnerEntity> findByTeamAndStatusAndLeg(TeamEntity team, RunnerStatus status, Integer leg);
    boolean existsByTeamAndLeg(TeamEntity team, Integer leg);
}
