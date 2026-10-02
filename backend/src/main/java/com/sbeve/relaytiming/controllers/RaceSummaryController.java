package com.sbeve.relaytiming.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbeve.relaytiming.dto.TeamSummaryDto;
import com.sbeve.relaytiming.services.RaceSummaryService;

@RestController
@RequestMapping("/api/summary")
public class RaceSummaryController {
    private final RaceSummaryService raceSummaryService;

    public RaceSummaryController(RaceSummaryService raceSummaryService) {
        this.raceSummaryService = raceSummaryService;
    }

    @GetMapping
    public List<TeamSummaryDto> getSummary() {
        return raceSummaryService.getSummary();
    }
}
