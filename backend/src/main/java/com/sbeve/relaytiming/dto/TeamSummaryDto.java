package com.sbeve.relaytiming.dto;

import java.time.Instant;
import java.util.List;

public record TeamSummaryDto(
        Long id,
        String name,
        String division,
        int overallRank,
        int divisionRank,
        int totalLaps,
        double totalDistanceKm,
        Double gapToLeaderKm,
        RunnerDto currentRunner,
        Instant startTimeThisLeg,
        RunnerDto nextRunner,
        Long teamLastLapMillis,
        Long avgPaceSecPerKm,
        List<RunnerSummaryDto> runners) {
}
