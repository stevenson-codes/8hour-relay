package com.org.relaytiming.controllers;

import com.org.relaytiming.dtos.LapRecordDTO;
import com.org.relaytiming.entities.LapRecordEntity;
import com.org.relaytiming.entities.RunnerEntity;
import com.org.relaytiming.repositories.LapRecordRepository;
import com.org.relaytiming.repositories.RunnerRepository;
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
@RequestMapping("/api/lap-records")
public class LapRecordController {

	private final LapRecordRepository lapRecordRepository;
	private final RunnerRepository runnerRepository;

	public LapRecordController(LapRecordRepository lapRecordRepository, RunnerRepository runnerRepository) {
		this.lapRecordRepository = lapRecordRepository;
		this.runnerRepository = runnerRepository;
	}

	@PostMapping
	public ResponseEntity<?> createLapRecord(@RequestBody LapRecordDTO request) {
		RunnerEntity runner = runnerRepository.findById(request.runnerId())
			.orElse(null);

		if (runner == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("No runner exists with id: " + request.runnerId());
		}

		LapRecordEntity lapRecord = new LapRecordEntity(
			runner,
			request.teamLapNumber(),
			request.runnerLapNumber(),
			request.lapTime(),
			request.timestamp()
		);

		LapRecordEntity savedLapRecord = lapRecordRepository.save(lapRecord);
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(savedLapRecord));
	}

	@GetMapping
	public List<LapRecordDTO> getLapRecords() {
		return lapRecordRepository.findAll().stream()
			.map(this::toDto)
			.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<LapRecordDTO> getLapRecord(@PathVariable Long id) {
		return lapRecordRepository.findById(id)
			.map(lapRecord -> ResponseEntity.ok(toDto(lapRecord)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private LapRecordDTO toDto(LapRecordEntity lapRecord) {
		return new LapRecordDTO(
			lapRecord.getId(),
			lapRecord.getRunner().getId(),
			lapRecord.getTeamLapNumber(),
			lapRecord.getRunnerLapNumber(),
			lapRecord.getLapTime(),
			lapRecord.getTimestamp()
		);
	}
}