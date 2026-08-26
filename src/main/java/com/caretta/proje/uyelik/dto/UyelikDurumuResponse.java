package com.caretta.proje.uyelik.dto;

import com.caretta.proje.uyelik.entity.UyelikDurumu;

public record UyelikDurumuResponse(
        Long otelId,
        Integer satinAlinanKoltukSayisi,
        long kullanilanKoltukSayisi,
        UyelikDurumu uyelikDurumu,
        boolean premiumMu
) {
}
