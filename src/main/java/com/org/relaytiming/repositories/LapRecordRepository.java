package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LapRecordRepository extends JpaRepository<LapRecordEntity, Long> {
    @Query("select max(l.lapNumber) from LapRecordEntity l where l.runner = :runner")
    Integer findMaxLapNumberByRunner(@Param("runner") RunnerEntity runner);

    Optional<LapRecordEntity> findTopByRunnerOrderByLapNumberDesc(RunnerEntity runner);

    @Query("select max(l.lapNumber) from LapRecordEntity l where l.team = :team")
    Integer findMaxLapNumberByTeam(@Param("team") TeamEntity team);

    Optional<LapRecordEntity> findTopByTeamOrderByLapNumberDesc(TeamEntity team);
}
