package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LapRecordRepositoryTest {

    @Autowired
    private LapRecordRepository lapRecordRepository;

    @Autowired
    private RunnerRepository runnerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void testSaveAndFindLapRecord() {
        TeamEntity team = new TeamEntity(null, "TEAM-EPC-1", "Team A");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "EPC-001", "John Doe");
        runnerRepository.saveAndFlush(runner);

        LapRecordEntity lapRecord = new LapRecordEntity(runner, 1, 1, 300.5, LocalDateTime.now());
        lapRecordRepository.saveAndFlush(lapRecord);

        assertNotNull(lapRecord.getId());
    }

    @Test
    void testLapRecordRelationshipWithRunner() {
        TeamEntity team = new TeamEntity(null, "TEAM-EPC-2", "Team B");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "EPC-002", "Jane Smith");
        runnerRepository.saveAndFlush(runner);

        LapRecordEntity lapRecord = new LapRecordEntity(runner, 1, 1, 250.75, LocalDateTime.now());
        lapRecordRepository.saveAndFlush(lapRecord);

        Optional<LapRecordEntity> found = lapRecordRepository.findById(lapRecord.getId());

        assertTrue(found.isPresent());
        assertEquals(1, found.get().getRunnerLapNumber());
        assertEquals(250.75, found.get().getLapTime());
        assertEquals("Jane Smith", found.get().getRunner().getName());
    }
}
