package com.caretta.proje.yuvatakip.dto;

import com.caretta.proje.yuvatakip.entity.Mevsim;
import com.caretta.proje.yuvatakip.entity.YuvaDurumu;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record YuvaKaydiResponse(
        Long id,
        Double latitude,
        Double longitude,
        LocalDate tarih,
        YuvaDurumu durum,
        String notlar,
        LocalDateTime createdAt,
        Mevsim mevsim
) {
}
