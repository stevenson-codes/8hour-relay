package com.sbeve.relaytiming.services;

import java.util.List;

import com.sbeve.relaytiming.entities.TeamEntity;

record TeamAgg(
        TeamEntity team,
        List<RunnerAgg> runners,
        int totalLaps,
        double totalDistanceKm,
        Long teamLastLapMillis,
        Long avgPaceSecPerKm) {
}
