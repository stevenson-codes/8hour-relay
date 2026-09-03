package com.sbeve.relaytiming.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.requests.CreateRunnerRequest;
import com.sbeve.relaytiming.requests.CreateRunnerResponse;
import com.sbeve.relaytiming.requests.CreateTeamRequest;
import com.sbeve.relaytiming.requests.CreateTeamResponse;
import com.sbeve.relaytiming.services.RaceStateService;
import com.sbeve.relaytiming.services.TeamService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;
    private final RaceStateService raceStateService;

    public TeamController(TeamService teamService, RaceStateService raceStateService) {
        this.teamService = teamService;
        this.raceStateService = raceStateService;
    }

    @PostMapping
    public ResponseEntity<CreateTeamResponse> createTeam(@RequestBody CreateTeamRequest request) {
        TeamEntity team = teamService.createTeam(request);
        CreateTeamResponse response = new CreateTeamResponse(team.getId(), team.getName(), team.getDivision(),
                team.getTag().getEpcHex());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{teamId}/runners")
    public ResponseEntity<CreateRunnerResponse> createRunner(@PathVariable Long teamId,
            @RequestBody CreateRunnerRequest request) {
        RunnerEntity runner = teamService.createRunner(teamId, request);
        CreateRunnerResponse response = new CreateRunnerResponse(runner.getId(), runner.getName(), runner.getLeg(),
                runner.getBib(), runner.getSex() == null ? null : runner.getSex().name(),
                runner.getStatus().name(), teamId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> wipeAll() {
        if (raceStateService.isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        teamService.wipeAll();
        return ResponseEntity.noContent().build();
    }
}
