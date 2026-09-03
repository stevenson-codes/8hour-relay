package com.sbeve.relaytiming.requests;

public record CreateTeamRequest(
        String name,
        String division,
        String epcHex) {
}
