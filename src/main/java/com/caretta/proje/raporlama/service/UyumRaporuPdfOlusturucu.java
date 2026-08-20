package com.caretta.proje.raporlama.service;

import com.caretta.proje.raporlama.dto.UyumRaporuVerisi;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Uyum raporunun gercek PDF baytlarini ureten sinif. Veritabanina erismez, sadece
 * kendisine hazir halde verilen {@link UyumRaporuVerisi} nesnesini PDF'e doker.
 *
 * NOT (fotograf gomme yok): Kapanis kaniti fotograflari bu rapora kasitli olarak
 * gomulmez. CLAUDE.md "Bilinen Riskler" bolum 1: yuklenen fotograflarin EXIF meta
 * verisi (GPS konumu, cihaz modeli) henuz temizlenmiyor. Fotograf rapora gomulseydi
 * bu veri, denetciye/uculcu tarafa giden bir belgeyle birlikte disari sizardi. O
 * yuzden rapor su an icin yalnizca tarih/oran verisi icerir.
 */
@Service
public class UyumRaporuPdfOlusturucu {

    private static final DateTimeFormatter TARIH_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TARIH_SAAT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Gorev tanimindaki metin BIREBIR (kelimesi kelimesine) korunmali, degistirilmemeli.
    private static final String DURUSTLUK_NOTU =
            "Bu rapor, otel personeli tarafından yüklenen kanıt fotoğraflarının kayıt sıklığına dayanır. "
                    + "Fotoğrafların içeriği bağımsız olarak doğrulanmamıştır. Bu belge resmi bir sertifika veya "
                    + "denetim raporu değildir; sertifikasyon başvurularında destekleyici belge olarak kullanılmak "
                    + "üzere hazırlanmıştır.";

    // Font dosyalarini her istekte classpath'ten yeniden okuyup parse etmek pahali bir islem.
    // Bu sinif Spring'de singleton (@Service) oldugu icin fontlar uygulama acilirken
    // sadece BIR KEZ yuklenir (@PostConstruct) ve butun istekler ayni Font nesnelerini kullanir.
    private Font baslikFontu;
    private Font altBaslikFontu;
    private Font normalFontu;
    private Font kalinFontu;
    private Font kucukFontu;
    private Font tabloBaslikFontu;
    private Font oranBuyukFontu;

    @PostConstruct
    void fontlariYukle() {
        try {
            BaseFont normal = classpathTtfYukle("/fonts/DejaVuSans.ttf");
            BaseFont kalin = classpathTtfYukle("/fonts/DejaVuSans-Bold.ttf");

            baslikFontu = new Font(kalin, 16, Font.BOLD);
            altBaslikFontu = new Font(kalin, 12, Font.BOLD);
            normalFontu = new Font(normal, 10, Font.NORMAL);
            kalinFontu = new Font(kalin, 10, Font.BOLD);
            kucukFontu = new Font(normal, 9, Font.NORMAL);
            tabloBaslikFontu = new Font(kalin, 9, Font.BOLD, Color.WHITE);
            oranBuyukFontu = new Font(kalin, 28, Font.BOLD);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Rapor fontlari yuklenemedi", e);
        }
    }

    /**
     * Turkce karakterler (s/g/i/I/c/o/u ve buyuk/kucuk halleri) PDF'in varsayilan
     * fontlarinda bozuk gorunur. Bu yuzden fontu IDENTITY_H kodlamasi ve EMBEDDED=true
     * ile, dosyanin kendi baytlarini PDF icine gomerek yukluyoruz - boylece rapor,
     * hedef makinede DejaVuSans kurulu olmasa bile dogru gorunur.
     *
     * Font, classpath'ten (getResourceAsStream) byte dizisi olarak okunur; dosya
     * yoluna guvenilmez, boylece uygulama jar icinde (Docker container'da) calisirken de
     * calisir.
     */
    private BaseFont classpathTtfYukle(String classpathYolu) throws IOException, DocumentException {
        try (InputStream in = getClass().getResourceAsStream(classpathYolu)) {
            if (in == null) {
                throw new IOException("Font dosyasi classpath'te bulunamadi: " + classpathYolu);
            }
            byte[] fontBaytlari = in.readAllBytes();
            String dosyaAdi = classpathYolu.substring(classpathYolu.lastIndexOf('/') + 1);
            return BaseFont.createFont(dosyaAdi, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, fontBaytlari, null);
        }
    }

    public byte[] olustur(UyumRaporuVerisi veri) {
        ByteArrayOutputStream cikti = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);

        try {
            PdfWriter.getInstance(document, cikti);
            document.open();

            List<GunDurumu> gunlukDurumlar = gunlukDurumlariHesapla(veri);

            baslikBolumuEkle(document, veri);
            ozetBolumuEkle(document, veri);
            detayTablosuEkle(document, gunlukDurumlar);
            eksikGunlerBolumuEkle(document, gunlukDurumlar);
            yasalBaglamBolumuEkle(document);
            durustlukNotuEkle(document);
        } catch (DocumentException e) {
            throw new IllegalStateException("PDF rapor olusturulamadi", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return cikti.toByteArray();
    }

    private void baslikBolumuEkle(Document document, UyumRaporuVerisi veri) throws DocumentException {
        Paragraph baslik = new Paragraph("Kaplumbağa Yuvalama Sahili Uyum Raporu", baslikFontu);
        baslik.setAlignment(Element.ALIGN_CENTER);
        baslik.setSpacingAfter(14f);
        document.add(baslik);

        document.add(bilgiSatiri("Otel", veri.otel().getAd()));
        document.add(bilgiSatiri("Konum (Enlem, Boylam)",
                veri.otel().getLatitude() + ", " + veri.otel().getLongitude()));
        document.add(bilgiSatiri("Rapor Dönemi",
                veri.donemBaslangic().format(TARIH_FORMAT) + " - " + veri.donemBitis().format(TARIH_FORMAT)));
        document.add(bilgiSatiri("Rapor Üretim Tarihi",
                veri.uretimZamani().format(TARIH_SAAT_FORMAT) + " (Europe/Istanbul)"));
        document.add(bilgiSatiri("Rapor No", veri.raporNo()));

        Paragraph bosluk = new Paragraph(" ", normalFontu);
        bosluk.setSpacingAfter(4f);
        document.add(bosluk);
    }

    private Paragraph bilgiSatiri(String etiket, String deger) {
        Phrase satir = new Phrase();
        satir.add(new Chunk(etiket + ": ", kalinFontu));
        satir.add(new Chunk(deger == null ? "-" : deger, normalFontu));
        Paragraph paragraf = new Paragraph(satir);
        paragraf.setSpacingAfter(2f);
        return paragraf;
    }

    private void ozetBolumuEkle(Document document, UyumRaporuVerisi veri) throws DocumentException {
        document.add(bolumBasligi("Özet"));

        Paragraph oran = new Paragraph(String.format("%%%.1f", veri.uyumOrani()), oranBuyukFontu);
        oran.setSpacingAfter(6f);
        document.add(oran);

        document.add(new Paragraph(
                "Kanıt yüklenen gün sayısı: " + veri.kanitliGunSayisi() + " / " + veri.donemGunSayisi(),
                normalFontu));

        Paragraph degerlendirme = new Paragraph(degerlendirmeCumlesi(veri.uyumOrani()), kalinFontu);
        degerlendirme.setSpacingBefore(6f);
        degerlendirme.setSpacingAfter(4f);
        document.add(degerlendirme);

        Paragraph esikNotu = new Paragraph(
                "Değerlendirme eşikleri: %90 ve üzeri \"Yüksek uyum\", %70-90 arası \"Kabul edilebilir uyum\", "
                        + "%70 altı \"İyileştirme gerekli\" olarak sınıflandırılır.",
                kucukFontu);
        esikNotu.setSpacingAfter(12f);
        document.add(esikNotu);
    }

    private String degerlendirmeCumlesi(double uyumOrani) {
        if (uyumOrani >= 90.0) {
            return "Değerlendirme: Yüksek uyum.";
        } else if (uyumOrani >= 70.0) {
            return "Değerlendirme: Kabul edilebilir uyum.";
        } else {
            return "Değerlendirme: İyileştirme gerekli.";
        }
    }

    private void detayTablosuEkle(Document document, List<GunDurumu> gunlukDurumlar) throws DocumentException {
        document.add(bolumBasligi("Gün Gün Döküm"));

        PdfPTable tablo = new PdfPTable(new float[]{1f, 1f});
        tablo.setWidthPercentage(60);
        // Basligin her sayfada tekrarlanmasi ve otomatik sayfalama icin: 365 gunluk bir
        // rapor tek sayfaya sigmaz, PdfPTable document.add() ile eklendiginde satirlari
        // kendiliginden bir sonraki sayfaya tasir.
        tablo.setHeaderRows(1);
        tablo.setSpacingAfter(12f);

        tablo.addCell(tabloBasligiHucresi("Tarih"));
        tablo.addCell(tabloBasligiHucresi("Durum"));

        for (GunDurumu gunDurumu : gunlukDurumlar) {
            tablo.addCell(tabloVerisiHucresi(gunDurumu.tarih().format(TARIH_FORMAT)));
            tablo.addCell(tabloVerisiHucresi(gunDurumu.kanitVar() ? "Kanıt var" : "Kanıt yok"));
        }

        document.add(tablo);
    }

    private PdfPCell tabloBasligiHucresi(String metin) {
        PdfPCell hucre = new PdfPCell(new Phrase(metin, tabloBaslikFontu));
        hucre.setBackgroundColor(new Color(0x2E, 0x7D, 0x32));
        hucre.setHorizontalAlignment(Element.ALIGN_CENTER);
        hucre.setPadding(5f);
        return hucre;
    }

    private PdfPCell tabloVerisiHucresi(String metin) {
        PdfPCell hucre = new PdfPCell(new Phrase(metin, normalFontu));
        hucre.setHorizontalAlignment(Element.ALIGN_CENTER);
        hucre.setPadding(4f);
        return hucre;
    }

    private void eksikGunlerBolumuEkle(Document document, List<GunDurumu> gunlukDurumlar) throws DocumentException {
        document.add(bolumBasligi("Eksik Günler"));

        List<String> eksikGunler = gunlukDurumlar.stream()
                .filter(g -> !g.kanitVar())
                .map(g -> g.tarih().format(TARIH_FORMAT))
                .toList();

        if (eksikGunler.isEmpty()) {
            Paragraph p = new Paragraph("Dönemde eksik gün bulunmamaktadır.", normalFontu);
            p.setSpacingAfter(12f);
            document.add(p);
            return;
        }

        Paragraph p = new Paragraph(String.join(", ", eksikGunler), normalFontu);
        p.setSpacingAfter(12f);
        document.add(p);
    }

    private void yasalBaglamBolumuEkle(Document document) throws DocumentException {
        document.add(bolumBasligi("Yasal Bağlam"));

        // DIKKAT: Kesin ceza tutari veya yaptirim rakami YAZILMAZ. docs/proje_plani.md
        // Bolum 11'e gore regulasyon detaylari (ceza tutari, bolge siniflandirmasi)
        // haber kaynaklarindan derlenmis, resmi yonetmelik metniyle dogrulanmamistir.
        // Denetciye giden bir belgede dogrulanmamis rakam/iddia bulunamaz.
        String metin = "Caretta caretta (deniz kaplumbağası) yuvalama sezonu Mayıs-Eylül ayları arasındadır. "
                + "Bu dönemde yuvalama sahillerinde gece 20:00 ile sabah 08:00 arası sahile giriş kısıtlaması "
                + "uygulandığı, bu sahillerin 2872 sayılı Çevre Kanunu kapsamında koruma bölgesi olarak "
                + "sınıflandırıldığı kamuya açık kaynaklarda belirtilmektedir. Bu bölümdeki bilgiler kamuya "
                + "açık kaynaklardan derlenmiş olup resmi mevzuat metniyle doğrulanmamıştır ve hukuki görüş "
                + "niteliği taşımaz. Bu raporda herhangi bir ceza tutarı veya kesin yaptırım bilgisi yer "
                + "almamaktadır; ilgili yükümlülüklerin güncel ve kesin içeriği için resmi mevzuat metni esas "
                + "alınmalıdır.";

        Paragraph p = new Paragraph(metin, normalFontu);
        p.setSpacingAfter(14f);
        document.add(p);
    }

    private void durustlukNotuEkle(Document document) throws DocumentException {
        Paragraph ayirac = new Paragraph(" ", kucukFontu);
        document.add(ayirac);

        Paragraph p = new Paragraph(DURUSTLUK_NOTU, kucukFontu);
        document.add(p);
    }

    private Paragraph bolumBasligi(String metin) {
        Paragraph baslik = new Paragraph(metin, altBaslikFontu);
        baslik.setSpacingBefore(8f);
        baslik.setSpacingAfter(6f);
        return baslik;
    }

    private List<GunDurumu> gunlukDurumlariHesapla(UyumRaporuVerisi veri) {
        Set<LocalDate> kanitliSet = new HashSet<>(veri.kanitliTarihler());
        List<GunDurumu> sonuc = new ArrayList<>();

        LocalDate gun = veri.donemBaslangic();
        while (!gun.isAfter(veri.donemBitis())) {
            sonuc.add(new GunDurumu(gun, kanitliSet.contains(gun)));
            gun = gun.plusDays(1);
        }
        return sonuc;
    }

    private record GunDurumu(LocalDate tarih, boolean kanitVar) {
    }
}
