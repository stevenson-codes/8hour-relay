package com.org.relaytiming.services;

import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.mqtt.R700TagEvent;
import com.org.relaytiming.repositories.RunnerRepository;
import com.org.relaytiming.repositories.TeamRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class R700EventService {

    private static final Logger logger = LoggerFactory.getLogger(R700EventService.class);
    private static final Duration PASS_WINDOW = Duration.ofSeconds(10);

    private final RunnerRepository runnerRepository;
    private final TeamRepository teamRepository;
    private final LapRecordService lapRecordService;
    private final Map<String, BestRead> strongestByPass = new ConcurrentHashMap<>();

    public R700EventService(RunnerRepository runnerRepository, TeamRepository teamRepository, LapRecordService lapRecordService) {
        this.runnerRepository = runnerRepository;
        this.teamRepository = teamRepository;
        this.lapRecordService = lapRecordService;
    }

    public void handleTagEvent(R700TagEvent event) {
        if (event == null || event.tagInventoryEvent() == null) {
            logger.warn("Received MQTT payload without tagInventoryEvent data");
            return;
        }

        R700TagEvent.TagInventoryEvent tag = event.tagInventoryEvent();
        if (tag.epcHex() == null || tag.epcHex().isBlank()) {
            logger.warn("Received MQTT payload without an EPC identifier");
            return;
        }

        if (runnerRepository.findByEpcHex(tag.epcHex()).isEmpty()) {
            return;
        }

        String passKey = tag.epcHex();
        Instant now = Instant.now();
        BestRead currentBest = strongestByPass.get(passKey);

        if (currentBest == null) {
            strongestByPass.put(passKey, new BestRead(tag.epcHex(), tag.antennaPort(), tag.peakRssiCdbm(), now, event.timestamp()));
            return;
        }

        if (isPassExpired(currentBest, now)) {
            flushBestRead(currentBest);
            strongestByPass.remove(passKey);
            strongestByPass.put(passKey, new BestRead(tag.epcHex(), tag.antennaPort(), tag.peakRssiCdbm(), now, event.timestamp()));
            return;
        }

        if (tag.peakRssiCdbm() > currentBest.peakRssiCdbm) {
            currentBest.peakRssiCdbm = tag.peakRssiCdbm();
            currentBest.seenAt = now;
            currentBest.timestamp = event.timestamp();
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void flushExpiredPasses() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, BestRead>> iterator = strongestByPass.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, BestRead> entry = iterator.next();
            BestRead bestRead = entry.getValue();

            if (isPassExpired(bestRead, now)) {
                iterator.remove();
                flushBestRead(bestRead);
            }
        }
    }

    private boolean isPassExpired(BestRead currentBest, Instant now) {
        return Duration.between(currentBest.seenAt, now).compareTo(PASS_WINDOW) > 0;
    }

    private void flushBestRead(BestRead bestRead) {
        if (bestRead == null) {
            return;
        }

        Optional<RunnerEntity> runner = runnerRepository.findByEpcHex(bestRead.epcHex);

        if (runner.isPresent()) {
            logger.info("[PASS] EPC: {} | Ant: {} | Strongest RSSI: {} dBm | Runner: {} | Timestamp: {}",
                    bestRead.epcHex,
                    bestRead.antennaPort,
                    bestRead.peakRssiCdbm / 100.0,
                    runner.get().getName(),
                    bestRead.timestamp);
            lapRecordService.recordLap(bestRead.epcHex, bestRead.timestamp);
        }
    }

    private static final class BestRead {
        private final String epcHex;
        private final int antennaPort;
        private int peakRssiCdbm;
        private Instant seenAt;
        private String timestamp;

        private BestRead(String epcHex, int antennaPort, int peakRssiCdbm, Instant seenAt, String timestamp) {
            this.epcHex = epcHex;
            this.antennaPort = antennaPort;
            this.peakRssiCdbm = peakRssiCdbm;
            this.seenAt = seenAt;
            this.timestamp = timestamp;
        }
    }
}
