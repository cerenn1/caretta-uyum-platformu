package com.caretta.proje.raporlama.service;

import com.caretta.proje.common.PdfFontYukleyici;
import com.caretta.proje.raporlama.dto.UyumRaporuVerisi;
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
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    // Ay ismi dizisi: 1=Ocak ... 12=Aralik. java.time'in Locale("tr") uzerinden ay adi
    // uretmesine (Turkce "I/i" locale hatalarina acik) guvenmek yerine sabit dizi kullanilir.
    private static final String[] AY_ADLARI = {
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    };

    // Takvim izgarasi Pazartesi'den baslar (DayOfWeek.MONDAY=1 ... SUNDAY=7 ile birebir siralı).
    private static final String[] HAFTA_GUNLERI = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};

    // Mobil uygulamanin paletiyle uyumlu deniz mavisi - baslik bandi ve takvim basligi.
    private static final Color RENK_BASLIK_BANDI = new Color(0x01, 0x57, 0x9B);
    private static final Color RENK_GRID_CIZGISI = new Color(0xDD, 0xDD, 0xDD);

    // Takvim hucre zeminleri: renge EK OLARAK isaret (✓ / –) kullanilir, sadece renge
    // dayanilmaz (renk korlugu / siyah-beyaz cikti icin).
    private static final Color RENK_KANIT_VAR_ZEMIN = new Color(0xC8, 0xE6, 0xC9);
    private static final Color RENK_KANIT_YOK_ZEMIN = new Color(0xFF, 0xCD, 0xD2);
    private static final Color RENK_DONEM_DISI_ZEMIN = new Color(0xF5, 0xF5, 0xF5);
    private static final Color RENK_DONEM_DISI_YAZI = new Color(0x75, 0x75, 0x75);

    // Uyum orani esik renkleri: >=90 yesil, %70-90 turuncu, <%70 kirmizi.
    private static final Color RENK_YUKSEK_UYUM = new Color(0x2E, 0x7D, 0x32);
    private static final Color RENK_KABUL_EDILEBILIR = new Color(0xEF, 0x6C, 0x00);
    private static final Color RENK_IYILESTIRME_GEREKLI = new Color(0xC6, 0x28, 0x28);
    private static final Color RENK_GAUGE_BOS_ZEMIN = new Color(0xE0, 0xE0, 0xE0);

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
    private Font takvimGunFontu;
    private Font takvimGunNotrFontu;

    @PostConstruct
    void fontlariYukle() {
        try {
            BaseFont normal = PdfFontYukleyici.classpathTtfYukle("/fonts/DejaVuSans.ttf");
            BaseFont kalin = PdfFontYukleyici.classpathTtfYukle("/fonts/DejaVuSans-Bold.ttf");

            baslikFontu = new Font(kalin, 16, Font.BOLD);
            altBaslikFontu = new Font(kalin, 12, Font.BOLD);
            normalFontu = new Font(normal, 10, Font.NORMAL);
            kalinFontu = new Font(kalin, 10, Font.BOLD);
            kucukFontu = new Font(normal, 9, Font.NORMAL);
            tabloBaslikFontu = new Font(kalin, 8, Font.BOLD, Color.WHITE);
            oranBuyukFontu = new Font(kalin, 28, Font.BOLD);
            takvimGunFontu = new Font(kalin, 8, Font.BOLD);
            takvimGunNotrFontu = new Font(normal, 8, Font.NORMAL, RENK_DONEM_DISI_YAZI);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Rapor fontlari yuklenemedi", e);
        }
    }

    public byte[] olustur(UyumRaporuVerisi veri) {
        ByteArrayOutputStream cikti = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 34, 32);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, cikti);
            // Her sayfanin altina "Sayfa X / Y" ve rapor no yazan altbilgi. Toplam sayfa
            // sayisi belge kapanana kadar bilinmedigi icin PdfTemplate yer tutucusu
            // kullanilir (bkz. AltbilgiOlayDinleyici).
            writer.setPageEvent(new AltbilgiOlayDinleyici(veri.raporNo(), kucukFontu));
            document.open();

            List<GunDurumu> gunlukDurumlar = gunlukDurumlariHesapla(veri);

            baslikBolumuEkle(document, veri);
            ozetBolumuEkle(document, veri);
            gunGunDokumBolumuEkle(document, gunlukDurumlar);
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
        document.add(baslikBandiOlustur());
        document.add(kunyeTablosuOlustur(veri));

        Paragraph bosluk = new Paragraph(" ", kucukFontu);
        bosluk.setSpacingAfter(2f);
        document.add(bosluk);
    }

    private PdfPTable baslikBandiOlustur() {
        PdfPTable bant = new PdfPTable(1);
        bant.setWidthPercentage(100);
        bant.setSpacingAfter(6f);

        Font baslikBeyazFontu = new Font(baslikFontu.getBaseFont(), baslikFontu.getSize(), baslikFontu.getStyle(), Color.WHITE);
        PdfPCell hucre = new PdfPCell(new Phrase("Kaplumbağa Yuvalama Sahili Uyum Raporu", baslikBeyazFontu));
        hucre.setBackgroundColor(RENK_BASLIK_BANDI);
        hucre.setHorizontalAlignment(Element.ALIGN_CENTER);
        hucre.setVerticalAlignment(Element.ALIGN_MIDDLE);
        hucre.setPadding(6f);
        hucre.setBorder(Rectangle.NO_BORDER);
        bant.addCell(hucre);

        return bant;
    }

    /**
     * Kunye bilgileri (otel, konum, donem, uretim tarihi, rapor no) iki sutunlu
     * (etiket | deger) bir tabloda, alt cizgili satirlar halinde gosterilir.
     */
    private PdfPTable kunyeTablosuOlustur(UyumRaporuVerisi veri) {
        PdfPTable tablo = new PdfPTable(new float[]{30f, 70f});
        tablo.setWidthPercentage(100);
        tablo.setSpacingAfter(4f);

        kunyeSatiriEkle(tablo, "Otel", veri.otel().getAd());
        kunyeSatiriEkle(tablo, "Konum (Enlem, Boylam)",
                veri.otel().getLatitude() + ", " + veri.otel().getLongitude());
        kunyeSatiriEkle(tablo, "Rapor Dönemi",
                veri.donemBaslangic().format(TARIH_FORMAT) + " - " + veri.donemBitis().format(TARIH_FORMAT));
        kunyeSatiriEkle(tablo, "Rapor Üretim Tarihi",
                veri.uretimZamani().format(TARIH_SAAT_FORMAT) + " (Europe/Istanbul)");
        kunyeSatiriEkle(tablo, "Rapor No", veri.raporNo());

        return tablo;
    }

    private void kunyeSatiriEkle(PdfPTable tablo, String etiket, String deger) {
        PdfPCell etiketHucresi = new PdfPCell(new Phrase(etiket, kalinFontu));
        etiketHucresi.setBorder(Rectangle.BOTTOM);
        etiketHucresi.setBorderColor(RENK_GRID_CIZGISI);
        etiketHucresi.setPadding(2f);
        tablo.addCell(etiketHucresi);

        PdfPCell degerHucresi = new PdfPCell(new Phrase(deger == null ? "-" : deger, normalFontu));
        degerHucresi.setBorder(Rectangle.BOTTOM);
        degerHucresi.setBorderColor(RENK_GRID_CIZGISI);
        degerHucresi.setPadding(2f);
        tablo.addCell(degerHucresi);
    }

    private void ozetBolumuEkle(Document document, UyumRaporuVerisi veri) throws DocumentException {
        document.add(bolumBasligi("Özet"));

        Color esikRengi = esikRengi(veri.uyumOrani());

        Font oranRenkliFontu = new Font(oranBuyukFontu.getBaseFont(), oranBuyukFontu.getSize(),
                oranBuyukFontu.getStyle(), esikRengi);
        Paragraph oran = new Paragraph(String.format("%%%.1f", veri.uyumOrani()), oranRenkliFontu);
        oran.setSpacingAfter(3f);
        document.add(oran);

        document.add(uyumOraniCubuguOlustur(veri.uyumOrani(), esikRengi));

        Paragraph kanitSatiri = new Paragraph(
                "Kanıt yüklenen gün sayısı: " + veri.kanitliGunSayisi() + " / " + veri.donemGunSayisi(),
                normalFontu);
        kanitSatiri.setSpacingBefore(4f);
        document.add(kanitSatiri);

        Font degerlendirmeRenkliFontu = new Font(kalinFontu.getBaseFont(), kalinFontu.getSize(),
                kalinFontu.getStyle(), esikRengi);
        Paragraph degerlendirme = new Paragraph(degerlendirmeCumlesi(veri.uyumOrani()), degerlendirmeRenkliFontu);
        degerlendirme.setSpacingBefore(4f);
        degerlendirme.setSpacingAfter(3f);
        document.add(degerlendirme);

        Paragraph esikNotu = new Paragraph(
                "Değerlendirme eşikleri: %90 ve üzeri \"Yüksek uyum\", %70-90 arası \"Kabul edilebilir uyum\", "
                        + "%70 altı \"İyileştirme gerekli\" olarak sınıflandırılır.",
                kucukFontu);
        esikNotu.setSpacingAfter(8f);
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

    /**
     * Uyum oranina karsilik gelen esik rengini dondurur: >=90 yesil, %70-90 turuncu,
     * %70 alti kirmizi. Hem yuzde metninin hem de doluluk cubugunun rengi bu metottan
     * gelir - iki yerde ayri esik mantigi tekrarlanmaz.
     */
    private Color esikRengi(double uyumOrani) {
        if (uyumOrani >= 90.0) {
            return RENK_YUKSEK_UYUM;
        } else if (uyumOrani >= 70.0) {
            return RENK_KABUL_EDILEBILIR;
        } else {
            return RENK_IYILESTIRME_GEREKLI;
        }
    }

    /**
     * Uyum oranini gorsel olarak da gosteren yatay doluluk cubugu. Ayri bir cizim
     * kutuphanesi kullanilmaz: genislikleri orana gore ayarlanmis iki hucreli tek
     * satirlik bir PdfPTable ile yapilir (dolu kisim esik rengiyle, kalan kisim gri).
     */
    private PdfPTable uyumOraniCubuguOlustur(double uyumOrani, Color renk) {
        // Yuzde teorik olarak her zaman [0, 100] araliginda gelir (bkz. KapanisKanitiService),
        // ama PdfPTable'a SIFIR genislikli sutun vermemek icin savunmaci sekilde sinirlandirilir.
        float doluOran = (float) Math.max(0.0, Math.min(100.0, uyumOrani));
        float dolu = Math.max(doluOran, 0.01f);
        float bos = Math.max(100f - doluOran, 0.01f);

        PdfPTable cubuk = new PdfPTable(new float[]{dolu, bos});
        cubuk.setWidthPercentage(60);
        cubuk.setSpacingAfter(3f);

        PdfPCell doluHucre = new PdfPCell(new Phrase(" ", kucukFontu));
        doluHucre.setBackgroundColor(renk);
        doluHucre.setFixedHeight(10f);
        doluHucre.setBorder(Rectangle.NO_BORDER);
        cubuk.addCell(doluHucre);

        PdfPCell bosHucre = new PdfPCell(new Phrase(" ", kucukFontu));
        bosHucre.setBackgroundColor(RENK_GAUGE_BOS_ZEMIN);
        bosHucre.setFixedHeight(10f);
        bosHucre.setBorder(Rectangle.NO_BORDER);
        cubuk.addCell(bosHucre);

        return cubuk;
    }

    /**
     * Gun gun dokum, uzun bir dikey tablo yerine ay gorunumlu bir takvim izgarasi
     * olarak gosterilir. Donem birden fazla aya yayilirsa her ay icin ayri bir
     * izgara alt alta eklenir.
     */
    private void gunGunDokumBolumuEkle(Document document, List<GunDurumu> gunlukDurumlar) throws DocumentException {
        document.add(bolumBasligi("Gün Gün Döküm"));

        LocalDate donemBaslangic = gunlukDurumlar.get(0).tarih();
        LocalDate donemBitis = gunlukDurumlar.get(gunlukDurumlar.size() - 1).tarih();

        Map<LocalDate, Boolean> kanitDurumu = new HashMap<>();
        for (GunDurumu gunDurumu : gunlukDurumlar) {
            kanitDurumu.put(gunDurumu.tarih(), gunDurumu.kanitVar());
        }

        YearMonth ay = YearMonth.from(donemBaslangic);
        YearMonth sonAy = YearMonth.from(donemBitis);

        while (!ay.isAfter(sonAy)) {
            Paragraph ayBasligi = new Paragraph(AY_ADLARI[ay.getMonthValue() - 1] + " " + ay.getYear(), kalinFontu);
            ayBasligi.setSpacingBefore(1f);
            ayBasligi.setSpacingAfter(1f);
            document.add(ayBasligi);

            document.add(ayTakvimiOlustur(ay, donemBaslangic, donemBitis, kanitDurumu));

            ay = ay.plusMonths(1);
        }
    }

    private PdfPTable ayTakvimiOlustur(YearMonth ay, LocalDate donemBaslangic, LocalDate donemBitis,
                                        Map<LocalDate, Boolean> kanitDurumu) {
        PdfPTable tablo = new PdfPTable(7);
        tablo.setWidthPercentage(100);
        tablo.setSpacingAfter(4f);
        // Bir ayin haftalari sayfa sinirinda bolunup ikiye ayrilmasin - denetim
        // belgesinde bir haftanin yarisinin bir sonraki sayfaya tasmasi okunaksiz olur.
        tablo.setKeepTogether(true);

        for (String gunAdi : HAFTA_GUNLERI) {
            PdfPCell baslikHucresi = new PdfPCell(new Phrase(gunAdi, tabloBaslikFontu));
            baslikHucresi.setBackgroundColor(RENK_BASLIK_BANDI);
            baslikHucresi.setHorizontalAlignment(Element.ALIGN_CENTER);
            baslikHucresi.setPadding(2f);
            tablo.addCell(baslikHucresi);
        }

        LocalDate ayinIlkGunu = ay.atDay(1);
        // DayOfWeek.MONDAY=1 ... SUNDAY=7; ayin ilk gunu haftanin ortasina denk
        // geliyorsa baslangictaki hucreler bos birakilir.
        int bosBaslangicHucreSayisi = ayinIlkGunu.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < bosBaslangicHucreSayisi; i++) {
            tablo.addCell(takvimBosHucresi());
        }

        for (int gunNo = 1; gunNo <= ay.lengthOfMonth(); gunNo++) {
            LocalDate tarih = ay.atDay(gunNo);
            tablo.addCell(takvimGunHucresi(tarih, gunNo, donemBaslangic, donemBitis, kanitDurumu));
        }

        int doluHucreSayisi = bosBaslangicHucreSayisi + ay.lengthOfMonth();
        int sonSatirBosHucreSayisi = (7 - (doluHucreSayisi % 7)) % 7;
        for (int i = 0; i < sonSatirBosHucreSayisi; i++) {
            tablo.addCell(takvimBosHucresi());
        }

        return tablo;
    }

    /**
     * Bir takvim gunu hucresi. Uc durum vardir:
     *  - Donem disi (ayin gunu ama rapor araliginin disinda): notr/gri, sadece gun
     *    numarasi - "kanit yok" gibi gosterilmez, bu yanlis olur.
     *  - Donem icinde, kanit var: yesil zemin + "N ✓"
     *  - Donem icinde, kanit yok: kirmizi zemin + "N –"
     */
    private PdfPCell takvimGunHucresi(LocalDate tarih, int gunNo, LocalDate donemBaslangic, LocalDate donemBitis,
                                       Map<LocalDate, Boolean> kanitDurumu) {
        boolean donemIcinde = !tarih.isBefore(donemBaslangic) && !tarih.isAfter(donemBitis);

        String metin;
        Color zemin;
        Font yaziFontu;

        if (!donemIcinde) {
            metin = String.valueOf(gunNo);
            zemin = RENK_DONEM_DISI_ZEMIN;
            yaziFontu = takvimGunNotrFontu;
        } else if (Boolean.TRUE.equals(kanitDurumu.get(tarih))) {
            metin = gunNo + " ✓"; // ✓
            zemin = RENK_KANIT_VAR_ZEMIN;
            yaziFontu = takvimGunFontu;
        } else {
            metin = gunNo + " –"; // –
            zemin = RENK_KANIT_YOK_ZEMIN;
            yaziFontu = takvimGunFontu;
        }

        PdfPCell hucre = new PdfPCell(new Phrase(metin, yaziFontu));
        hucre.setBackgroundColor(zemin);
        hucre.setHorizontalAlignment(Element.ALIGN_CENTER);
        hucre.setVerticalAlignment(Element.ALIGN_MIDDLE);
        hucre.setPadding(2.5f);
        hucre.setBorderColor(RENK_GRID_CIZGISI);
        return hucre;
    }

    private PdfPCell takvimBosHucresi() {
        PdfPCell hucre = new PdfPCell(new Phrase(" ", kucukFontu));
        hucre.setBackgroundColor(Color.WHITE);
        hucre.setBorderColor(RENK_GRID_CIZGISI);
        hucre.setPadding(2.5f);
        return hucre;
    }

    private void eksikGunlerBolumuEkle(Document document, List<GunDurumu> gunlukDurumlar) throws DocumentException {
        document.add(bolumBasligi("Eksik Günler"));

        List<LocalDate> eksikTarihler = gunlukDurumlar.stream()
                .filter(g -> !g.kanitVar())
                .map(GunDurumu::tarih)
                .toList();

        Paragraph p = new Paragraph(eksikGunlerMetni(eksikTarihler), normalFontu);
        p.setSpacingAfter(10f);
        document.add(p);
    }

    /**
     * Eksik gunleri tek tek listelemek yerine ("22.07.2026, 23.07.2026, ...") ardisik
     * gun araliklarina gruplayip ozetler. Ornekler:
     *  - Tum donem kesintisiz eksikse: "28 gün eksik: 22.07.2026 – 18.08.2026 arası kesintisiz"
     *  - Birden fazla ayri aralik varsa: "3 gün eksik: 22.07.2026, 25.07.2026 – 26.07.2026"
     *  - Hic eksik yoksa: "Dönemde eksik gün bulunmamaktadır."
     */
    private String eksikGunlerMetni(List<LocalDate> eksikTarihler) {
        if (eksikTarihler.isEmpty()) {
            return "Dönemde eksik gün bulunmamaktadır.";
        }

        List<GunAraligi> araliklar = gunAraliklariniGrupla(eksikTarihler);
        String basSatir = eksikTarihler.size() + " gün eksik: ";

        if (araliklar.size() == 1) {
            GunAraligi tekAralik = araliklar.get(0);
            if (tekAralik.baslangic().equals(tekAralik.bitis())) {
                return basSatir + gunAraligiMetni(tekAralik);
            }
            return basSatir + gunAraligiMetni(tekAralik) + " arası kesintisiz";
        }

        String aralikListesi = araliklar.stream()
                .map(UyumRaporuPdfOlusturucu::gunAraligiMetni)
                .collect(Collectors.joining(", "));
        return basSatir + aralikListesi;
    }

    /**
     * Bir tarih araligini "dd.MM.yyyy" veya (tek gunse) sadece "dd.MM.yyyy" olarak
     * bicimlendirir. "X – X" gibi anlamsiz tekrar YAZILMAZ.
     */
    private static String gunAraligiMetni(GunAraligi araligi) {
        if (araligi.baslangic().equals(araligi.bitis())) {
            return araligi.baslangic().format(TARIH_FORMAT);
        }
        return araligi.baslangic().format(TARIH_FORMAT) + " – " + araligi.bitis().format(TARIH_FORMAT);
    }

    /**
     * Bir tarih listesini (herhangi bir sirada, tekrarli olabilir) ardisik gun
     * araliklarina gruplayan SAF fonksiyon - PDF/OpenPDF baglamina bagli degildir,
     * bu yuzden UyumRaporuPdfOlusturucuTest icinde dogrudan birim testi yazilabilir.
     *
     * Ornek: [22.07, 23.07, 25.07, 26.07] -> [(22.07-23.07), (25.07-26.07)]
     */
    static List<GunAraligi> gunAraliklariniGrupla(List<LocalDate> tarihler) {
        List<LocalDate> siraliTarihler = tarihler.stream().distinct().sorted().toList();

        List<GunAraligi> sonuc = new ArrayList<>();
        if (siraliTarihler.isEmpty()) {
            return sonuc;
        }

        LocalDate aralikBaslangici = siraliTarihler.get(0);
        LocalDate oncekiTarih = aralikBaslangici;

        for (int i = 1; i < siraliTarihler.size(); i++) {
            LocalDate tarih = siraliTarihler.get(i);
            if (!tarih.equals(oncekiTarih.plusDays(1))) {
                sonuc.add(new GunAraligi(aralikBaslangici, oncekiTarih));
                aralikBaslangici = tarih;
            }
            oncekiTarih = tarih;
        }
        sonuc.add(new GunAraligi(aralikBaslangici, oncekiTarih));

        return sonuc;
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
        p.setSpacingAfter(10f);
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
        baslik.setSpacingBefore(4f);
        baslik.setSpacingAfter(3f);
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

    /**
     * Ardisik gun araligi (bir "eksik gun" bloğu). baslangic == bitis ise tek gunluk
     * bir aralik demektir.
     */
    record GunAraligi(LocalDate baslangic, LocalDate bitis) {
    }

    /**
     * Her sayfanin altina "Sayfa X / Y" ve rapor numarasini yazan sayfa olayi.
     *
     * TEKNIK NOT: OpenPDF'te toplam sayfa sayisi (Y) belge tamamen kapanana kadar
     * bilinmez - sayfalar tek tek yazildigi icin "kacinci sayfada oldugumuz" bilinir
     * ama "toplam kac sayfa olacagi" bilinmez. Standart cozum: onOpenDocument'ta
     * kucuk bir PdfTemplate (bosluk/yer tutucu) olusturulur; her sayfada bu template
     * "Sayfa X / " metninin hemen sagina yerlestirilir (onEndPage); belge tamamen
     * kapanip nihai sayfa sayisi netlestiginde bu template'in ICINE gercek sayi
     * yazilir (onCloseDocument). Boylece PDF'in en son sayfasi da dahil TUM
     * sayfalardaki template ayni objeye referans verdigi icin dogru toplami gosterir.
     */
    private static final class AltbilgiOlayDinleyici extends PdfPageEventHelper {

        private static final float SABLON_GENISLIK = 24f;
        private static final float SABLON_YUKSEKLIK = 12f;

        private final String raporNo;
        private final Font altbilgiFontu;
        private PdfTemplate toplamSayfaSablonu;
        // OpenPDF'te document.close() sirasinda writer, gercekte hic var olmayan/bos
        // kalan bir "bir sonraki sayfa" icin sayacini onceden bir arttirir; bu yuzden
        // onCloseDocument icinde writer.getPageNumber() COGU ZAMAN gercek toplamdan
        // 1 FAZLA doner (orn. 1 sayfalik bir raporda "Sayfa 1 / 2" gibi yanlis bir
        // cikti verir). Dogru toplami almak icin writer.getPageNumber() yerine, SADECE
        // GERCEKTEN OLUSTURULAN sayfalarda tetiklenen onEndPage'de gorulen SON sayfa
        // numarasi burada saklanir ve onCloseDocument'ta kullanilir.
        private int sonGercekSayfaNo;

        private AltbilgiOlayDinleyici(String raporNo, Font altbilgiFontu) {
            this.raporNo = raporNo;
            this.altbilgiFontu = altbilgiFontu;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            toplamSayfaSablonu = writer.getDirectContent().createTemplate(SABLON_GENISLIK, SABLON_YUKSEKLIK);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            sonGercekSayfaNo = writer.getPageNumber();

            PdfContentByte icerik = writer.getDirectContent();
            BaseFont baseFont = altbilgiFontu.getBaseFont();
            float boyut = altbilgiFontu.getSize();
            float y = document.bottom() - 16f;

            // Sol: rapor no - belge yazdirilip sayfalari ayrilirsa hangi rapora ait
            // oldugu kaybolmamasi icin her sayfada tekrarlanir.
            ColumnText.showTextAligned(icerik, Element.ALIGN_LEFT,
                    new Phrase("Rapor No: " + raporNo, altbilgiFontu),
                    document.left(), y, 0);

            // Sag: "Sayfa X / " metni + hemen ardindan toplam sayfa sayisini tasiyan template.
            String sayfaMetni = "Sayfa " + writer.getPageNumber() + " / ";
            float sayfaMetniGenisligi = baseFont.getWidthPoint(sayfaMetni, boyut);
            float metinBaslangicX = document.right() - sayfaMetniGenisligi - SABLON_GENISLIK;

            icerik.beginText();
            icerik.setFontAndSize(baseFont, boyut);
            icerik.setTextMatrix(metinBaslangicX, y);
            icerik.showText(sayfaMetni);
            icerik.endText();

            icerik.addTemplate(toplamSayfaSablonu, document.right() - SABLON_GENISLIK, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            BaseFont baseFont = altbilgiFontu.getBaseFont();
            toplamSayfaSablonu.beginText();
            toplamSayfaSablonu.setFontAndSize(baseFont, altbilgiFontu.getSize());
            toplamSayfaSablonu.setTextMatrix(0, 0);
            toplamSayfaSablonu.showText(String.valueOf(sonGercekSayfaNo));
            toplamSayfaSablonu.endText();
        }
    }
}
