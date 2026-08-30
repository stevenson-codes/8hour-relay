package com.sbeve.relaytiming.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbeve.relaytiming.services.LapRecordService;
import com.sbeve.relaytiming.services.RaceStateService;

@RestController
@RequestMapping("/api/lap-records")
public class LapRecordController {
    private final LapRecordService lapRecordService;
    private final RaceStateService raceStateService;

    public LapRecordController(LapRecordService lapRecordService, RaceStateService raceStateService) {
        this.lapRecordService = lapRecordService;
        this.raceStateService = raceStateService;
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAll() {
        if (raceStateService.isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        lapRecordService.clearAllLapRecords();
        return ResponseEntity.noContent().build();
    }
}
