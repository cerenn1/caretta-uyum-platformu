package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.puansistemi.entity.KullaniciPuani;
import com.caretta.proje.puansistemi.entity.Rozet;
import com.caretta.proje.puansistemi.repository.KullaniciPuaniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// GUVENLIK: puanEkle'yi disaridan dogrudan cagiran hicbir HTTP endpoint yoktur ve
// olmamalidir. puanEkle SADECE sunucu tarafinda, ilgili is akisinin (ör.
// YuvaKaydiService.ekle, KapanisKanitiService.yukle) icinden, sabit degerlerle cagrilir.
// Istemciden "puan" diye bir alan asla kabul edilmez. detayHesapla ise SALT OKUNUR bir
// hesaplamadir (puan eklemez) ve /api/puan-detay endpoint'i uzerinden (PuanDetayService
// araciligiyla) dolayli olarak cagrilabilir.
@Service
@RequiredArgsConstructor
public class PuanService {

    // ODUL TURU (partner otel indirimi mi, dogrudan maddi odul mu) HENUZ KARARA
    // BAGLANMADI - bu yuzden burada SPESIFIK bir sey (yuzde, tutar, indirim turu)
    // VAAT EDILMEZ, net ama belirsiz genel bir mesaj gosterilir. Rol farki YOK -
    // otel calisani da normal kullanici da AYNI mesaji gorur. Paket-private (test
    // erisimi icin): bkz. PuanServiceTest.
    static final String ODUL_MESAJI =
            "Bu rozet seviyesinde bir ödül hak ettin! Detaylar için proje yöneticinle iletişime geç.";

    private final KullaniciPuaniRepository kullaniciPuaniRepository;

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
     * verilir - PuanService, modul sinirlarina saygi geregi YuvaKaydiRepository'ye
     * dogrudan bagimli degildir (bkz. PuanDetayService).
     */
    public PuanDetayResponse detayHesapla(User currentUser, long yuvaKayitToplam) {
        long toplamPuan = toplamPuanHesapla(currentUser.getId());
        Rozet mevcutRozet = Rozet.hesapla(yuvaKayitToplam);
        Rozet sonrakiRozet = mevcutRozet == null ? Rozet.BRONZ : mevcutRozet.sonraki();

        Long kalanKayit = sonrakiRozet == null
                ? null
                : Math.max(0, sonrakiRozet.getEsikYuvaKayitSayisi() - yuvaKayitToplam);

        String odulMesaji = mevcutRozet == null ? null : ODUL_MESAJI;

        return new PuanDetayResponse(
                toplamPuan,
                mevcutRozet == null ? null : mevcutRozet.name(),
                yuvaKayitToplam,
                sonrakiRozet == null ? null : sonrakiRozet.name(),
                kalanKayit,
                odulMesaji
        );
    }
}
