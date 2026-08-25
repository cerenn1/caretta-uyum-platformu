package com.caretta.proje.puansistemi.service;

import com.caretta.proje.common.PdfFontYukleyici;
import com.caretta.proje.common.ZamanDilimi;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Herhangi bir rolun (KULLANICI/OTEL_CALISANI farki yok) kendi gonullu katkisini
 * belgeleyen, TEK sayfalik basit bir PDF uretir ({@code GET /api/katki-sertifikasi}).
 *
 * UyumRaporuPdfOlusturucu kadar karmasik degildir: takvim izgarasi, "Sayfa X / Y"
 * altbilgisi (page event) yoktur - icerik her zaman tek sayfaya sigacak kadar kisadir.
 * Font yukleme deseni (IDENTITY_H + EMBEDDED, Turkce karakterlerin bozuk gorunmemesi
 * icin) UyumRaporuPdfOlusturucu ile AYNI ve ortak {@link PdfFontYukleyici} uzerinden
 * paylasilir.
 */
@Service
public class KatkiSertifikasiPdfOlusturucu {

    private static final DateTimeFormatter TARIH_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Mobil uygulamanin paletiyle uyumlu deniz mavisi - UyumRaporuPdfOlusturucu'daki
    // baslik bandiyla ayni renk, gorsel tutarlilik icin.
    private static final Color RENK_BASLIK_BANDI = new Color(0x01, 0x57, 0x9B);

    // Font dosyalarini her istekte classpath'ten yeniden okuyup parse etmek pahali bir
    // islem. Bu sinif Spring'de singleton (@Service) oldugu icin fontlar uygulama
    // acilirken sadece BIR KEZ yuklenir (@PostConstruct).
    private Font baslikFontu;
    private Font normalFontu;
    private Font kalinFontu;

    @PostConstruct
    void fontlariYukle() {
        try {
            BaseFont normal = PdfFontYukleyici.classpathTtfYukle("/fonts/DejaVuSans.ttf");
            BaseFont kalin = PdfFontYukleyici.classpathTtfYukle("/fonts/DejaVuSans-Bold.ttf");

            baslikFontu = new Font(kalin, 18, Font.BOLD, Color.WHITE);
            normalFontu = new Font(normal, 11, Font.NORMAL);
            kalinFontu = new Font(kalin, 11, Font.BOLD);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Sertifika fontlari yuklenemedi", e);
        }
    }

    /**
     * @param email            sertifikayi alan kullanicinin e-postasi (ad/soyad alani
     *                         projede hicbir yerde yok, e-posta yeterli kimlik bilgisidir)
     * @param yuvaKayitToplam  kullanicinin toplam yuva/gozlem kayit sayisi
     * @param toplamPuan       kullanicinin toplam puani
     * @param rozet            mevcut rozet seviyesinin adi (BRONZ/GUMUS/ALTIN) ya da
     *                         henuz bir rozete ulasilmadiysa null
     */
    public byte[] olustur(String email, long yuvaKayitToplam, long toplamPuan, String rozet) {
        ByteArrayOutputStream cikti = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 48, 48, 60, 60);

        try {
            PdfWriter.getInstance(document, cikti);
            document.open();

            document.add(baslikBandiOlustur());

            Paragraph bosluk = new Paragraph(" ", normalFontu);
            bosluk.setSpacingAfter(14f);
            document.add(bosluk);

            Paragraph ozet = new Paragraph(
                    "Bu belge, " + email + " kullanıcısının Kaplumbağa Yuvalama Bölgeleri için Kıyı Turizmi "
                            + "Sürdürülebilirlik Uyum Platformu'na sağladığı gönüllü katkıyı belgeler.",
                    normalFontu);
            ozet.setAlignment(Element.ALIGN_JUSTIFIED);
            ozet.setSpacingAfter(18f);
            document.add(ozet);

            document.add(bilgiSatiri("Toplam Yuva/Gözlem Kaydı", String.valueOf(yuvaKayitToplam)));
            document.add(bilgiSatiri("Toplam Puan", String.valueOf(toplamPuan)));
            document.add(bilgiSatiri("Mevcut Rozet Seviyesi",
                    rozet == null ? "Henüz bir rozet seviyesine ulaşılmadı" : rozet));

            Paragraph tarihSatiri = new Paragraph(
                    "Belge Tarihi: " + LocalDate.now(ZamanDilimi.TURKIYE).format(TARIH_FORMAT), normalFontu);
            tarihSatiri.setSpacingBefore(24f);
            document.add(tarihSatiri);
        } catch (DocumentException e) {
            throw new IllegalStateException("Katki sertifikasi PDF olusturulamadi", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return cikti.toByteArray();
    }

    private PdfPTable baslikBandiOlustur() {
        PdfPTable bant = new PdfPTable(1);
        bant.setWidthPercentage(100);

        PdfPCell hucre = new PdfPCell(new Phrase("Gönüllü Katkı Sertifikası", baslikFontu));
        hucre.setBackgroundColor(RENK_BASLIK_BANDI);
        hucre.setHorizontalAlignment(Element.ALIGN_CENTER);
        hucre.setVerticalAlignment(Element.ALIGN_MIDDLE);
        hucre.setPadding(16f);
        hucre.setBorder(Rectangle.NO_BORDER);
        bant.addCell(hucre);

        return bant;
    }

    private Paragraph bilgiSatiri(String etiket, String deger) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiket + ": ", kalinFontu));
        p.add(new Chunk(deger, normalFontu));
        p.setSpacingAfter(8f);
        return p;
    }
}
