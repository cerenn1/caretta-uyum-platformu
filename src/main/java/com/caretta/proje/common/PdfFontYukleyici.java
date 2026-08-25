package com.caretta.proje.common;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classpath'teki bir TTF font dosyasini OpenPDF'in {@link BaseFont} nesnesine
 * yukleyen paylasilan yardimci sinif. Hem UyumRaporuPdfOlusturucu hem
 * KatkiSertifikasiPdfOlusturucu (ve ileride PDF ureten baska her sinif) AYNI
 * deseni kullanir - bu yuzden buraya, ortak bir yere cikarildi.
 */
public final class PdfFontYukleyici {

    private PdfFontYukleyici() {
    }

    /**
     * Turkce karakterler (s/g/i/I/c/o/u ve buyuk/kucuk halleri) PDF'in varsayilan
     * fontlarinda bozuk gorunur. Bu yuzden fontu IDENTITY_H kodlamasi ve EMBEDDED=true
     * ile, dosyanin kendi baytlarini PDF icine gomerek yukluyoruz - boylece uretilen
     * belge, hedef makinede DejaVuSans kurulu olmasa bile dogru gorunur.
     *
     * Font, classpath'ten (getResourceAsStream) byte dizisi olarak okunur; dosya
     * yoluna guvenilmez, boylece uygulama jar icinde (Docker container'inda) calisirken de
     * calisir.
     */
    public static BaseFont classpathTtfYukle(String classpathYolu) throws IOException, DocumentException {
        try (InputStream in = PdfFontYukleyici.class.getResourceAsStream(classpathYolu)) {
            if (in == null) {
                throw new IOException("Font dosyasi classpath'te bulunamadi: " + classpathYolu);
            }
            byte[] fontBaytlari = in.readAllBytes();
            String dosyaAdi = classpathYolu.substring(classpathYolu.lastIndexOf('/') + 1);
            return BaseFont.createFont(dosyaAdi, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, fontBaytlari, null);
        }
    }
}
