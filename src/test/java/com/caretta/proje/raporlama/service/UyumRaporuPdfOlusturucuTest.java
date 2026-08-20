package com.caretta.proje.raporlama.service;

import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.raporlama.dto.UyumRaporuVerisi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
