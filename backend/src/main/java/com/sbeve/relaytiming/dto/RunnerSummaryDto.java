package com.sbeve.relaytiming.dto;

import java.time.Instant;

public record RunnerSummaryDto(
        int leg,
        String name,
        String bib,
        String sex,
        String status,
        String statusLabel,
        int laps,
        double distanceKm,
        Long lastLapMillis,
        Long bestLapMillis,
        Long avgPaceSecPerKm,
        Instant legStart,
        Instant legEnd) {
}
