package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class RunnerRepositoryTest {

    @Autowired
    private RunnerRepository runnerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void testSaveAndFindRunnerById() {
        TeamEntity team = new TeamEntity(10L, "Team Runner A");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "R100", "Runner One");
        setRunnerId(runner, 1100L);
        runnerRepository.saveAndFlush(runner);

        Optional<RunnerEntity> found = runnerRepository.findById(1100L);

        assertTrue(found.isPresent());
        assertEquals("R100", found.get().getRunnerID());
        assertEquals("Runner One", found.get().getName());
    }

    @Test
    void testRunnerTeamRelationshipIsPersisted() {
        TeamEntity team = new TeamEntity(11L, "Team Runner B");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "R101", "Runner Two");
        setRunnerId(runner, 1101L);
        runnerRepository.saveAndFlush(runner);

        Optional<RunnerEntity> found = runnerRepository.findById(1101L);

        assertTrue(found.isPresent());
        assertEquals("Team Runner B", found.get().getTeam().getName());
        assertEquals(11L, found.get().getTeam().getId());
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