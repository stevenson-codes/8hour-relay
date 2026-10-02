package com.sbeve.relaytiming.requests;

import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        String division,
        String epcHex,
        List<RunnerResponse> runners) {
}
