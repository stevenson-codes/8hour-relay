package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LapRecordRepository extends JpaRepository<LapRecordEntity, Long> {
    @Query("select max(l.runnerLapNumber) from LapRecordEntity l where l.runner = :runner")
    Integer findMaxRunnerLapNumberByRunner(@Param("runner") RunnerEntity runner);

    @Query("select max(l.teamLapNumber) from LapRecordEntity l where l.runner.team.id = :teamId")
    Integer findMaxTeamLapNumberByRunnerTeamId(@Param("teamId") Long teamId);

    Optional<LapRecordEntity> findTopByRunnerOrderByRunnerLapNumberDesc(RunnerEntity runner);
}
