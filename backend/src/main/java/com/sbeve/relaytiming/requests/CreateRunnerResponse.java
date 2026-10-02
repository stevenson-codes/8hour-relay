package com.sbeve.relaytiming.requests;

public record CreateRunnerResponse(
        Long id,
        String name,
        Integer leg,
        String bib,
        String sex,
        String status,
        Long teamId) {
}
