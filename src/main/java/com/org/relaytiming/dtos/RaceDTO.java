package com.org.relaytiming.dtos;

import java.time.LocalDateTime;

public record RaceDTO(
    Long id,
    LocalDateTime startedAt
) {}
