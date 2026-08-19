package com.org.relaytiming.services;

import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import com.org.relaytiming.repositories.LapRecordRepository;
import com.org.relaytiming.repositories.RunnerRepository;
import com.org.relaytiming.repositories.TeamRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class LapRecordService {

    private static final Logger logger = LoggerFactory.getLogger(LapRecordService.class);

    private final LapRecordRepository lapRecordRepository;
    private final TeamRepository teamRepository;
    private final RunnerRepository runnerRepository;

    public LapRecordService(LapRecordRepository lapRecordRepository, TeamRepository teamRepository, RunnerRepository runnerRepository) {
        this.lapRecordRepository = lapRecordRepository;
        this.teamRepository = teamRepository;
        this.runnerRepository = runnerRepository;
    }

    public void recordLap(String epcHex, String timestamp) {
        if (epcHex == null || epcHex.isBlank()) {
            logger.warn("Cannot record lap for blank EPC");
            return;
        }

        Optional<RunnerEntity> runnerOpt = runnerRepository.findByEpcHex(epcHex);
        Optional<TeamEntity> teamOpt = teamRepository.findByEpcHex(epcHex);
        if (runnerOpt.isEmpty() && teamOpt.isEmpty()) {
            logger.warn("No runner or team found for EPC {}. Skipping lap record.", epcHex);
            return;
        }

        LocalDateTime recordedAt = parseTimestamp(timestamp);

        if (runnerOpt.isPresent()) {
            recordRunnerLap(runnerOpt.get(), recordedAt);
            return;
        }

        recordTeamLap(teamOpt.get(), recordedAt);
    }

    private void recordRunnerLap(RunnerEntity runner, LocalDateTime recordedAt) {
        Integer latestLap = lapRecordRepository.findMaxLapNumberByRunner(runner);
        int lapNumber = (latestLap == null) ? 0 : latestLap + 1;

        Double lapTimeSeconds = 0.0;
        Optional<LapRecordEntity> previousLap = lapRecordRepository.findTopByRunnerOrderByLapNumberDesc(runner);
        if (previousLap.isPresent() && previousLap.get().getTimestamp() != null) {
            lapTimeSeconds = (double) Duration.between(previousLap.get().getTimestamp(), recordedAt).getSeconds();
        }

        if (lapNumber == 0) {
            lapTimeSeconds = 0.0;
        }

        LapRecordEntity lapRecord = new LapRecordEntity(runner, lapNumber, lapTimeSeconds, recordedAt);
        lapRecordRepository.save(lapRecord);

        logger.info("Saved runner lap for {} at {} (lap {}, lapTime={})",
                runner.getName(), recordedAt, lapNumber, lapTimeSeconds);
    }

    private void recordTeamLap(TeamEntity team, LocalDateTime recordedAt) {
        Integer latestLap = lapRecordRepository.findMaxLapNumberByTeam(team);
        int lapNumber = (latestLap == null) ? 0 : latestLap + 1;

        Double lapTimeSeconds = 0.0;
        Optional<LapRecordEntity> previousLap = lapRecordRepository.findTopByTeamOrderByLapNumberDesc(team);
        if (previousLap.isPresent() && previousLap.get().getTimestamp() != null) {
            lapTimeSeconds = (double) Duration.between(previousLap.get().getTimestamp(), recordedAt).getSeconds();
        }

        if (lapNumber == 0) {
            lapTimeSeconds = 0.0;
        }

        LapRecordEntity lapRecord = new LapRecordEntity(team, lapNumber, lapTimeSeconds, recordedAt);
        lapRecordRepository.save(lapRecord);

        logger.info("Saved team lap for {} at {} (lap {}, lapTime={})",
                team.getName(), recordedAt, lapNumber, lapTimeSeconds);
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return OffsetDateTime.parse(timestamp).toLocalDateTime();
        } catch (DateTimeParseException e) {
            logger.warn("Could not parse MQTT timestamp '{}', defaulting to now", timestamp);
            return LocalDateTime.now();
        }
    }
}
