package com.org.relaytiming.services;

import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.repositories.LapRecordRepository;
import com.org.relaytiming.repositories.RunnerRepository;
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
    private final RunnerRepository runnerRepository;

    public LapRecordService(LapRecordRepository lapRecordRepository, RunnerRepository runnerRepository) {
        this.lapRecordRepository = lapRecordRepository;
        this.runnerRepository = runnerRepository;
    }

    public void recordLap(String epcHex, String timestamp) {
        if (epcHex == null || epcHex.isBlank()) {
            logger.warn("Cannot record lap for blank EPC");
            return;
        }

        Optional<RunnerEntity> runnerOpt = runnerRepository.findByEpcHex(epcHex);
        if (runnerOpt.isEmpty()) {
            logger.warn("No runner found for EPC {}. Skipping lap record.", epcHex);
            return;
        }

        RunnerEntity runner = runnerOpt.get();
        LocalDateTime recordedAt = parseTimestamp(timestamp);

        Integer latestRunnerLap = lapRecordRepository.findMaxRunnerLapNumberByRunner(runner);
        Integer latestTeamLap = lapRecordRepository.findMaxTeamLapNumberByRunnerTeamId(runner.getTeam().getId());

        int runnerLapNumber = (latestRunnerLap == null) ? 0 : latestRunnerLap + 1;
        int teamLapNumber = (latestTeamLap == null) ? 0 : latestTeamLap + 1;

        Double lapTimeSeconds = 0.0;
        Optional<LapRecordEntity> previousLap = lapRecordRepository.findTopByRunnerOrderByRunnerLapNumberDesc(runner);
        if (previousLap.isPresent() && previousLap.get().getTimestamp() != null) {
            lapTimeSeconds = (double) Duration.between(previousLap.get().getTimestamp(), recordedAt).getSeconds();
        }

        if (runnerLapNumber == 0) {
            lapTimeSeconds = 0.0;
        }

        LapRecordEntity lapRecord = new LapRecordEntity(runner, teamLapNumber, runnerLapNumber, lapTimeSeconds, recordedAt);
        lapRecordRepository.save(lapRecord);

        logger.info("Saved lap record for runner {} at {} (team lap {}, runner lap {}, lapTime={})",
                runner.getName(), recordedAt, teamLapNumber, runnerLapNumber, lapTimeSeconds);
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
