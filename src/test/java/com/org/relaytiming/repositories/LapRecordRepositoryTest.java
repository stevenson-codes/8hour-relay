package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.lang.reflect.Field;
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
        TeamEntity team = new TeamEntity(1L, "Team A");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "R001", "John Doe");
        setRunnerId(runner, 1001L);
        runnerRepository.saveAndFlush(runner);

        LapRecordEntity lapRecord = new LapRecordEntity(runner, 1, 1, 300.5, LocalDateTime.now());
        lapRecordRepository.saveAndFlush(lapRecord);

        assertNotNull(lapRecord.getId());
    }

    @Test
    void testLapRecordRelationshipWithRunner() {
        TeamEntity team = new TeamEntity(2L, "Team B");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "R002", "Jane Smith");
        setRunnerId(runner, 1002L);
        runnerRepository.saveAndFlush(runner);

        LapRecordEntity lapRecord = new LapRecordEntity(runner, 1, 1, 250.75, LocalDateTime.now());
        lapRecordRepository.saveAndFlush(lapRecord);

        Optional<LapRecordEntity> found = lapRecordRepository.findById(lapRecord.getId());
        
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getRunnerLapNumber());
        assertEquals(250.75, found.get().getLapTime());
        assertEquals("Jane Smith", found.get().getRunner().getName());
    }

    private void setRunnerId(RunnerEntity runner, Long id) {
        try {
            Field idField = RunnerEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(runner, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set runner id for test setup", e);
        }
    }
}
