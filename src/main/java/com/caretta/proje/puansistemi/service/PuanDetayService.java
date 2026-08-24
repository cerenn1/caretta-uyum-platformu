package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * /api/puan-detay endpoint'inin is mantigi. yuvatakip modulune bagimliligi
 * PuanService disinda ayri bir servis olarak tutuyoruz ki PuanService kendi
 * modul sinirlari icinde kalsin (bkz. PanelOzetiService'teki ayni desen).
 */
@Service
@RequiredArgsConstructor
public class PuanDetayService {

    private final YuvaKaydiRepository yuvaKaydiRepository;
    private final PuanService puanService;

    public PuanDetayResponse detayGetir(User currentUser) {
        long yuvaKayitToplam = yuvaKaydiRepository.countByUserId(currentUser.getId());
        return puanService.detayHesapla(currentUser, yuvaKayitToplam);
    }
}
