package com.org.relaytiming.controllers;

import com.org.relaytiming.dtos.RunnerDTO;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.entities.TeamEntity;
import com.org.relaytiming.repositories.RunnerRepository;
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
@RequestMapping("/api/runners")
public class RunnerController {

	private final RunnerRepository runnerRepository;
	private final TeamRepository teamRepository;

	public RunnerController(RunnerRepository runnerRepository, TeamRepository teamRepository) {
		this.runnerRepository = runnerRepository;
		this.teamRepository = teamRepository;
	}

	@PostMapping
	public ResponseEntity<?> createRunner(@RequestBody RunnerDTO request) {
		TeamEntity team = teamRepository.findByName(request.teamName())
			.orElse(null);

		if (team == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("No team exists with name: " + request.teamName());
		}

		RunnerEntity runner = new RunnerEntity(team, String.valueOf(request.id()), request.name());
		runner.setId(request.id());

		RunnerEntity savedRunner = runnerRepository.save(runner);
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(savedRunner));
	}

	@GetMapping
	public List<RunnerDTO> getRunners() {
		return runnerRepository.findAll().stream()
			.map(this::toDto)
			.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<RunnerDTO> getRunner(@PathVariable Long id) {
		return runnerRepository.findById(id)
			.map(runner -> ResponseEntity.ok(toDto(runner)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private RunnerDTO toDto(RunnerEntity runner) {
		return new RunnerDTO(runner.getId(), runner.getName(), runner.getTeam().getName());
	}
}