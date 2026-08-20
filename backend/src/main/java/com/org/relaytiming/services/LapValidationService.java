package com.org.relaytiming.services;

import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import com.org.relaytiming.repositories.LapRecordRepository;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LapValidationService {

    private static final Duration TEAM_READ_WINDOW = Duration.ofSeconds(5);

    private final LapRecordRepository lapRecordRepository;

    public LapValidationService(LapRecordRepository lapRecordRepository) {
        this.lapRecordRepository = lapRecordRepository;
    }

    public boolean hasRecentTeamRead(RunnerEntity runner, LocalDateTime recordedAt) {
        TeamEntity team = runner.getTeam();
        if (team == null || recordedAt == null) {
            return false;
        }

        LocalDateTime windowStart = recordedAt.minus(TEAM_READ_WINDOW);
        LocalDateTime windowEnd = recordedAt.plus(TEAM_READ_WINDOW);
        return lapRecordRepository.existsByTeamAndTimestampBetween(team, windowStart, windowEnd);
    }
}
