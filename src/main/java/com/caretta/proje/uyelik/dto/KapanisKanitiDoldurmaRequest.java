package com.caretta.proje.uyelik.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DEMO/SUNUM ARACI - bkz. AdminUyelikController#kapanisKanitiDoldur. Bugunden
 * geriye kac gunun demo kapanis kaniti kaydiyla doldurulacagini belirtir.
 */
public record KapanisKanitiDoldurmaRequest(
        @Min(1) @Max(90) int gunSayisi
) {
}
