package com.sbeve.relaytiming.services;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sbeve.relaytiming.repositories.TagRepository;

import jakarta.annotation.PreDestroy;

@Service
public class TagReadService {
    private static final Logger logger = LoggerFactory.getLogger(TagReadService.class);
    private static final long WINDOW_SECONDS = 10;

    private final Map<String, Read> readWindow = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TagRepository tagRepository;
    private final LapRecordService lapRecordService;

    public TagReadService(TagRepository tagRepository, LapRecordService lapRecordService) {
        this.tagRepository = tagRepository;
        this.lapRecordService = lapRecordService;
    }

    public void handleTagRead(String epcHex, String timestamp, int peakRssiCdbm) {
        if (!tagRepository.existsById(epcHex)) {
            return;
        }

        String passKey = epcHex;
        Instant now = Instant.now();
        AtomicBoolean windowOpened = new AtomicBoolean(false);

        readWindow.compute(passKey, (key, currentBest) -> {
            if (currentBest == null) {
                windowOpened.set(true);
                return new Read(now, peakRssiCdbm, Instant.parse(timestamp));
            }

            if (peakRssiCdbm > currentBest.peakRssiCdbm()) {
                return new Read(currentBest.windowStart(), peakRssiCdbm, Instant.parse(timestamp));
            }

            return currentBest;
        });

        if (windowOpened.get()) {
            scheduler.schedule(() -> flushRead(passKey, now), WINDOW_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void flushRead(String passKey, Instant windowStart) {
        readWindow.compute(passKey, (key, currentBest) -> {
            if (currentBest == null) {
                return currentBest;
            }

            logger.info("Tag {} strongest read in {}s window: {} cdBm", passKey, WINDOW_SECONDS, currentBest.peakRssiCdbm());
            lapRecordService.saveLapRecord(passKey, currentBest.timestamp());
            return null;
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

    private record Read(
        Instant windowStart, 
        int peakRssiCdbm, 
        Instant timestamp) {
    }
}
