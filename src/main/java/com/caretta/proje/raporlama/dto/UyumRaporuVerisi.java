package com.caretta.proje.raporlama.dto;

import com.caretta.proje.otel.entity.Otel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PDF raporu olusturulurken servis katmani ile PDF olusturucu arasinda tasinan veri.
 * Bu bir API cevabi DEGILDIR, disari donmez; sadece dahili bir tasiyici (transfer) nesnesidir.
 */
public record UyumRaporuVerisi(
        Otel otel,
        LocalDate donemBaslangic,
        LocalDate donemBitis,
        long donemGunSayisi,
        long kanitliGunSayisi,
        double uyumOrani,
        List<LocalDate> kanitliTarihler,
        String raporNo,
        LocalDateTime uretimZamani
) {
}
