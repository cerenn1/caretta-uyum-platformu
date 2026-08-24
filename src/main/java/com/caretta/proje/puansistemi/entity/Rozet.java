package com.caretta.proje.puansistemi.entity;

// Rozet HICBIR YERDE persist edilmez; kullanicinin toplam yuva kayit sayisindan
// anlik olarak hesaplanir (bkz. PanelOzetiService). Ayri bir "rozetler" tablosu yoktur.
public enum Rozet {
    BRONZ, GUMUS, ALTIN;

    public static Rozet hesapla(long yuvaKayitSayisi) {
        if (yuvaKayitSayisi >= 50) return ALTIN;
        if (yuvaKayitSayisi >= 20) return GUMUS;
        if (yuvaKayitSayisi >= 5) return BRONZ;
        return null; // esik altinda henuz rozet yok
    }
}
