package com.sbeve.relaytiming.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.entities.enums.RunnerStatus;

public interface RunnerRepository extends JpaRepository<RunnerEntity, Long> {
    Optional<RunnerEntity> findByTag(TagEntity tag);
    boolean existsByTag(TagEntity tag);
    Optional<RunnerEntity> findByTeamAndLeg(TeamEntity team, Integer leg);
    Optional<RunnerEntity> findByTeamAndStatusAndLeg(TeamEntity team, RunnerStatus status, Integer leg);
    boolean existsByTeamAndLeg(TeamEntity team, Integer leg);
    List<RunnerEntity> findByTeamOrderByLeg(TeamEntity team);
    int countByTeam(TeamEntity team);
}
