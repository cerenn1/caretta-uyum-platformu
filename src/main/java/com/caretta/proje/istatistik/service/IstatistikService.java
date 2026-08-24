package com.caretta.proje.istatistik.service;

import com.caretta.proje.common.ZamanDilimi;
import com.caretta.proje.istatistik.dto.IstatistikResponse;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import com.caretta.proje.otel.service.KapanisKanitiService;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IstatistikService {

    /**
     * k-anonimlik esigi: aktif otel sayisi bu degerin ALTINDAYSA ortalamaUyumOrani
     * bastirilir (null donulur). Bu endpoint kimlik dogrulamasi olmadan (permitAll)
     * disariya/fon kuruluslarina acik "toplu, kisisel veri icermeyen istatistik"
     * olarak tasarlandi; ancak aktif otel sayisi 1 veya 2 iken donen ortalama,
     * pratikte o TEK (veya iki) otelin kendi ozel uyum oranini ifsa eder - bu artik
     * anonim/toplu bir veri degildir. 3, kucuk orneklem k-anonimligi icin yaygin
     * kullanilan bir esik degeridir.
     */
    private static final int K_ANONIMLIK_ESIGI = 3;

    private final YuvaKaydiRepository yuvaKaydiRepository;
    private final KapanisKanitiRepository kapanisKanitiRepository;
    private final KapanisKanitiService kapanisKanitiService;

    public IstatistikResponse hesapla() {
        long toplamYuvaKaydiSayisi = yuvaKaydiRepository.count();
        long toplamKatkidaBulunanKullaniciSayisi = yuvaKaydiRepository.distinctKullaniciSayisi();

        List<Long> aktifOtelIdListesi = kapanisKanitiRepository.distinctAktifOtelIdListesi();
        long aktifOtelSayisi = aktifOtelIdListesi.size();

        Double ortalamaUyumOrani = ortalamaUyumOraniHesapla(aktifOtelIdListesi);

        return new IstatistikResponse(
                toplamYuvaKaydiSayisi,
                toplamKatkidaBulunanKullaniciSayisi,
                aktifOtelSayisi,
                ortalamaUyumOrani,
                LocalDate.now(ZamanDilimi.TURKIYE)
        );
    }

    /**
     * Aktif otel yoksa null doner - 0 donmek "oteller var ama uyumsuzlar" gibi
     * yaniltici bir izlenim yaratirdi. Aktif otel sayisi K_ANONIMLIK_ESIGI'nin
     * altindaysa da (k-anonimlik) null doner - aksi halde tek/iki otelin kendi
     * uyum orani sizdirilmis olur. Her aktif otel icin uyum orani, TEK KAYNAK
     * olan KapanisKanitiService#donemUyumOraniHesapla ile hesaplanir; boylece bu
     * ortalama, /uyum-orani endpoint'iyle ayni "son 30 gun" tanimini kullanir.
     */
    private Double ortalamaUyumOraniHesapla(List<Long> aktifOtelIdListesi) {
        if (aktifOtelIdListesi.size() < K_ANONIMLIK_ESIGI) {
            return null;
        }

        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);
        LocalDate baslangic = bugun.minusDays(KapanisKanitiService.UYUM_ORANI_DONEM_GUN_SAYISI - 1);

        double toplam = 0.0;
        for (Long otelId : aktifOtelIdListesi) {
            toplam += kapanisKanitiService.donemUyumOraniHesapla(otelId, baslangic, bugun).uyumOrani();
        }

        double ortalama = toplam / aktifOtelIdListesi.size();
        return Math.round(ortalama * 10.0) / 10.0;
    }
}
