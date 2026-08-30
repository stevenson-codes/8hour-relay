package com.sbeve.relaytiming.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sbeve.relaytiming.dto.RunnerSummaryDto;
import com.sbeve.relaytiming.dto.TeamSummaryDto;
import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.LapStatus;
import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.repositories.LapRecordRepository;
import com.sbeve.relaytiming.repositories.RunnerRepository;
import com.sbeve.relaytiming.repositories.TeamRepository;

@Service
public class TeamStatsService {
    private final TeamRepository teamRepository;
    private final RunnerRepository runnerRepository;
    private final LapRecordRepository lapRecordRepository;

    public TeamStatsService(TeamRepository teamRepository, RunnerRepository runnerRepository,
            LapRecordRepository lapRecordRepository) {
        this.teamRepository = teamRepository;
        this.runnerRepository = runnerRepository;
        this.lapRecordRepository = lapRecordRepository;
    }

    public List<TeamSummaryDto> getAllTeamSummaries() {
        return teamRepository.findAll().stream()
                .map(this::buildTeamSummary)
                .toList();
    }

    public Optional<TeamSummaryDto> getTeamSummary(Long teamId) {
        return teamRepository.findById(teamId).map(this::buildTeamSummary);
    }

    private TeamSummaryDto buildTeamSummary(TeamEntity team) {
        List<RunnerSummaryDto> runners = runnerRepository.findByTeamOrderByLeg(team).stream()
                .map(this::buildRunnerSummary)
                .toList();

        int totalLaps = runners.stream().mapToInt(RunnerSummaryDto::lapsCompleted).sum();
        Long avgLapTimeMillis = weightedAverageLapTime(runners, totalLaps);

        return new TeamSummaryDto(team.getId(), team.getName(), totalLaps, avgLapTimeMillis, runners);
    }

    private RunnerSummaryDto buildRunnerSummary(RunnerEntity runner) {
        List<LapRecordEntity> validLaps = lapRecordRepository.findByTagAndStatus(runner.getTag(), LapStatus.VALID);

        int lapsCompleted = validLaps.size();
        Long avgLapTimeMillis = lapsCompleted == 0
                ? null
                : (long) validLaps.stream().mapToLong(LapRecordEntity::getLapTime).average().orElseThrow();

        return new RunnerSummaryDto(runner.getId(), runner.getName(), runner.getLeg(), runner.getStatus(),
                lapsCompleted, avgLapTimeMillis);
    }

    private Long weightedAverageLapTime(List<RunnerSummaryDto> runners, int totalLaps) {
        if (totalLaps == 0) {
            return null;
        }

        long totalMillis = runners.stream()
                .filter(runner -> runner.avgLapTimeMillis() != null)
                .mapToLong(runner -> runner.avgLapTimeMillis() * runner.lapsCompleted())
                .sum();

        return totalMillis / totalLaps;
    }
}
