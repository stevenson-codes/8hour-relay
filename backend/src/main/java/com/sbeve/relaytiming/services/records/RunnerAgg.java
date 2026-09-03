package com.sbeve.relaytiming.services.records;

import java.time.Instant;

import com.sbeve.relaytiming.entities.RunnerEntity;

public record RunnerAgg(
        RunnerEntity runner,
        int laps,
        Long lastLapMillis,
        Long bestLapMillis,
        Long avgLapMillis,
        int turnCount,
        Instant legStart,
        Instant lastRecordTimestamp) {
}
