package com.caretta.proje.otel.dto;

import java.time.LocalDate;

public record UyumOraniResponse(
        Long otelId,
        String otelAdi,
        LocalDate donemBaslangic,
        LocalDate donemBitis,
        long donemGunSayisi,
        long kanitYuklenenGunSayisi,
        double uyumOrani
) {
}
