package com.caretta.proje.yuvatakip.dto;

import com.caretta.proje.yuvatakip.entity.YuvaDurumu;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record YuvaKaydiRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull LocalDate tarih,
        @NotNull YuvaDurumu durum,
        String notlar
) {
}
