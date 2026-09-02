package com.caretta.proje.uyelik.dto;

/**
 * DEMO/SUNUM ARACI - bkz. AdminUyelikController#kapanisKanitiDoldur.
 */
public record KapanisKanitiDoldurmaResponse(
        int eklenenGunSayisi,
        int atlananGunSayisi
) {
}
