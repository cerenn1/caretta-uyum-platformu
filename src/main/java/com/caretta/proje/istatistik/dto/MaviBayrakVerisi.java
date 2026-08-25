package com.caretta.proje.istatistik.dto;

import java.util.Map;

/**
 * Il bazinda 2026 Mavi Bayrak plaj sayisi - SABIT/STATIK veri.
 *
 * DIKKAT: Bu, {@link ResmiKumsal} listesinden (yuvalama kumsallari) TAMAMEN
 * AYRI, farkli bir kaynaktan gelen ve farkli bir seyi olcen bir veri kumesidir
 * - IKISI KARISTIRILMAMALIDIR. Mavi Bayrak, sahil temizligi/altyapisi/su
 * kalitesi standardidir; bir ilin Mavi Bayrak sayisi, o ildeki yuvalama
 * kumsali koruma statusuyle birebir orantili degildir ve onunla dogrudan
 * kiyaslanmamalidir.
 *
 * KAYNAK: FEE (Foundation for Environmental Education) 2026 verisi - haber
 * kaynaklarindan derlenmistir; resmi teyit icin FEE / Turkiye Cevre Egitim
 * Vakfi'nin (TURCEV) guncel yayinina bakilmalidir.
 */
public final class MaviBayrakVerisi {

    public static final int YIL = 2026;

    public static final String KAYNAK =
            "FEE (Foundation for Environmental Education) 2026 verisi - haber "
                    + "kaynaklarindan derlenmistir, resmi teyit icin FEE / Turkiye Cevre "
                    + "Egitim Vakfi (TURCEV) guncel yayinina bakilmalidir.";

    public static final Map<String, Integer> IL_BAZINDA_SAYI = Map.of(
            "Antalya", 234,
            "Muğla", 114,
            "Mersin", 12
    );

    private MaviBayrakVerisi() {
    }
}
