package com.caretta.proje.istatistik.service;

import com.caretta.proje.istatistik.dto.BolgeKaydiSayisi;
import com.caretta.proje.istatistik.dto.KapsamAlaniResponse;
import com.caretta.proje.istatistik.dto.MaviBayrakVerisi;
import com.caretta.proje.istatistik.dto.ResmiKumsal;
import com.caretta.proje.istatistik.dto.ResmiKumsalIlGrubu;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * "Etkimiz / Kapsam Alanimiz" bolumu icin uc AYRI veri kumesini bir arada
 * hazirlayan servis: (1) resmi kumsallarin sabit listesi, (2) Mavi Bayrak
 * sabit verisi, (3) platformun kendi canli istatistigi + kayitlarin otomatik
 * bolge dagilimi.
 *
 * ONEMLI TASARIM KARARI: kayitlarin bolgeye atanmasi GERCEK bir reverse-
 * geocoding (harici servise koordinat gonderip yer adi alma) ile YAPILMAZ.
 * Bunun yerine her yuva kaydinin enlem/boylami, 21 resmi kumsalin SABIT
 * referans koordinatlariyla Haversine formuluyle TAMAMEN yerel/sunucu-ici
 * olarak karsilastirilir - hicbir dis servise istek atilmaz, yuva
 * koordinatlari sunucudan hic cikmaz. Boylece CLAUDE.md'de zaten belgelenmis
 * "harita/konum verisinin uculcu tarafa sizmasi" riski hic artmaz.
 */
@Service
@RequiredArgsConstructor
public class KapsamAlaniService {

    // IstatistikService.K_ANONIMLIK_ESIGI ile AYNI deger/mantik: bir bolgede
    // bu esigin altinda kayit varsa o bolgeyi ayri gostermek, o bolgedeki
    // tek/iki kullaniciyi fiilen ifsa eder - bu artik anonim/toplu bir veri
    // olmaktan cikar. Esigin altinda kalan bolgeler "Diger Bolge"ye tasinir.
    private static final int K_ANONIMLIK_ESIGI = 3;

    // Bir yuva kaydinin, en yakin resmi kumsala "ait" sayilabilmesi icin
    // gereken maksimum mesafe (km). Bu esigin disinda kalan kayitlar "Diger
    // Bolge"ye toplanir - yanlis/uzak bir kumsala atfetmektense genel bir
    // kovaya koymak tercih edildi.
    private static final double ESLESME_ESIGI_KM = 25.0;

    private static final String DIGER_BOLGE_ADI = "Diğer Bölge";

    private static final String RESMI_VERI_KAYNAGI =
            "Haber kaynaklarindan derlenmis resmi koruma statusu bilgisidir; resmi "
                    + "urunlestirmede Cevre, Sehircilik ve Iklim Degisikligi Bakanligi'nin "
                    + "guncel yonetmelik metniyle dogrulanmalidir. Kumsal konumlari da yaklasik "
                    + "(GPS olcumu degil) referans koordinatlardir.";

    private final YuvaKaydiRepository yuvaKaydiRepository;
    private final KapanisKanitiRepository kapanisKanitiRepository;

    public KapsamAlaniResponse hesapla() {
        List<ResmiKumsalIlGrubu> resmiKumsallar = resmiKumsallariIlBazindaGrupla();

        long toplamYuvaKaydiSayisi = yuvaKaydiRepository.count();
        long aktifOtelSayisi = kapanisKanitiRepository.distinctAktifOtelSayisi();

        List<BolgeKaydiSayisi> bolgeler = platformKayitBolgeleriHesapla();

        return new KapsamAlaniResponse(
                resmiKumsallar,
                RESMI_VERI_KAYNAGI,
                MaviBayrakVerisi.IL_BAZINDA_SAYI,
                MaviBayrakVerisi.YIL,
                MaviBayrakVerisi.KAYNAK,
                toplamYuvaKaydiSayisi,
                aktifOtelSayisi,
                bolgeler
        );
    }

    /**
     * ResmiKumsal.TUMU listesini (Mugla, Antalya, Mersin, Adana, Hatay
     * sirasiyla) il bazinda gruplar. LinkedHashMap kullanilir ki cevaptaki
     * il sirasi da kaynak listedeki sirayla ayni kalsin.
     */
    private List<ResmiKumsalIlGrubu> resmiKumsallariIlBazindaGrupla() {
        Map<String, List<String>> ilBazinda = new LinkedHashMap<>();
        for (ResmiKumsal kumsal : ResmiKumsal.TUMU) {
            ilBazinda.computeIfAbsent(kumsal.il(), il -> new ArrayList<>()).add(kumsal.ad());
        }

        List<ResmiKumsalIlGrubu> sonuc = new ArrayList<>();
        ilBazinda.forEach((il, kumsallar) -> sonuc.add(new ResmiKumsalIlGrubu(il, kumsallar)));
        return sonuc;
    }

    /**
     * Tum yuva kayitlarinin konumlarini ceker, her birini en yakin resmi
     * kumsala (25km esik ile) atar, k-anonimlik esigini uygular ve
     * kayitSayisi'na gore azalan sirada dondurur.
     */
    private List<BolgeKaydiSayisi> platformKayitBolgeleriHesapla() {
        List<Object[]> konumlar = yuvaKaydiRepository.tumKonumlariGetir();

        // bolgeAdi -> il (Diger Bolge icin deger her zaman null), bolgeAdi -> sayac.
        Map<String, String> bolgeIlEslesmesi = new LinkedHashMap<>();
        Map<String, Long> sayaclar = new LinkedHashMap<>();

        for (Object[] konum : konumlar) {
            double lat = ((Number) konum[0]).doubleValue();
            double lon = ((Number) konum[1]).doubleValue();

            ResmiKumsal enYakinKumsal = null;
            double enKisaMesafeKm = Double.MAX_VALUE;
            for (ResmiKumsal kumsal : ResmiKumsal.TUMU) {
                double mesafeKm = haversineKm(lat, lon, kumsal.latitude(), kumsal.longitude());
                if (mesafeKm < enKisaMesafeKm) {
                    enKisaMesafeKm = mesafeKm;
                    enYakinKumsal = kumsal;
                }
            }

            String bolgeAdi;
            String il;
            if (enYakinKumsal != null && enKisaMesafeKm <= ESLESME_ESIGI_KM) {
                bolgeAdi = enYakinKumsal.ad();
                il = enYakinKumsal.il();
            } else {
                bolgeAdi = DIGER_BOLGE_ADI;
                il = null;
            }

            bolgeIlEslesmesi.putIfAbsent(bolgeAdi, il);
            sayaclar.merge(bolgeAdi, 1L, Long::sum);
        }

        Map<String, Long> kAnonimlikUygulanmisSayaclar = kAnonimlikUygula(sayaclar);

        List<BolgeKaydiSayisi> sonuc = new ArrayList<>();
        kAnonimlikUygulanmisSayaclar.forEach((bolgeAdi, sayi) ->
                sonuc.add(new BolgeKaydiSayisi(bolgeAdi, bolgeIlEslesmesi.get(bolgeAdi), sayi)));

        sonuc.sort(Comparator.comparingLong(BolgeKaydiSayisi::kayitSayisi).reversed());
        return sonuc;
    }

    /**
     * K-anonimlik: K_ANONIMLIK_ESIGI'nin ALTINDA kayda sahip bir bolgeyi
     * (1 veya 2 kayit) AYRI GOSTERMEZ, "Diger Bolge" kovasina tasir. Aksi
     * halde o bolgede aktif olan TEK/IKI kullaniciyi fiilen ifsa eder
     * (IstatistikService'teki k-anonimlik gerekcesiyle AYNI mantik).
     */
    private Map<String, Long> kAnonimlikUygula(Map<String, Long> sayaclar) {
        long digerBolgeSayisi = sayaclar.getOrDefault(DIGER_BOLGE_ADI, 0L);
        Map<String, Long> sonuc = new LinkedHashMap<>();

        for (Map.Entry<String, Long> entry : sayaclar.entrySet()) {
            String bolgeAdi = entry.getKey();
            long sayi = entry.getValue();

            if (bolgeAdi.equals(DIGER_BOLGE_ADI)) {
                continue;
            }
            if (sayi < K_ANONIMLIK_ESIGI) {
                digerBolgeSayisi += sayi;
            } else {
                sonuc.put(bolgeAdi, sayi);
            }
        }

        if (digerBolgeSayisi > 0) {
            sonuc.put(DIGER_BOLGE_ADI, digerBolgeSayisi);
        }

        return sonuc;
    }

    /**
     * Haversine formulu - dunyayi kure varsayarak iki koordinat arasindaki
     * "kus ucusu" mesafeyi km cinsinden hesaplar. Tamamen yerel/matematiksel
     * bir hesaplamadir; hicbir dis servise (reverse-geocoding vb.) istek
     * ATILMAZ. Paket-private (default erisim) - KapsamAlaniServiceTest'in
     * ayni pakette dogrudan cagirip Haversine sonucunu tek basina
     * dogrulayabilmesi icindir.
     */
    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double dunyaYaricapiKm = 6371.0;

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return dunyaYaricapiKm * c;
    }
}
