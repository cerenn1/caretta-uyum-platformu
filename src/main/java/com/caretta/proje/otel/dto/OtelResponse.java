package com.caretta.proje.otel.dto;

public record OtelResponse(
        Long id,
        String ad,
        Double latitude,
        Double longitude
) {
}
