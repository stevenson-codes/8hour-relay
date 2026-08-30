package com.sbeve.relaytiming.dto;

import java.util.List;

public record TeamSummaryDto(
        Long id,
        String name,
        int totalLaps,
        Long avgLapTimeMillis,
        List<RunnerSummaryDto> runners) {
}
