package com.caretta.proje.istatistik.dto;

import java.util.List;
import java.util.Map;

/**
 * "Etkimiz / Kapsam Alanimiz" bolumu icin herkese acik (permitAll), kisisel
 * veri icermeyen cevap. Uc AYRI veri kumesini bir arada dondurur - bunlar
 * birbirine KARISTIRILMAMALI:
 *
 * <ol>
 *   <li>{@code resmiKorumaAltindakiKumsallar} - Turkiye'nin resmi olarak
 *       koruma altindaki 21 yuvalama kumsalinin SABIT/STATIK listesi.</li>
 *   <li>{@code maviBayrakSayilari} - ayni illerdeki 2026 Mavi Bayrak plaj
 *       sayisi; yuvalama kumsallarindan BAGIMSIZ, FARKLI bir kaynaktan
 *       gelen ayri bir istatistik.</li>
 *   <li>{@code platformKayitBolgeleri} - platformun kendi canli
 *       istatistigi; yuva kayitlarinin en yakin resmi kumsala gore
 *       OTOMATIK (elle girilmeyen) dagilimi, k-anonimlik uygulanmis
 *       haliyle.</li>
 * </ol>
 */
public record KapsamAlaniResponse(
        List<ResmiKumsalIlGrubu> resmiKorumaAltindakiKumsallar,
        String resmiVeriKaynagi,
        Map<String, Integer> maviBayrakSayilari,
        int maviBayrakYili,
        String maviBayrakKaynagi,
        long platformToplamYuvaKaydiSayisi,
        long platformAktifOtelSayisi,
        List<BolgeKaydiSayisi> platformKayitBolgeleri
) {
}
