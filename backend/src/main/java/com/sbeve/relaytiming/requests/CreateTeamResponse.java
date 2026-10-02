package com.sbeve.relaytiming.requests;

public record CreateTeamResponse(
        Long id,
        String name,
        String division,
        String epcHex) {
}
