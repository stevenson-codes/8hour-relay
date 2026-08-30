package com.sbeve.relaytiming.services;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class RaceStateService {
    private final AtomicBoolean active = new AtomicBoolean(false);

    public boolean isActive() {
        return active.get();
    }

    public void start() {
        active.set(true);
    }

    public void stop() {
        active.set(false);
    }
}
