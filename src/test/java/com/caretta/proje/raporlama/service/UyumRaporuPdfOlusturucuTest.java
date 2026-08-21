package com.caretta.proje.raporlama.service;

import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.raporlama.dto.UyumRaporuVerisi;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gercek OpenPDF cagrilarini (mock YOK) calistiran birim testi. Amac: fontlarin
 * classpath'ten dogru yuklendigini ve uretilen ciktinin gercekten gecerli bir PDF
 * oldugunu (ilk baytlarin "%PDF" imzasi tasidigini) dogrulamak.
 */
class UyumRaporuPdfOlusturucuTest {

    private static final byte[] PDF_IMZASI = {'%', 'P', 'D', 'F'};

    private UyumRaporuPdfOlusturucu olusturucu;
    private Otel otel;

    @BeforeEach
    void setUp() {
        olusturucu = new UyumRaporuPdfOlusturucu();
        olusturucu.fontlariYukle();

        otel = Otel.builder().id(1L).ad("Test Oteli").latitude(36.85).longitude(30.7).build();
    }

    @Test
    void olustur_GecerliVeriIle_PDFImzasiTasiyanGecerliBirCiktiUretir() {
        LocalDate bitis = LocalDate.of(2026, 6, 15);
        LocalDate baslangic = bitis.minusDays(29);

        UyumRaporuVerisi veri = new UyumRaporuVerisi(
                otel,
                baslangic,
                bitis,
                30,
                3,
                10.0,
                List.of(bitis, bitis.minusDays(5), bitis.minusDays(10)),
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 6, 15, 10, 30)
        );

        byte[] pdf = olusturucu.olustur(veri);

        assertThat(pdf).isNotEmpty();
        assertThat(Arrays.copyOf(pdf, 4)).isEqualTo(PDF_IMZASI);
    }

    @Test
    void olustur_HicKanitliTarihOlmayanDonemIcinCokmedenGecerliPdfUretir() {
        LocalDate bitis = LocalDate.of(2026, 6, 15);
        LocalDate baslangic = bitis.minusDays(29);

        UyumRaporuVerisi veri = new UyumRaporuVerisi(
                otel,
                baslangic,
                bitis,
                30,
                0,
                0.0,
                List.of(),
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 6, 15, 10, 30)
        );

        byte[] pdf = olusturucu.olustur(veri);

        assertThat(pdf).isNotEmpty();
        assertThat(Arrays.copyOf(pdf, 4)).isEqualTo(PDF_IMZASI);
    }

    @Test
    void olustur_30GunlukIkiAyaYayilanDonemIcinTekSayfayaSigar() throws IOException {
        // 17.05.2026 - 15.06.2026: 30 gunluk varsayilan donem, iki ayrik ay (Mayis +
        // Haziran) icerir - takvim izgarasi acisindan en zorlu 30 gunluk senaryo.
        LocalDate bitis = LocalDate.of(2026, 6, 15);
        LocalDate baslangic = bitis.minusDays(29);

        UyumRaporuVerisi veri = new UyumRaporuVerisi(
                otel,
                baslangic,
                bitis,
                30,
                2,
                6.7,
                List.of(bitis, bitis.minusDays(20)),
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 6, 15, 10, 30)
        );

        byte[] pdf = olusturucu.olustur(veri);

        try (PdfReader okuyucu = new PdfReader(pdf)) {
            assertThat(okuyucu.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void olustur_365GunlukUzunBirDonemIcinCokmedenCokSayfaliPdfUretir() {
        LocalDate bitis = LocalDate.of(2026, 6, 15);
        LocalDate baslangic = bitis.minusDays(364);

        UyumRaporuVerisi veri = new UyumRaporuVerisi(
                otel,
                baslangic,
                bitis,
                365,
                0,
                0.0,
                List.of(),
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 6, 15, 10, 30)
        );

        byte[] pdf = olusturucu.olustur(veri);

        assertThat(pdf).isNotEmpty();
        assertThat(Arrays.copyOf(pdf, 4)).isEqualTo(PDF_IMZASI);
        // 365 satirlik bir tablo iceren PDF, kucuk bir ozet raporundan gozle gorulur
        // sekilde daha buyuk olmali - kabaca bir "sayfalama gercekten oldu mu" kontrolu.
        assertThat(pdf.length).isGreaterThan(4000);
    }

    // --- gunAraliklariniGrupla: eksik gun listesini ardisik araliklara gruplayan
    // saf fonksiyonun testleri. OpenPDF/PDF uretimiyle ilgisi yok, bu yuzden PDF
    // olusturmadan dogrudan cagrilabiliyor. ---

    @Test
    void gunAraliklariniGrupla_ArdisikGunler_TekAralikDoner() {
        List<LocalDate> gunler = List.of(
                LocalDate.of(2026, 7, 22),
                LocalDate.of(2026, 7, 23),
                LocalDate.of(2026, 7, 24));

        List<UyumRaporuPdfOlusturucu.GunAraligi> araliklar =
                UyumRaporuPdfOlusturucu.gunAraliklariniGrupla(gunler);

        assertThat(araliklar).containsExactly(
                new UyumRaporuPdfOlusturucu.GunAraligi(
                        LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 24)));
    }

    @Test
    void gunAraliklariniGrupla_KesintiliGunler_AyriAraliklarDoner() {
        List<LocalDate> gunler = List.of(
                LocalDate.of(2026, 7, 22),
                LocalDate.of(2026, 7, 25),
                LocalDate.of(2026, 7, 26));

        List<UyumRaporuPdfOlusturucu.GunAraligi> araliklar =
                UyumRaporuPdfOlusturucu.gunAraliklariniGrupla(gunler);

        assertThat(araliklar).containsExactly(
                new UyumRaporuPdfOlusturucu.GunAraligi(
                        LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 22)),
                new UyumRaporuPdfOlusturucu.GunAraligi(
                        LocalDate.of(2026, 7, 25), LocalDate.of(2026, 7, 26)));
    }

    @Test
    void gunAraliklariniGrupla_TekGun_BaslangicBitisAyniOlanTekAralikDoner() {
        List<LocalDate> gunler = List.of(LocalDate.of(2026, 7, 22));

        List<UyumRaporuPdfOlusturucu.GunAraligi> araliklar =
                UyumRaporuPdfOlusturucu.gunAraliklariniGrupla(gunler);

        assertThat(araliklar).containsExactly(
                new UyumRaporuPdfOlusturucu.GunAraligi(
                        LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 22)));
    }

    @Test
    void gunAraliklariniGrupla_HicEksikGunYok_BosListeDoner() {
        List<UyumRaporuPdfOlusturucu.GunAraligi> araliklar =
                UyumRaporuPdfOlusturucu.gunAraliklariniGrupla(List.of());

        assertThat(araliklar).isEmpty();
    }

    @Test
    void gunAraliklariniGrupla_TumDonemEksik_DonemBastanSonaTekAralikDoner() {
        LocalDate baslangic = LocalDate.of(2026, 7, 1);
        List<LocalDate> gunler = baslangic.datesUntil(LocalDate.of(2026, 7, 29)).toList(); // 1-28 Temmuz

        List<UyumRaporuPdfOlusturucu.GunAraligi> araliklar =
                UyumRaporuPdfOlusturucu.gunAraliklariniGrupla(gunler);

        assertThat(araliklar).containsExactly(
                new UyumRaporuPdfOlusturucu.GunAraligi(
                        LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 28)));
    }

    @Test
    void gunAraliklariniGrupla_SiraliOlmayanVeTekrarliTarihler_YineDeDogruGruplar() {
        // Fonksiyon saf oldugu ve kendi icinde sort+distinct uyguladigi icin,
        // caginin siralamasi/olasi tekrarlar sonucu etkilememeli.
        List<LocalDate> gunler = List.of(
                LocalDate.of(2026, 7, 24),
                LocalDate.of(2026, 7, 22),
                LocalDate.of(2026, 7, 23),
                LocalDate.of(2026, 7, 22));

        List<UyumRaporuPdfOlusturucu.GunAraligi> araliklar =
                UyumRaporuPdfOlusturucu.gunAraliklariniGrupla(gunler);

        assertThat(araliklar).containsExactly(
                new UyumRaporuPdfOlusturucu.GunAraligi(
                        LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 24)));
    }
}
