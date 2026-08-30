package com.sbeve.relaytiming.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbeve.relaytiming.dto.TeamSummaryDto;
import com.sbeve.relaytiming.services.TeamStatsService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamStatsService teamStatsService;

    public TeamController(TeamStatsService teamStatsService) {
        this.teamStatsService = teamStatsService;
    }

    @GetMapping
    public List<TeamSummaryDto> getAllTeams() {
        return teamStatsService.getAllTeamSummaries();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamSummaryDto> getTeam(@PathVariable Long id) {
        return teamStatsService.getTeamSummary(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
