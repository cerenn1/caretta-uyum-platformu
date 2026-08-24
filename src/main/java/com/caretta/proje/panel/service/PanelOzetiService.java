package com.caretta.proje.panel.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.common.ZamanDilimi;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import com.caretta.proje.otel.service.KapanisKanitiService;
import com.caretta.proje.panel.dto.PanelOzetiResponse;
import com.caretta.proje.puansistemi.entity.Rozet;
import com.caretta.proje.puansistemi.service.PuanService;
import com.caretta.proje.yuvatakip.entity.YuvaKaydi;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PanelOzetiService {

    private final YuvaKaydiRepository yuvaKaydiRepository;
    private final KapanisKanitiRepository kapanisKanitiRepository;
    private final KapanisKanitiService kapanisKanitiService;
    private final PuanService puanService;

    public PanelOzetiResponse ozetGetir(User currentUser) {
        long yuvaKayitToplam = yuvaKaydiRepository.countByUserId(currentUser.getId());

        Optional<YuvaKaydi> sonKayit =
                yuvaKaydiRepository.findFirstByUserIdOrderByTarihDescIdDesc(currentUser.getId());
        LocalDate sonYuvaKaydiTarih = sonKayit.map(YuvaKaydi::getTarih).orElse(null);
        String sonYuvaKaydiDurum = sonKayit.map(k -> k.getDurum().name()).orElse(null);

        Long otelId = null;
        String otelAdi = null;
        Double uyumOrani = null;
        LocalDate donemBaslangic = null;
        LocalDate donemBitis = null;
        Long donemGunSayisi = null;
        Long kanitYuklenenGunSayisi = null;
        Boolean bugunKanitYuklendiMi = null;

        // KRITIK: bu alanlar SADECE otel calisanina ve sadece bir otele bagliysa doldurulur.
        // Baska her durumda (rol farkli, ya da teorik olarak otel==null) hepsi null kalir.
        if (currentUser.getRole() == Rol.OTEL_CALISANI && currentUser.getOtel() != null) {
            Otel otel = currentUser.getOtel();

            // Zaman dilimi ACIKCA Turkiye - KapanisKanitiService#uyumOraniHesapla ile
            // BIREBIR AYNI "bugun" ve "son 30 gun" tanimini kullanmak icin. TEK KAYNAK
            // olan donemUyumOraniHesapla metodu cagrilir; uyum orani burada YENIDEN
            // HESAPLANMAZ, sadece ortak metottan okunur.
            LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);
            LocalDate baslangic = bugun.minusDays(KapanisKanitiService.UYUM_ORANI_DONEM_GUN_SAYISI - 1);

            UyumOraniResponse uyumOraniResponse =
                    kapanisKanitiService.donemUyumOraniHesapla(otel.getId(), baslangic, bugun);

            otelId = uyumOraniResponse.otelId();
            otelAdi = uyumOraniResponse.otelAdi();
            uyumOrani = uyumOraniResponse.uyumOrani();
            donemBaslangic = uyumOraniResponse.donemBaslangic();
            donemBitis = uyumOraniResponse.donemBitis();
            donemGunSayisi = uyumOraniResponse.donemGunSayisi();
            kanitYuklenenGunSayisi = uyumOraniResponse.kanitYuklenenGunSayisi();

            // Ayni "bugun" degeriyle: uyum orani hesabinda kullanilan bugun ile burada
            // kontrol edilen bugun farkli olursa (ör. gece yarisi race'i) tutarsizlik cikar.
            bugunKanitYuklendiMi = kapanisKanitiRepository.existsByOtelIdAndTarih(otel.getId(), bugun);
        }

        // Puan/rozet: rol farki gozetmeksizin TUM kullanicilar icin doldurulur.
        Long toplamPuan = puanService.toplamPuanHesapla(currentUser.getId());
        String rozet = Optional.ofNullable(Rozet.hesapla(yuvaKayitToplam)).map(Enum::name).orElse(null);

        return new PanelOzetiResponse(
                currentUser.getEmail(),
                currentUser.getRole().name(),
                yuvaKayitToplam,
                sonYuvaKaydiTarih,
                sonYuvaKaydiDurum,
                otelId,
                otelAdi,
                uyumOrani,
                donemBaslangic,
                donemBitis,
                donemGunSayisi,
                kanitYuklenenGunSayisi,
                bugunKanitYuklendiMi,
                toplamPuan,
                rozet
        );
    }
}
