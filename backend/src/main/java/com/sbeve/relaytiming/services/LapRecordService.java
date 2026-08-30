package com.sbeve.relaytiming.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.RunnerStatus;
import com.sbeve.relaytiming.config.Config;
import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.LapStatus;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TagType;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.repositories.LapRecordRepository;
import com.sbeve.relaytiming.repositories.TagRepository;
import com.sbeve.relaytiming.repositories.RunnerRepository;

@Service
public class LapRecordService {
    private static final Logger log = LoggerFactory.getLogger(LapRecordService.class);
    private static final List<LapStatus> VALID_STATUSES = List.of(LapStatus.START, LapStatus.VALID);
    private static final Duration HANDOFF_WINDOW = Duration.ofSeconds(Config.HANDOFF_WINDOW);
    private static final Duration LAP_TIMEOUT = Duration.ofMinutes(Config.LAP_TIMEOUT);

    private final LapRecordRepository lapRecordRepository;
    private final RunnerRepository runnerRepository;
    private final TagRepository tagRepository;

    public LapRecordService(LapRecordRepository lapRecordRepository, RunnerRepository runnerRepository, TagRepository tagRepository) {
        this.lapRecordRepository = lapRecordRepository;
        this.runnerRepository = runnerRepository;
        this.tagRepository = tagRepository;
    }

    public void clearAllLapRecords() {
        lapRecordRepository.deleteAll();
        log.atInfo().log("Cleared all lap records");
    }

    public void saveLapRecord(String epcHex, Instant timestamp) {
        Optional<TagEntity> tagOpt = tagRepository.findById(epcHex);
        if (tagOpt.isEmpty()) {
            log.atInfo().log("No tag found for epcHex {}", epcHex);
            return;
        }
        TagEntity tag = tagOpt.get();

        if (tag.getTagType() == TagType.RUNNER) {
            Optional<RunnerEntity> runnerOpt = runnerRepository.findByTag(tag);
            if (runnerOpt.isEmpty()) {
                log.atInfo().log("No runner found for tag {}", epcHex);
                return;
            }
            RunnerEntity runner = runnerOpt.get();

            if (runner.getStatus() != RunnerStatus.ACTIVE) {
                LapRecordEntity lapRecord = new LapRecordEntity(tag, timestamp, null, LapStatus.INVALID);
                lapRecordRepository.save(lapRecord);
                checkHandoff(runner, lapRecord);
                return;
            }

            LapRecordEntity lapRecord = buildLapRecord(tag, timestamp);
            lapRecordRepository.save(lapRecord);
            checkHandoff(runner, lapRecord);
            return;
        }

        lapRecordRepository.save(buildLapRecord(tag, timestamp));
    }

    private LapRecordEntity buildLapRecord(TagEntity tag, Instant timestamp) {
        Optional<LapRecordEntity> previousLap = lapRecordRepository
                .findTopByTagEpcHexAndStatusInOrderByTimestampDesc(tag.getEpcHex(), VALID_STATUSES);

        LapRecordEntity lapRecord;
        if (previousLap.isEmpty()) {
            lapRecord = new LapRecordEntity(tag, timestamp, null, LapStatus.START);
        } else {
            long lapTime = Duration.between(previousLap.get().getTimestamp(), timestamp).toMillis();
            lapRecord = new LapRecordEntity(tag, timestamp, lapTime, LapStatus.VALID);
        }

        log.atInfo().log("Saving lap record for tag {}: timestamp={}, lapTime={}, status={}",
                tag.getEpcHex(), timestamp, lapRecord.getLapTime(), lapRecord.getStatus());
        return lapRecord;
    }

    private void checkHandoff(RunnerEntity runner, LapRecordEntity lapRecord) {
        RunnerStatus runnerStatus = runner.getStatus();
        Integer leg = runner.getLeg();
        TeamEntity team = runner.getTeam();

        Optional<RunnerEntity> counterpartOpt;
        if (runnerStatus == RunnerStatus.ACTIVE) {
            if (runnerRepository.existsByTeamAndLeg(team, leg + 1)) {
                counterpartOpt = runnerRepository.findByTeamAndLeg(team, leg + 1);
            } else {
                counterpartOpt = runnerRepository.findByTeamAndLeg(team, 1);
            }
        } else {
            int prevLeg = leg > 1 ? leg - 1 : runnerRepository.countByTeam(team);
            counterpartOpt = runnerRepository.findByTeamAndStatusAndLeg(team, RunnerStatus.ACTIVE, prevLeg);
        }

        if (counterpartOpt.isEmpty()) {
            return;
        }
        RunnerEntity counterpart = counterpartOpt.get();

        RunnerEntity activeRunner = runnerStatus == RunnerStatus.ACTIVE ? runner : counterpart;
        Optional<LapRecordEntity> activeRunnerStart = lapRecordRepository
                .findTopByTagAndStatusOrderByTimestampDesc(activeRunner.getTag(), LapStatus.START);
        if (activeRunnerStart.isEmpty()) {
            return;
        }
        Duration sinceStart = Duration.between(activeRunnerStart.get().getTimestamp(), lapRecord.getTimestamp());
        if (sinceStart.compareTo(LAP_TIMEOUT) < 0) {
            return;
        }

        Optional<LapRecordEntity> counterpartLap = lapRecordRepository
                .findTopByTagEpcHexOrderByTimestampDesc(counterpart.getTag().getEpcHex());
        if (counterpartLap.isEmpty()) {
            return;
        }

        Duration gap = Duration.between(counterpartLap.get().getTimestamp(), lapRecord.getTimestamp()).abs();
        if (gap.compareTo(HANDOFF_WINDOW) > 0) {
            return;
        }

        RunnerEntity nextRunner = runnerStatus == RunnerStatus.INACTIVE ? runner : counterpart;
        LapRecordEntity nextLap = runnerStatus == RunnerStatus.INACTIVE ? lapRecord : counterpartLap.get();

        nextLap.setStatus(LapStatus.START);
        nextLap.setLapTime(null);
        lapRecordRepository.save(nextLap);

        activeRunner.setStatus(RunnerStatus.INACTIVE);
        nextRunner.setStatus(RunnerStatus.ACTIVE);
        runnerRepository.save(activeRunner);
        runnerRepository.save(nextRunner);

        log.atInfo().log("Handoff detected: {} (INACTIVE) -> {} (ACTIVE) at {}",
                activeRunner.getName(), nextRunner.getName(), lapRecord.getTimestamp());
    }
}
