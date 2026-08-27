package com.caretta.proje.puansistemi.entity;

// Rozet HICBIR YERDE persist edilmez; kullanicinin toplam yuva kayit sayisindan
// anlik olarak hesaplanir (bkz. PanelOzetiService). Ayri bir "rozetler" tablosu yoktur.
//
// NOT: Odul mesaji tamamen kaldirildi (bkz. PuanService.detayHesapla) - platform
// sadece puan + rozet + siralama sunar, herhangi bir odul vaadinde bulunmaz. Enum
// sadece esik degerlerini ve seviye gecislerini bilir.
public enum Rozet {
    BRONZ(5),
    GUMUS(20),
    ALTIN(50);

    private final long esikYuvaKayitSayisi;

    Rozet(long esikYuvaKayitSayisi) {
        this.esikYuvaKayitSayisi = esikYuvaKayitSayisi;
    }

    public long getEsikYuvaKayitSayisi() {
        return esikYuvaKayitSayisi;
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
