package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
        TeamEntity team = new TeamEntity(null, "TEAM-EPC-10", "Team Runner A");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "EPC-100", "Runner One");
        runnerRepository.saveAndFlush(runner);

        Optional<RunnerEntity> found = runnerRepository.findById(runner.getId());

        assertTrue(found.isPresent());
        assertEquals("EPC-100", found.get().getEpcHex());
        assertEquals("Runner One", found.get().getName());
    }

    @Test
    void testRunnerTeamRelationshipIsPersisted() {
        TeamEntity team = new TeamEntity(null, "TEAM-EPC-11", "Team Runner B");
        teamRepository.saveAndFlush(team);

        RunnerEntity runner = new RunnerEntity(team, "EPC-101", "Runner Two");
        runnerRepository.saveAndFlush(runner);

        Optional<RunnerEntity> found = runnerRepository.findById(runner.getId());

        assertTrue(found.isPresent());
        assertEquals("Team Runner B", found.get().getTeam().getName());
        assertEquals(team.getId(), found.get().getTeam().getId());
    }
}