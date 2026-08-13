package com.caretta.proje.otel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtelRequest(
        @NotBlank String ad,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
