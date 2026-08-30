package com.sbeve.relaytiming.services;

import com.sbeve.relaytiming.config.Config;
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
    private static final Logger log = LoggerFactory.getLogger(TagReadService.class);
    private static final long READ_WINDOW = Config.READ_WINDOW;

    private final Map<String, Read> readWindow = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TagRepository tagRepository;
    private final LapRecordService lapRecordService;
    private final RaceStateService raceStateService;

    public TagReadService(TagRepository tagRepository, LapRecordService lapRecordService,
            RaceStateService raceStateService) {
        this.tagRepository = tagRepository;
        this.lapRecordService = lapRecordService;
        this.raceStateService = raceStateService;
    }

    public void handleTagRead(String epcHex, String timestamp, int peakRssiCdbm) {
        if (!raceStateService.isActive()) {
            return;
        }

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
            scheduler.schedule(() -> flushRead(passKey, now), READ_WINDOW, TimeUnit.SECONDS);
        }
    }

    private void flushRead(String passKey, Instant windowStart) {
        readWindow.compute(passKey, (key, currentBest) -> {
            if (currentBest == null) {
                return currentBest;
            }

            log.info("Tag {} strongest read in {}s window: {} cdBm", passKey, READ_WINDOW, currentBest.peakRssiCdbm());
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
