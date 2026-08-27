package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.puansistemi.dto.SiralamaResponse;
import com.caretta.proje.puansistemi.dto.SiralamaSatiri;
import com.caretta.proje.puansistemi.entity.KullaniciPuani;
import com.caretta.proje.puansistemi.entity.Rozet;
import com.caretta.proje.puansistemi.repository.KullaniciPuaniRepository;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// GUVENLIK: puanEkle'yi disaridan dogrudan cagiran hicbir HTTP endpoint yoktur ve
// olmamalidir. puanEkle SADECE sunucu tarafinda, ilgili is akisinin (ör.
// YuvaKaydiService.ekle, KapanisKanitiService.yukle) icinden, sabit degerlerle cagrilir.
// Istemciden "puan" diye bir alan asla kabul edilmez. detayHesapla ise SALT OKUNUR bir
// hesaplamadir (puan eklemez) ve /api/puan-detay endpoint'i uzerinden (PuanDetayService
// araciligiyla) dolayli olarak cagrilabilir.
@Service
@RequiredArgsConstructor
public class PuanService {

    private final KullaniciPuaniRepository kullaniciPuaniRepository;
    private final UserRepository userRepository;
    private final YuvaKaydiRepository yuvaKaydiRepository;

    public void puanEkle(User kullanici, int puan, String sebep) {
        KullaniciPuani kayit = KullaniciPuani.builder()
                .kullanici(kullanici)
                .puan(puan)
                .sebep(sebep)
                .build();

        kullaniciPuaniRepository.save(kayit);
    }

    public Long toplamPuanHesapla(Long kullaniciId) {
        Long toplam = kullaniciPuaniRepository.toplamPuanHesapla(kullaniciId);
        return toplam != null ? toplam : 0L;
    }

    /**
     * Puan/rozet detay ekrani icin tum hesabin TEK KAYNAGI. yuvaKayitToplam disaridan
     * verilir - detay hesabi PuanDetayService araciligiyla cagrilir. (NOT:
     * siralamaGetir asagida hiz icin dogrudan YuvaKaydiRepository'ye bagimli -
     * bu metodun modul siniri istisnasidir.)
     */
    public PuanDetayResponse detayHesapla(User currentUser, long yuvaKayitToplam) {
        long toplamPuan = toplamPuanHesapla(currentUser.getId());
        Rozet mevcutRozet = Rozet.hesapla(yuvaKayitToplam);
        Rozet sonrakiRozet = mevcutRozet == null ? Rozet.BRONZ : mevcutRozet.sonraki();

        Long kalanKayit = sonrakiRozet == null
                ? null
                : Math.max(0, sonrakiRozet.getEsikYuvaKayitSayisi() - yuvaKayitToplam);

        return new PuanDetayResponse(
                toplamPuan,
                mevcutRozet == null ? null : mevcutRozet.name(),
                yuvaKayitToplam,
                sonrakiRozet == null ? null : sonrakiRozet.name(),
                kalanKayit
        );
    }

    /**
     * Liderlik tablosu (/api/puan-siralamasi). Rol farki YOK - otel calisani da normal
     * kullanici da AYNI birlesik siralamada yer alir. MVP olcegi (az kullanici) icin
     * N+1 sorgu kabul edilebilir, optimize etmeye CALISILMADI.
     */
    public SiralamaResponse siralamaGetir(User currentUser) {
        List<Object[]> siralama = kullaniciPuaniRepository.kullaniciBazindaToplamPuanSiralamasi();

        List<SiralamaSatiri> tumSira = new ArrayList<>();
        SiralamaSatiri kendiSirasi = null;

        for (int i = 0; i < siralama.size(); i++) {
            Object[] satir = siralama.get(i);
            Long kullaniciId = (Long) satir[0];
            long toplamPuan = ((Number) satir[1]).longValue();

            String email = userRepository.findById(kullaniciId)
                    .map(User::getEmail)
                    .orElse("bilinmiyor");
            long yuvaKayitToplam = yuvaKaydiRepository.countByUserId(kullaniciId);
            Rozet rozet = Rozet.hesapla(yuvaKayitToplam);

            SiralamaSatiri satiriDto = new SiralamaSatiri(
                    i + 1, email, toplamPuan, rozet == null ? null : rozet.name());

            if (i < 10) {
                tumSira.add(satiriDto);
            }
            if (kullaniciId.equals(currentUser.getId())) {
                kendiSirasi = satiriDto;
            }
        }

        SiralamaSatiri kendiSirasiDisariAktarilacak = tumSira.contains(kendiSirasi) ? null : kendiSirasi;

        return new SiralamaResponse(tumSira, kendiSirasiDisariAktarilacak);
    }
}
