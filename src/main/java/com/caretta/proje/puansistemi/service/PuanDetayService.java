package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.puansistemi.entity.Rozet;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * /api/puan-detay ve /api/katki-sertifikasi endpoint'lerinin is mantigi. yuvatakip
 * modulune bagimliligi PuanService disinda ayri bir servis olarak tutuyoruz ki
 * PuanService kendi modul sinirlari icinde kalsin (bkz. PanelOzetiService'teki ayni desen).
 */
@Service
@RequiredArgsConstructor
public class PuanDetayService {

    private final YuvaKaydiRepository yuvaKaydiRepository;
    private final PuanService puanService;
    private final KatkiSertifikasiPdfOlusturucu katkiSertifikasiPdfOlusturucu;

    public PuanDetayResponse detayGetir(User currentUser) {
        long yuvaKayitToplam = yuvaKaydiRepository.countByUserId(currentUser.getId());
        return puanService.detayHesapla(currentUser, yuvaKayitToplam);
    }

    /**
     * Herhangi bir rol (KULLANICI/OTEL_CALISANI farki yok) kendi katki sertifikasini
     * PDF olarak indirebilir. Sertifika ureticisi (KatkiSertifikasiPdfOlusturucu) hata
     * durumunda checked exception FIRLATMAZ - UyumRaporuPdfOlusturucu ile AYNI desen:
     * IOException/DocumentException ureticinin kendi icinde IllegalStateException'a
     * sarilir, bu yuzden burada da controller'da da ekstra bir throws/try-catch
     * gerekmez; GlobalExceptionHandler'daki genel Exception handler'i (500, stack
     * trace SIZDIRMADAN) zaten kapsar.
     */
    public byte[] sertifikaUret(User currentUser) {
        long yuvaKayitToplam = yuvaKaydiRepository.countByUserId(currentUser.getId());
        long toplamPuan = puanService.toplamPuanHesapla(currentUser.getId());
        Rozet rozet = Rozet.hesapla(yuvaKayitToplam);
        return katkiSertifikasiPdfOlusturucu.olustur(
                currentUser.getEmail(), yuvaKayitToplam, toplamPuan, rozet == null ? null : rozet.name());
    }
}
