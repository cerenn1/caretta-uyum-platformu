package com.caretta.proje.panel.dto;

import java.time.LocalDate;

/**
 * Mobil ana panel icin tek ozet cevabi. Kullanicidan alinan hicbir id yoktur;
 * tum alanlar token'daki kullanicidan turetilir (bkz. PanelOzetiService).
 *
 * otelId/otelAdi/uyumOrani ve donem alanlari SADECE OTEL_CALISANI rolu icin
 * doldurulur; diger rollerde hepsi null doner. Boylece bu DTO'yu tuketen mobil
 * taraf, alanlar null ise otel kartini hic gostermez.
 */
public record PanelOzetiResponse(
        String email,
        String role,
        long yuvaKayitToplam,
        LocalDate sonYuvaKaydiTarih,
        String sonYuvaKaydiDurum,

        Long otelId,
        String otelAdi,
        Double uyumOrani,
        LocalDate donemBaslangic,
        LocalDate donemBitis,
        Long donemGunSayisi,
        Long kanitYuklenenGunSayisi,
        Boolean bugunKanitYuklendiMi,

        // Puan/rozet TUM roller icin doldurulur - otel/uyum alanlarinin aksine
        // role bagli degil, cunku her kullanici yuva kaydi ekleyebilir.
        Long toplamPuan,
        String rozet
) {
}
