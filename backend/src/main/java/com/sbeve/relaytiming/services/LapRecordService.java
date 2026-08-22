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
import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.LapStatus;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TagType;
import com.sbeve.relaytiming.repositories.LapRecordRepository;
import com.sbeve.relaytiming.repositories.TagRepository;
import com.sbeve.relaytiming.repositories.RunnerRepository;

@Service
public class LapRecordService {
    private static final Logger log = LoggerFactory.getLogger(LapRecordService.class);
    private static final List<LapStatus> VALID_STATUSES = List.of(LapStatus.START, LapStatus.VALID);

    private final LapRecordRepository lapRecordRepository;
    private final RunnerRepository runnerRepository;
    private final TagRepository tagRepository;

    public LapRecordService(LapRecordRepository lapRecordRepository, RunnerRepository runnerRepository, TagRepository tagRepository) {
        this.lapRecordRepository = lapRecordRepository;
        this.runnerRepository = runnerRepository;
        this.tagRepository = tagRepository;
    }

    public void saveLapRecord(String epcHex, Instant timestamp) {
        TagEntity tag = tagRepository.getReferenceById(epcHex);

        if (tag.getTagType() == TagType.RUNNER) {
            Optional<RunnerEntity> runner = runnerRepository.findByTag(tag);
            if (runner.isEmpty()) {
                log.atInfo().log("No runner found for tag {}", epcHex);
                return;
            }

            if (runner.get().getStatus() != RunnerStatus.ACTIVE) {
                lapRecordRepository.save(new LapRecordEntity(tag, timestamp, null, LapStatus.INVALID));
                return;
            }
        }

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
                epcHex, timestamp, lapRecord.getLapTime(), lapRecord.getStatus());
        lapRecordRepository.save(lapRecord);
    }
}
