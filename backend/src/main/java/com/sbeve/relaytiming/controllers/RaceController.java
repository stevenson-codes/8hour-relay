package com.sbeve.relaytiming.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbeve.relaytiming.dto.RaceStatusDto;
import com.sbeve.relaytiming.services.RaceStateService;

@RestController
@RequestMapping("/api/race")
public class RaceController {
    private final RaceStateService raceStateService;

    public RaceController(RaceStateService raceStateService) {
        this.raceStateService = raceStateService;
    }

    @GetMapping("/status")
    public RaceStatusDto getStatus() {
        return new RaceStatusDto(raceStateService.isActive());
    }

    @PostMapping("/start")
    public RaceStatusDto start() {
        raceStateService.start();
        return new RaceStatusDto(raceStateService.isActive());
    }

    @PostMapping("/stop")
    public RaceStatusDto stop() {
        raceStateService.stop();
        return new RaceStatusDto(raceStateService.isActive());
    }
}
