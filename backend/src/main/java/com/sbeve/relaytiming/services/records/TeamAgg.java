package com.sbeve.relaytiming.services.records;

import java.util.List;

import com.sbeve.relaytiming.entities.TeamEntity;

public record TeamAgg(
        TeamEntity team,
        List<RunnerAgg> runners,
        int totalLaps,
        double totalDistanceKm,
        Long teamLastLapMillis,
        Long avgPaceSecPerKm) {
}
