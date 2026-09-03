package com.sbeve.relaytiming.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sbeve.relaytiming.config.Config;
import com.sbeve.relaytiming.dto.RunnerSummaryDto;
import com.sbeve.relaytiming.dto.RunnerDto;
import com.sbeve.relaytiming.dto.TeamSummaryDto;
import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.LapStatus;
import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.RunnerStatus;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.repositories.LapRecordRepository;
import com.sbeve.relaytiming.repositories.RunnerRepository;
import com.sbeve.relaytiming.repositories.TeamRepository;

@Service
public class RaceSummaryService {
    private static final double LAP_DISTANCE_KM = Config.LAP_DISTANCE_KM;

    private final TeamRepository teamRepository;
    private final RunnerRepository runnerRepository;
    private final LapRecordRepository lapRecordRepository;

    public RaceSummaryService(TeamRepository teamRepository, RunnerRepository runnerRepository,
            LapRecordRepository lapRecordRepository) {
        this.teamRepository = teamRepository;
        this.runnerRepository = runnerRepository;
        this.lapRecordRepository = lapRecordRepository;
    }

    public List<TeamSummaryDto> getSummary() {
        List<TeamEntity> teams = teamRepository.findAll();

        List<TeamAgg> teamAggs = new ArrayList<>();
        for (TeamEntity team : teams) {
            List<RunnerEntity> runners = runnerRepository.findByTeamOrderByLeg(team);
            List<RunnerAgg> runnerAggs = runners.stream().map(this::buildRunnerAgg).toList();
            teamAggs.add(buildTeamAgg(team, runnerAggs));
        }

        List<TeamAgg> byTotalLapsDesc = teamAggs.stream()
                .sorted(Comparator.comparingInt(TeamAgg::totalLaps).reversed())
                .toList();

        Map<TeamEntity, Integer> overallRanks = new HashMap<>();
        for (int i = 0; i < byTotalLapsDesc.size(); i++) {
            overallRanks.put(byTotalLapsDesc.get(i).team(), i + 1);
        }

        Map<TeamEntity, Integer> divisionRanks = new HashMap<>();
        Map<String, List<TeamAgg>> byDivision = new HashMap<>();
        for (TeamAgg agg : byTotalLapsDesc) {
            byDivision.computeIfAbsent(agg.team().getDivision(), key -> new ArrayList<>()).add(agg);
        }
        for (List<TeamAgg> group : byDivision.values()) {
            for (int i = 0; i < group.size(); i++) {
                divisionRanks.put(group.get(i).team(), i + 1);
            }
        }

        double leaderDistanceKm = byTotalLapsDesc.isEmpty() ? 0 : byTotalLapsDesc.get(0).totalDistanceKm();

        return byTotalLapsDesc.stream()
                .map(agg -> buildTeamSummaryDto(agg.team(), agg.runners(), agg.totalLaps(), agg.totalDistanceKm(),
                        agg.teamLastLapMillis(), agg.avgPaceSecPerKm(), overallRanks.get(agg.team()),
                        divisionRanks.get(agg.team()), leaderDistanceKm))
                .toList();
    }

    private RunnerAgg buildRunnerAgg(RunnerEntity runner) {
        List<LapRecordEntity> validLaps = lapRecordRepository.findByTagAndStatus(runner.getTag(), LapStatus.VALID);
        int laps = validLaps.size();

        Optional<LapRecordEntity> lastValidLap = validLaps.stream()
                .max(Comparator.comparing(LapRecordEntity::getTimestamp));
        Long lastLapMillis = lastValidLap.map(LapRecordEntity::getLapTime).orElse(null);

        Long bestLapMillis = validLaps.stream()
                .map(LapRecordEntity::getLapTime)
                .min(Comparator.naturalOrder())
                .orElse(null);

        Long avgLapMillis = laps == 0 ? null
                : (long) validLaps.stream().mapToLong(LapRecordEntity::getLapTime).average().orElseThrow();

        int turnCount = lapRecordRepository.findByTagAndStatus(runner.getTag(), LapStatus.START).size();

        Instant legStart = lapRecordRepository
                .findTopByTagAndStatusOrderByTimestampDesc(runner.getTag(), LapStatus.START)
                .map(LapRecordEntity::getTimestamp)
                .orElse(null);

        Instant lastRecordTimestamp = lapRecordRepository
                .findTopByTagEpcHexOrderByTimestampDesc(runner.getTag().getEpcHex())
                .map(LapRecordEntity::getTimestamp)
                .orElse(null);

        return new RunnerAgg(runner, laps, lastLapMillis, bestLapMillis, avgLapMillis, turnCount, legStart,
                lastRecordTimestamp);
    }

    private TeamAgg buildTeamAgg(TeamEntity team, List<RunnerAgg> runnerAggs) {
        List<LapRecordEntity> validLaps = lapRecordRepository.findByTagAndStatus(team.getTag(), LapStatus.VALID);
        int totalLaps = validLaps.size();
        double totalDistanceKm = totalLaps * LAP_DISTANCE_KM;

        Long teamLastLapMillis = validLaps.stream()
                .max(Comparator.comparing(LapRecordEntity::getTimestamp))
                .map(LapRecordEntity::getLapTime)
                .orElse(null);

        Long avgLapMillis = totalLaps == 0 ? null
                : (long) validLaps.stream().mapToLong(LapRecordEntity::getLapTime).average().orElseThrow();
        Long avgPaceSecPerKm = avgLapMillis == null
                ? null
                : Math.round((avgLapMillis / 1000.0) / LAP_DISTANCE_KM);

        return new TeamAgg(team, runnerAggs, totalLaps, totalDistanceKm, teamLastLapMillis, avgPaceSecPerKm);
    }

    private TeamSummaryDto buildTeamSummaryDto(TeamEntity team, List<RunnerAgg> runnerAggs, int totalLaps,
            double totalDistanceKm, Long teamLastLapMillis, Long avgPaceSecPerKm, int overallRank,
            int divisionRank, double leaderDistanceKm) {

        Optional<RunnerAgg> currentAgg = runnerAggs.stream()
                .filter(r -> r.runner().getStatus() == RunnerStatus.ACTIVE)
                .findFirst();

        int teamSize = runnerAggs.size();
        int nextLeg = currentAgg
                .map(r -> r.runner().getLeg() == teamSize ? 1 : r.runner().getLeg() + 1)
                .orElse(1);

        List<RunnerSummaryDto> runnerDtos = runnerAggs.stream()
                .map(agg -> buildRunnerSummaryDto(agg, currentAgg, nextLeg))
                .toList();

        RunnerDto currentRunnerRef = currentAgg.map(this::toRunner).orElse(null);
        Instant startTimeThisLeg = currentAgg.map(RunnerAgg::legStart).orElse(null);
        RunnerDto nextRunnerRef = runnerAggs.stream()
                .filter(r -> r.runner().getLeg() == nextLeg)
                .findFirst()
                .map(this::toRunner)
                .orElse(null);

        Double gapToLeaderKm = overallRank == 1 ? null : totalDistanceKm - leaderDistanceKm;

        return new TeamSummaryDto(team.getId(), team.getName(), team.getDivision(), overallRank, divisionRank,
                totalLaps, totalDistanceKm, gapToLeaderKm, currentRunnerRef, startTimeThisLeg, nextRunnerRef,
                teamLastLapMillis, avgPaceSecPerKm, runnerDtos);
    }

    private RunnerSummaryDto buildRunnerSummaryDto(RunnerAgg agg, Optional<RunnerAgg> currentAgg, int nextLeg) {
        RunnerEntity runner = agg.runner();
        boolean isCurrent = currentAgg.isPresent() && currentAgg.get().runner().getId().equals(runner.getId());
        boolean isNext = !isCurrent && runner.getLeg() == nextLeg;

        String status;
        String statusLabel;
        if (isCurrent) {
            status = "RUNNING";
            statusLabel = "Running Now (Leg " + agg.turnCount() + ")";
        } else if (isNext) {
            status = "NEXT";
            statusLabel = "Next to Run";
        } else if (agg.turnCount() > 0) {
            status = "COMPLETED";
            statusLabel = "Completed (Leg " + agg.turnCount() + ")";
        } else {
            status = "WAITING";
            statusLabel = "Waiting";
        }

        Long avgPaceSecPerKm = agg.avgLapMillis() == null
                ? null
                : Math.round((agg.avgLapMillis() / 1000.0) / LAP_DISTANCE_KM);

        Instant legEnd = isCurrent ? null : (agg.turnCount() > 0 ? agg.lastRecordTimestamp() : null);

        return new RunnerSummaryDto(runner.getLeg(), runner.getName(), runner.getBib(),
                runner.getSex() == null ? null : runner.getSex().name(), status, statusLabel, agg.laps(),
                agg.laps() * LAP_DISTANCE_KM, agg.lastLapMillis(), agg.bestLapMillis(), avgPaceSecPerKm,
                agg.legStart(), legEnd);
    }

    private RunnerDto toRunner(RunnerAgg agg) {
        RunnerEntity runner = agg.runner();
        return new RunnerDto(runner.getLeg(), runner.getName(), runner.getBib(),
                runner.getSex() == null ? null : runner.getSex().name());
    }
}
