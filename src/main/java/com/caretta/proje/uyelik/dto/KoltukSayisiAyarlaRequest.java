package com.caretta.proje.uyelik.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DEMO/SUNUM ARACI - bkz. AdminUyelikController#koltukSayisiAyarla. Stripe odeme
 * akisini BYPASS edip koltuk sayisini dogrudan ayarlamak icindir.
 */
public record KoltukSayisiAyarlaRequest(
        @Min(1) @Max(1000) int satinAlinanKoltukSayisi
) {
}
