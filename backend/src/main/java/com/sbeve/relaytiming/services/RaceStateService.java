package com.sbeve.relaytiming.services;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

import com.sbeve.relaytiming.entities.RaceStateEntity;
import com.sbeve.relaytiming.repositories.RaceStateRepository;

@Service
public class RaceStateService {
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final RaceStateRepository raceStateRepository;

    public RaceStateService(RaceStateRepository raceStateRepository) {
        this.raceStateRepository = raceStateRepository;
    }

    public boolean isActive() {
        return active.get();
    }

    public void start() {
        active.set(true);
        raceStateRepository.save(new RaceStateEntity(Instant.now()));
    }

    public void stop() {
        active.set(false);
    }
}
