package com.caretta.proje.otel.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record KapanisKanitiResponse(
        Long id,
        Long otelId,
        LocalDate tarih,
        String fotografYolu,
        LocalDateTime olusturulmaZamani
) {
}
