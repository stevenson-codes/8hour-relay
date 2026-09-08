package com.sbeve.relaytiming.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.config.Config;
import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.entities.enums.LapStatus;
import com.sbeve.relaytiming.entities.enums.RunnerStatus;
import com.sbeve.relaytiming.entities.enums.TagType;
import com.sbeve.relaytiming.repositories.LapRecordRepository;
import com.sbeve.relaytiming.repositories.TagRepository;
import com.sbeve.relaytiming.repositories.RunnerRepository;

@Service
public class LapRecordService {
    private static final Logger log = LoggerFactory.getLogger(LapRecordService.class);
    private static final List<LapStatus> VALID_STATUSES = List.of(LapStatus.START, LapStatus.VALID);
    private static final Duration HANDOFF_WINDOW = Duration.ofMillis(Config.HANDOFF_WINDOW);
    private static final Duration HANDOFF_ENABLED_WINDOW = Duration.ofMinutes(Config.HANDOFF_ENABLED_WINDOW);
    private static final Duration LEG_TIME = Duration.ofMinutes(Config.LEG_TIME);
    private static final Duration LEG_TIMEOUT = Duration.ofMinutes(Config.LEG_TIMEOUT);

    private final LapRecordRepository lapRecordRepository;
    private final RunnerRepository runnerRepository;
    private final TagRepository tagRepository;
    private final RaceStateService raceStateService;

    public LapRecordService(LapRecordRepository lapRecordRepository, RunnerRepository runnerRepository,
            TagRepository tagRepository, RaceStateService raceStateService) {
        this.lapRecordRepository = lapRecordRepository;
        this.runnerRepository = runnerRepository;
        this.tagRepository = tagRepository;
        this.raceStateService = raceStateService;
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
        if (previousLap.isEmpty()
                || Duration.between(previousLap.get().getTimestamp(), timestamp).compareTo(LEG_TIMEOUT) > 0) {
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

        Instant now = Instant.now();

        // A handoff's own START record can itself still fall inside the same enabled
        // window that let it through (e.g. the outgoing runner's tag lingers near the
        // reader, or the newly active runner's first lap read lines up with the outgoing
        // runner's last one). Without this, that can trigger a second handoff for the
        // same team before the window that produced the first one closes. The window is
        // HANDOFF_ENABLED_WINDOW wide on both sides of the leg boundary, so the cooldown
        // needs to cover the full 2x span to guarantee it can't reopen mid-window.
        // This reads back handoffAt (stamped with the server clock below), not
        // lapRecord's own timestamp (the RFID reader's clock) - comparing a reader
        // timestamp against Instant.now() here would be thrown off by reader clock skew,
        // the same problem that made the leg-window check unreliable earlier.
        Optional<LapRecordEntity> lastHandoff = lapRecordRepository
                .findTopByTagAndHandoffAtIsNotNullOrderByHandoffAtDesc(activeRunner.getTag());
        if (lastHandoff.isPresent()
                && Duration.between(lastHandoff.get().getHandoffAt(), now)
                        .compareTo(HANDOFF_ENABLED_WINDOW.multipliedBy(2)) < 0) {
            return;
        }

        Instant raceStart = raceStateService.getStartedAt();
        if (raceStart == null || now.isBefore(raceStart)) {
            return;
        }
        Duration sinceRaceStart = Duration.between(raceStart, now);

        // Race start (t=0) isn't a real leg boundary - there's no leg before it to hand
        // off from - but it lands on one via the modulo below, which would otherwise open
        // a bogus window from t=0 to HANDOFF_ENABLED_WINDOW.
        if (sinceRaceStart.compareTo(HANDOFF_ENABLED_WINDOW) < 0) {
            return;
        }
        Duration sinceLegStart = Duration.ofMillis(sinceRaceStart.toMillis() % LEG_TIME.toMillis());
        Duration untilNextLegStart = LEG_TIME.minus(sinceLegStart);
        Duration distanceToLegBoundary = sinceLegStart.compareTo(untilNextLegStart) < 0 ? sinceLegStart
                : untilNextLegStart;
        if (distanceToLegBoundary.compareTo(HANDOFF_ENABLED_WINDOW) > 0) {
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
        nextLap.setHandoffAt(now);
        lapRecordRepository.save(nextLap);

        activeRunner.setStatus(RunnerStatus.INACTIVE);
        nextRunner.setStatus(RunnerStatus.ACTIVE);
        runnerRepository.save(activeRunner);
        runnerRepository.save(nextRunner);

        log.atInfo().log("Handoff detected: {} (INACTIVE) -> {} (ACTIVE) at {}",
                activeRunner.getName(), nextRunner.getName(), lapRecord.getTimestamp());
    }
}
