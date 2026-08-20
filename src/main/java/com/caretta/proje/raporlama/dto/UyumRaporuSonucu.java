package com.caretta.proje.raporlama.dto;

import java.time.LocalDate;

/**
 * Uyum raporu servisinin controller'a dondugu sonuc: uretilen PDF baytlari ve
 * (parametre verilmediyse varsayilan olarak cozumlenmis) donem tarihleri.
 *
 * Donem bitis tarihi, controller'daki dosya adi (Content-Disposition) icin gerekli;
 * bu yuzden sadece PDF baytini degil, cozumlenmis tarihleri de tasir. Boylece
 * varsayilan tarih hesaplama mantigi tek yerde (service) kalir, controller'da
 * tekrar edilmez.
 */
public record UyumRaporuSonucu(
        byte[] pdfBaytlari,
        LocalDate donemBaslangic,
        LocalDate donemBitis
) {
}
