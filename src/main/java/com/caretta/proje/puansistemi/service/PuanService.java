package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.entity.KullaniciPuani;
import com.caretta.proje.puansistemi.repository.KullaniciPuaniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// GUVENLIK: bu servisi disaridan cagiran hicbir HTTP endpoint yoktur ve olmamalidir.
// puanEkle SADECE sunucu tarafinda, ilgili is akisinin (ör. YuvaKaydiService.ekle)
// icinden, sabit degerlerle cagrilir. Istemciden "puan" diye bir alan asla kabul edilmez.
@Service
@RequiredArgsConstructor
public class PuanService {

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
}
