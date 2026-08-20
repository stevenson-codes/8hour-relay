package com.org.relaytiming.dtos;

import java.time.LocalDateTime;

public record LapRecordDTO(
    Long id,
    Long runnerId,
    Integer lapNumber,
    Double lapTime,
    LocalDateTime timestamp
) {
}