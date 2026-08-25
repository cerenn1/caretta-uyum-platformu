package com.caretta.proje.istatistik.dto;

import java.util.List;

/**
 * Turkiye'de resmi olarak koruma altindaki caretta caretta yuvalama
 * kumsallarinin SABIT/STATIK referans listesi.
 *
 * ONEMLI: Bu bir JPA @Entity DEGILDIR, veritabaninda TUTULMAZ - sadece
 * (1) "Etkimiz / Kapsam Alanimiz" bolumunde gosterilecek statik il/kumsal
 * listesi ve (2) bir yuva kaydinin en yakin resmi kumsala ne kadar
 * yakin oldugunu hesaplamak (bkz. KapsamAlaniService, Haversine formulu)
 * icin kullanilan, kodun icine gomulu sabit bir Java kaydidir (record).
 *
 * KAYNAK / DIKKAT (docs/proje_plani.md'deki temkinli dille ayni):
 * - Kumsal - il eslestirmesi haber kaynaklarindan derlenmis resmi koruma
 *   statusu bilgisidir; resmi urunlestirmede Cevre, Sehircilik ve Iklim
 *   Degisikligi Bakanligi'nin guncel yonetmelik metniyle dogrulanmalidir.
 * - latitude/longitude degerleri GENEL COGRAFI BILGIDEN TAHMIN EDILMISTIR,
 *   GPS OLCUMU veya RESMI KAYNAK DEGILDIR. Sadece "bu yuva kaydina en
 *   yakin resmi kumsal hangisi" sorusunu ~25km hassasiyetle cevaplamaya
 *   yeterlidir; ileride hassasiyet onemli olursa (orn. resmi sinir
 *   cizimi/sertifikasyon basvurusu) bakanlik/EKAD kaynakli resmi
 *   koordinatlarla degistirilmelidir.
 */
public record ResmiKumsal(String ad, String il, double latitude, double longitude) {

    public static final List<ResmiKumsal> TUMU = List.of(
            // Mugla
            new ResmiKumsal("Ekincik", "Muğla", 36.75, 28.55),
            new ResmiKumsal("Dalyan", "Muğla", 36.83, 28.63),
            new ResmiKumsal("Dalaman", "Muğla", 36.77, 28.80),
            new ResmiKumsal("Fethiye", "Muğla", 36.62, 29.12),
            // Antalya
            new ResmiKumsal("Patara", "Antalya", 36.27, 29.32),
            new ResmiKumsal("Kale (Demre)", "Antalya", 36.24, 29.98),
            new ResmiKumsal("Kumluca", "Antalya", 36.37, 30.29),
            new ResmiKumsal("Olimpos-Çıralı", "Antalya", 36.39, 30.47),
            new ResmiKumsal("Tekirova", "Antalya", 36.53, 30.53),
            new ResmiKumsal("Belek", "Antalya", 36.86, 31.05),
            new ResmiKumsal("Kızılot", "Antalya", 36.75, 31.45),
            new ResmiKumsal("Demirtaş", "Antalya", 36.57, 31.95),
            new ResmiKumsal("Gazipaşa", "Antalya", 36.27, 32.31),
            // Mersin
            new ResmiKumsal("Anamur", "Mersin", 36.08, 32.84),
            new ResmiKumsal("Göksu Deltası", "Mersin", 36.25, 33.95),
            new ResmiKumsal("Alata", "Mersin", 36.55, 34.30),
            new ResmiKumsal("Davultepe", "Mersin", 36.75, 34.55),
            new ResmiKumsal("Kazanlı", "Mersin", 36.82, 34.63),
            // Adana
            new ResmiKumsal("Akyatan", "Adana", 36.63, 35.28),
            new ResmiKumsal("Yumurtalık", "Adana", 36.77, 35.79),
            // Hatay
            new ResmiKumsal("Samandağ", "Hatay", 36.08, 35.98)
    );
}
