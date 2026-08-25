package com.caretta.proje.puansistemi.service;

import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gercek OpenPDF cagrilarini (mock YOK) calistiran birim testi. Amac: fontlarin
 * classpath'ten dogru yuklendigini ve uretilen ciktinin gercekten gecerli bir PDF
 * oldugunu (ilk baytlarin "%PDF" imzasi tasidigini) dogrulamak - UyumRaporuPdfOlusturucuTest
 * ile AYNI desen.
 */
class KatkiSertifikasiPdfOlusturucuTest {

    private static final byte[] PDF_IMZASI = {'%', 'P', 'D', 'F'};

    private KatkiSertifikasiPdfOlusturucu olusturucu;

    @BeforeEach
    void setUp() {
        olusturucu = new KatkiSertifikasiPdfOlusturucu();
        olusturucu.fontlariYukle();
    }

    @Test
    void olustur_RozetVarken_PDFImzasiTasiyanGecerliBirCiktiUretir() throws IOException {
        byte[] pdf = olusturucu.olustur("test@example.com", 8L, 70L, "BRONZ");

        assertThat(pdf).isNotEmpty();
        assertThat(Arrays.copyOf(pdf, 4)).isEqualTo(PDF_IMZASI);

        try (PdfReader okuyucu = new PdfReader(pdf)) {
            assertThat(okuyucu.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void olustur_RozetYokkenDeCokmedenGecerliPdfUretir() throws IOException {
        byte[] pdf = olusturucu.olustur("henuz-rozetsiz@example.com", 2L, 20L, null);

        assertThat(pdf).isNotEmpty();
        assertThat(Arrays.copyOf(pdf, 4)).isEqualTo(PDF_IMZASI);

        try (PdfReader okuyucu = new PdfReader(pdf)) {
            assertThat(okuyucu.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void olustur_TurkceKarakterIcerenEmailIleCokmedenGecerliPdfUretir() {
        byte[] pdf = olusturucu.olustur("gönüllü.çalışan@example.com", 55L, 550L, "ALTIN");

        assertThat(pdf).isNotEmpty();
        assertThat(Arrays.copyOf(pdf, 4)).isEqualTo(PDF_IMZASI);
    }
}
