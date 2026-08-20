package com.caretta.proje.raporlama.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.common.ZamanDilimi;
import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.service.KapanisKanitiService;
import com.caretta.proje.otel.service.OtelService;
import com.caretta.proje.raporlama.dto.UyumRaporuSonucu;
import com.caretta.proje.raporlama.dto.UyumRaporuVerisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Denetime sunulabilir uyum raporu (PDF) uretiminin ana orkestrasyon servisi.
 *
 * Onemli: uyum orani hesabi burada TEKRAR YAZILMAZ. Sayisal hesaplama, /uyum-orani
 * endpoint'inin de kullandigi {@link KapanisKanitiService#donemUyumOraniHesapla}
 * metoduna delege edilir; boylece rapor ve endpoint her zaman ayni sonucu verir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UyumRaporuService {

    private static final long MAKS_DONEM_GUN_SAYISI = 365;

    private final KapanisKanitiService kapanisKanitiService;
    private final OtelService otelService;
    private final UyumRaporuPdfOlusturucu pdfOlusturucu;

    public UyumRaporuSonucu raporUret(Long otelId, LocalDate baslangicParam, LocalDate bitisParam, User currentUser) {
        // Yetkilendirme /uyum-orani ile BIREBIR ayni desen: kullanici sadece kendi
        // otelinin raporunu isteyebilir. Kontrol, otelin var olup olmadigina BAKMADAN
        // once yapilir - aksi halde "otel yok -> 404" / "baska otel -> 403" farki,
        // saldirgana hangi otelId'lerin gercekten var oldugunu sizdirir.
        kapanisKanitiService.otelErisimYetkisiDogrula(otelId, currentUser);

        TarihAraligi donem = donemCozumle(baslangicParam, bitisParam);

        Otel otel = otelService.getEntity(otelId);
        UyumOraniResponse uyumOrani = kapanisKanitiService.donemUyumOraniHesapla(
                otelId, donem.baslangic(), donem.bitis());
        List<LocalDate> kanitliTarihler = kapanisKanitiService.kanitliTarihleriGetir(
                otelId, donem.baslangic(), donem.bitis());

        String raporNo = UUID.randomUUID().toString();
        // Rapor uretim zaman damgasi da Turkiye saatinde: bkz. ZamanDilimi sinifi.
        LocalDateTime uretimZamani = LocalDateTime.now(ZamanDilimi.TURKIYE);

        // Izlenebilirlik icin: hangi raporun, hangi otel/donem/kullanici tarafindan
        // istendigi sunucu loguna yazilir. Rapor no veritabanina KAYDEDILMEZ.
        log.info("Uyum raporu uretildi: raporNo={}, otelId={}, donem={}..{}, istekEdenKullanici={}",
                raporNo, otelId, donem.baslangic(), donem.bitis(), currentUser.getEmail());

        UyumRaporuVerisi veri = new UyumRaporuVerisi(
                otel,
                donem.baslangic(),
                donem.bitis(),
                uyumOrani.donemGunSayisi(),
                uyumOrani.kanitYuklenenGunSayisi(),
                uyumOrani.uyumOrani(),
                kanitliTarihler,
                raporNo,
                uretimZamani
        );

        byte[] pdfBaytlari = pdfOlusturucu.olustur(veri);
        return new UyumRaporuSonucu(pdfBaytlari, donem.baslangic(), donem.bitis());
    }

    /**
     * baslangic/bitis parametrelerini dogrular ve nihai tarih araligina cozumler.
     * Ikisi de bos ise varsayilan "son 30 gun" (uyum orani endpoint'iyle ayni tanim)
     * kullanilir.
     */
    private TarihAraligi donemCozumle(LocalDate baslangic, LocalDate bitis) {
        if ((baslangic == null) != (bitis == null)) {
            throw new GecersizIstekException(
                    "baslangic ve bitis parametreleri birlikte verilmeli veya ikisi de bos birakilmalidir");
        }

        // Bugun her zaman Turkiye saatine gore hesaplanir, sunucunun sistem saat
        // dilimine GUVENILMEZ (bkz. ZamanDilimi sinifi).
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        if (baslangic == null) {
            LocalDate varsayilanBaslangic = bugun.minusDays(KapanisKanitiService.UYUM_ORANI_DONEM_GUN_SAYISI - 1);
            return new TarihAraligi(varsayilanBaslangic, bugun);
        }

        if (bitis.isBefore(baslangic)) {
            throw new GecersizIstekException("bitis tarihi baslangic tarihinden once olamaz");
        }
        if (bitis.isAfter(bugun)) {
            throw new GecersizIstekException("bitis tarihi gelecekte olamaz");
        }

        long gunSayisi = ChronoUnit.DAYS.between(baslangic, bitis) + 1;
        if (gunSayisi > MAKS_DONEM_GUN_SAYISI) {
            throw new GecersizIstekException("Rapor araligi en fazla " + MAKS_DONEM_GUN_SAYISI + " gun olabilir");
        }

        return new TarihAraligi(baslangic, bitis);
    }

    private record TarihAraligi(LocalDate baslangic, LocalDate bitis) {
    }
}
