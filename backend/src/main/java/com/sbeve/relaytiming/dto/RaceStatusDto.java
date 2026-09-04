package com.sbeve.relaytiming.dto;

import java.time.Instant;

public record RaceStatusDto(boolean active, Instant startedAt) {
}
