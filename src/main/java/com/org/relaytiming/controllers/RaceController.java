package com.org.relaytiming.controllers;

import com.org.relaytiming.dtos.RaceDTO;
import com.org.relaytiming.entities.RaceEntity;
import com.org.relaytiming.repositories.RaceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/race")
public class RaceController {

	private static final Long RACE_ID = 1L;

	private final RaceRepository raceRepository;

	public RaceController(RaceRepository raceRepository) {
		this.raceRepository = raceRepository;
	}

	@PutMapping("/start")
	public ResponseEntity<RaceDTO> startRace() {
		RaceEntity savedRace = raceRepository.save(new RaceEntity(RACE_ID, LocalDateTime.now()));
		return ResponseEntity.ok(toDto(savedRace));
	}

	@GetMapping
	public ResponseEntity<RaceDTO> getRace() {
		return raceRepository.findById(RACE_ID)
			.map(race -> ResponseEntity.ok(toDto(race)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private RaceDTO toDto(RaceEntity race) {
		return new RaceDTO(race.getId(), race.getStartedAt());
	}
}
