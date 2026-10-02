package com.sbeve.relaytiming.requests;

public record RunnerResponse(
        Long id,
        String name,
        Integer leg,
        String bib,
        String sex,
        String epcHex,
        String status,
        Long teamId) {
}
