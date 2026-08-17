package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.TeamEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void testSaveAndFindTeamById() {
        TeamEntity team = new TeamEntity(20L, "Team Alpha");
        teamRepository.saveAndFlush(team);

        Optional<TeamEntity> found = teamRepository.findById(20L);

        assertTrue(found.isPresent());
        assertEquals("Team Alpha", found.get().getName());
    }

    @Test
    void testUpdateTeamName() {
        TeamEntity team = new TeamEntity(21L, "Team Beta");
        teamRepository.saveAndFlush(team);

        team.setName("Team Beta Updated");
        teamRepository.saveAndFlush(team);

        Optional<TeamEntity> found = teamRepository.findById(21L);

        assertTrue(found.isPresent());
        assertEquals("Team Beta Updated", found.get().getName());
    }
}