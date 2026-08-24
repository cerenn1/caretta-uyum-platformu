package com.caretta.proje.puansistemi.entity;

// Rozet HICBIR YERDE persist edilmez; kullanicinin toplam yuva kayit sayisindan
// anlik olarak hesaplanir (bkz. PanelOzetiService). Ayri bir "rozetler" tablosu yoktur.
//
// oduAciklamasi alanlari TAMAMEN SEMBOLIK/BILGILENDIRME amaclidir - sistem gercek bir
// indirim kodu URETMEZ, herhangi bir odeme/kupon entegrasyonu YOKTUR. Bu metin sadece
// "kullanici bu odulu hak etti" bilgisini kullaniciya gostermek icindir; odulun fiili
// teslimi (kod uretimi, indirim uygulamasi) bu sistemin disinda, manuel/is surecidir.
public enum Rozet {
    BRONZ(5, "Partner otelde %5 indirim kodu"),
    GUMUS(20, "Partner otelde %10 indirim kodu"),
    ALTIN(50, "Partner otelde ücretsiz bir gecelik konaklama");

    private final long esikYuvaKayitSayisi;
    private final String oduAciklamasi;

    Rozet(long esikYuvaKayitSayisi, String oduAciklamasi) {
        this.esikYuvaKayitSayisi = esikYuvaKayitSayisi;
        this.oduAciklamasi = oduAciklamasi;
    }

    public long getEsikYuvaKayitSayisi() {
        return esikYuvaKayitSayisi;
    }

    public String getOduAciklamasi() {
        return oduAciklamasi;
    }

    // Esik degerleri artik enum'un kendi alaninda tutuluyor - burada hardcoded sayi YOK,
    // tek kaynak yukaridaki enum sabitleri (BRONZ=5, GUMUS=20, ALTIN=50).
    public static Rozet hesapla(long yuvaKayitSayisi) {
        if (yuvaKayitSayisi >= ALTIN.esikYuvaKayitSayisi) return ALTIN;
        if (yuvaKayitSayisi >= GUMUS.esikYuvaKayitSayisi) return GUMUS;
        if (yuvaKayitSayisi >= BRONZ.esikYuvaKayitSayisi) return BRONZ;
        return null; // esik altinda henuz rozet yok
    }

    /** Bir sonraki rozet seviyesi - ALTIN icin null doner (zaten en yuksek seviye). */
    public Rozet sonraki() {
        if (this == BRONZ) return GUMUS;
        if (this == GUMUS) return ALTIN;
        return null;
    }
}
