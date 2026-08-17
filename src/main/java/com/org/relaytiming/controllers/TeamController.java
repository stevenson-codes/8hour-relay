package com.org.relaytiming.controllers;

import com.org.relaytiming.dtos.TeamDTO;
import com.org.relaytiming.entities.TeamEntity;
import com.org.relaytiming.repositories.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

	private final TeamRepository teamRepository;

	public TeamController(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	@PostMapping
	public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO request) {
		TeamEntity savedTeam = teamRepository.save(new TeamEntity(request.id(), request.name()));
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(savedTeam));
	}

	@GetMapping
	public List<TeamDTO> getTeams() {
		return teamRepository.findAll().stream()
			.map(this::toDto)
			.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<TeamDTO> getTeam(@PathVariable Long id) {
		return teamRepository.findById(id)
			.map(team -> ResponseEntity.ok(toDto(team)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private TeamDTO toDto(TeamEntity team) {
		return new TeamDTO(team.getId(), team.getName());
	}
}
