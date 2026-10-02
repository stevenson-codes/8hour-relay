package com.sbeve.relaytiming.requests;

public record CreateRunnerRequest(
        String name,
        Integer leg,
        String bib,
        String sex,
        String epcHex) {
}
