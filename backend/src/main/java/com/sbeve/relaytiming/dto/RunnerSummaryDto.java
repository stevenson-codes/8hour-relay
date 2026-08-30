package com.sbeve.relaytiming.dto;

import com.sbeve.relaytiming.entities.RunnerStatus;

public record RunnerSummaryDto(
        Long id,
        String name,
        Integer leg,
        RunnerStatus status,
        int lapsCompleted,
        Long avgLapTimeMillis) {
}
