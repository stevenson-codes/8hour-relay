package com.org.relaytiming.services;

import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import com.org.relaytiming.repositories.LapRecordRepository;
import com.org.relaytiming.repositories.RunnerRepository;
import com.org.relaytiming.repositories.TeamRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class LapRecordService {

    private static final Logger logger = LoggerFactory.getLogger(LapRecordService.class);
    private static final int TEAM_READ_VALIDATION_WINDOW_SECONDS = 5;

    // The team tag for the same pass can flush from R700EventService up to PASS_WINDOW (10s)
    // after the runner tag does, since each EPC is buffered independently. Give the team read
    // this much wall-clock time to show up before giving up on a runner read.
    private static final Duration PENDING_RUNNER_LAP_TIMEOUT = Duration.ofSeconds(10);

    private final LapRecordRepository lapRecordRepository;
    private final TeamRepository teamRepository;
    private final RunnerRepository runnerRepository;
    private final LapValidationService lapValidationService;
    private final ConcurrentLinkedQueue<PendingRunnerLap> pendingRunnerLaps = new ConcurrentLinkedQueue<>();

    public LapRecordService(LapRecordRepository lapRecordRepository, TeamRepository teamRepository,
            RunnerRepository runnerRepository, LapValidationService lapValidationService) {
        this.lapRecordRepository = lapRecordRepository;
        this.teamRepository = teamRepository;
        this.runnerRepository = runnerRepository;
        this.lapValidationService = lapValidationService;
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
        if (!lapValidationService.hasRecentTeamRead(runner, recordedAt)) {
            logger.info("No team read yet within {}s of runner {} read at {}. Holding for retry.",
                    TEAM_READ_VALIDATION_WINDOW_SECONDS, runner.getName(), recordedAt);
            pendingRunnerLaps.add(new PendingRunnerLap(runner, recordedAt, Instant.now()));
            return;
        }

        saveRunnerLap(runner, recordedAt);
    }

    @Scheduled(fixedDelay = 1000)
    public void retryPendingRunnerLaps() {
        if (pendingRunnerLaps.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        Iterator<PendingRunnerLap> iterator = pendingRunnerLaps.iterator();
        while (iterator.hasNext()) {
            PendingRunnerLap pending = iterator.next();

            if (lapValidationService.hasRecentTeamRead(pending.runner(), pending.recordedAt())) {
                iterator.remove();
                saveRunnerLap(pending.runner(), pending.recordedAt());
                continue;
            }

            if (Duration.between(pending.queuedAt(), now).compareTo(PENDING_RUNNER_LAP_TIMEOUT) > 0) {
                iterator.remove();
                logger.warn("No team read within {}s of runner {} read at {} after waiting {}s. Discarding lap record.",
                        TEAM_READ_VALIDATION_WINDOW_SECONDS, pending.runner().getName(), pending.recordedAt(),
                        PENDING_RUNNER_LAP_TIMEOUT.getSeconds());
            }
        }
    }

    private void saveRunnerLap(RunnerEntity runner, LocalDateTime recordedAt) {
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

    private record PendingRunnerLap(RunnerEntity runner, LocalDateTime recordedAt, Instant queuedAt) {
    }
}
