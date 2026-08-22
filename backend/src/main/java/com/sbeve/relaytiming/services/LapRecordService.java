package com.sbeve.relaytiming.services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.LapStatus;
import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.repositories.LapRecordRepository;
import com.sbeve.relaytiming.repositories.TagRepository;

@Service
public class LapRecordService {
    private static final List<LapStatus> COMPLETED_LAP_STATUSES = List.of(LapStatus.START, LapStatus.VALID);

    private final LapRecordRepository lapRecordRepository;
    private final TagRepository tagRepository;

    public LapRecordService(LapRecordRepository lapRecordRepository, TagRepository tagRepository) {
        this.lapRecordRepository = lapRecordRepository;
        this.tagRepository = tagRepository;
    }

    public void saveLapRecord(String epcHex, Instant timestamp) {
        TagEntity tag = tagRepository.getReferenceById(epcHex);
        Optional<LapRecordEntity> previousLap = lapRecordRepository
                .findTopByTagEpcHexAndStatusInOrderByTimestampDesc(epcHex, COMPLETED_LAP_STATUSES);

        LapRecordEntity lapRecord;
        if (previousLap.isEmpty()) {
            lapRecord = new LapRecordEntity(tag, timestamp, null, LapStatus.START);
        } else {
            long lapTime = Duration.between(previousLap.get().getTimestamp(), timestamp).toMillis();
            lapRecord = new LapRecordEntity(tag, timestamp, lapTime, LapStatus.VALID);
        }

        lapRecordRepository.save(lapRecord);
    }
}
