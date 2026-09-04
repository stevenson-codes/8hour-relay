package com.sbeve.relaytiming.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbeve.relaytiming.entities.RunnerEntity;
import com.sbeve.relaytiming.entities.TeamEntity;
import com.sbeve.relaytiming.repositories.RunnerRepository;
import com.sbeve.relaytiming.requests.CreateRunnerRequest;
import com.sbeve.relaytiming.requests.CreateRunnerResponse;
import com.sbeve.relaytiming.requests.CreateTeamRequest;
import com.sbeve.relaytiming.requests.CreateTeamResponse;
import com.sbeve.relaytiming.requests.RunnerResponse;
import com.sbeve.relaytiming.requests.TeamResponse;
import com.sbeve.relaytiming.services.RaceStateService;
import com.sbeve.relaytiming.services.TeamService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;
    private final RaceStateService raceStateService;
    private final RunnerRepository runnerRepository;

    public TeamController(TeamService teamService, RaceStateService raceStateService,
            RunnerRepository runnerRepository) {
        this.teamService = teamService;
        this.raceStateService = raceStateService;
        this.runnerRepository = runnerRepository;
    }

    @PostMapping
    public ResponseEntity<CreateTeamResponse> createTeam(@RequestBody CreateTeamRequest request) {
        TeamEntity team = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toTeamResponse(team));
    }

    @GetMapping("/{teamId}")
    public TeamResponse getTeam(@PathVariable Long teamId) {
        TeamEntity team = teamService.getTeam(teamId);
        List<RunnerResponse> runners = runnerRepository.findByTeamOrderByLeg(team).stream()
                .map(this::toRunnerResponse)
                .toList();
        return new TeamResponse(team.getId(), team.getName(), team.getDivision(), team.getTag().getEpcHex(),
                runners);
    }

    @PutMapping("/{teamId}")
    public CreateTeamResponse updateTeam(@PathVariable Long teamId, @RequestBody CreateTeamRequest request) {
        TeamEntity team = teamService.updateTeam(teamId, request);
        return toTeamResponse(team);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
        if (raceStateService.isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/runners")
    public ResponseEntity<CreateRunnerResponse> createRunner(@PathVariable Long teamId,
            @RequestBody CreateRunnerRequest request) {
        RunnerEntity runner = teamService.createRunner(teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCreateRunnerResponse(runner));
    }

    @PutMapping("/{teamId}/runners/{runnerId}")
    public CreateRunnerResponse updateRunner(@PathVariable Long teamId, @PathVariable Long runnerId,
            @RequestBody CreateRunnerRequest request) {
        RunnerEntity runner = teamService.updateRunner(teamId, runnerId, request);
        return toCreateRunnerResponse(runner);
    }

    @DeleteMapping("/{teamId}/runners/{runnerId}")
    public ResponseEntity<Void> deleteRunner(@PathVariable Long teamId, @PathVariable Long runnerId) {
        if (raceStateService.isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        teamService.deleteRunner(teamId, runnerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> wipeAll() {
        if (raceStateService.isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        teamService.wipeAll();
        return ResponseEntity.noContent().build();
    }

    private CreateTeamResponse toTeamResponse(TeamEntity team) {
        return new CreateTeamResponse(team.getId(), team.getName(), team.getDivision(), team.getTag().getEpcHex());
    }

    private CreateRunnerResponse toCreateRunnerResponse(RunnerEntity runner) {
        return new CreateRunnerResponse(runner.getId(), runner.getName(), runner.getLeg(), runner.getBib(),
                runner.getSex() == null ? null : runner.getSex().name(), runner.getStatus().name(),
                runner.getTeam().getId());
    }

    private RunnerResponse toRunnerResponse(RunnerEntity runner) {
        return new RunnerResponse(runner.getId(), runner.getName(), runner.getLeg(), runner.getBib(),
                runner.getSex() == null ? null : runner.getSex().name(), runner.getTag().getEpcHex(),
                runner.getStatus().name(), runner.getTeam().getId());
    }
}
