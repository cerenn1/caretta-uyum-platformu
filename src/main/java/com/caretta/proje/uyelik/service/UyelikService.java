package com.caretta.proje.uyelik.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.common.exception.YetkisizErisimException;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.otel.service.KapanisKanitiService;
import com.caretta.proje.otel.service.OtelService;
import com.caretta.proje.uyelik.dto.KoltukSatinAlmaResponse;
import com.caretta.proje.uyelik.dto.UyelikDurumuResponse;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Koltuk bazli uyelik modulunun orkestrasyon servisi: yatay yetki kontrolu,
 * uyelik/premium durumu hesabi ve (demo amacli) admin premium isaretleme burada
 * toplanir. Stripe ile ilgili tum detaylar StripeOdemeServisi'ne birakilir.
 */
@Service
@RequiredArgsConstructor
public class UyelikService {

    private final UserRepository userRepository;
    private final OtelRepository otelRepository;
    private final OtelService otelService;
    private final StripeOdemeServisi stripeOdemeServisi;
    // Yatay yetki kontrolu icin YENIDEN YAZILMAZ - /uyum-orani ve /uyum-raporu
    // endpoint'lerinin de kullandigi TEK KAYNAK metoda (otelErisimYetkisiDogrula)
    // delege edilir (bkz. raporlama.service.UyumRaporuService ile ayni desen).
    private final KapanisKanitiService kapanisKanitiService;

    // GUVENLIK: bu demo amacli manuel isaretleme aracinin gercek bir admin-rol
    // sistemi OLMADIGI icin, .env'de tanimlanmamis/bos birakilmis bir anahtarla
    // ENDPOINT'IN KAZARA HERKESE ACIK KALMASINI onlemek amaciyla - konfigure
    // edilmemis anahtar HER ZAMAN reddedilir (bkz. adminAnahtariDogrula).
    @Value("${admin.api-key}")
    private String yapilandirilmisAdminAnahtari;

    public long kullanilanKoltukSayisi(Long otelId) {
        // OTEL_CALISANI rolundeki, bu otele bagli TUM kullanicilarin sayisi.
        // NOT: "aktif/pasif calisan" ayrimi henuz yok (SONRAKI gorevde eklenecek),
        // simdilik hepsi sayiliyor.
        return userRepository.countByOtelIdAndRole(otelId, Rol.OTEL_CALISANI);
    }

    public boolean premiumMu(Otel otel) {
        return Boolean.TRUE.equals(otel.getManuelPremiumMu())
                || otel.getUyelikDurumu() == UyelikDurumu.AKTIF;
    }

    public UyelikDurumuResponse durumGetir(Otel otel) {
        return new UyelikDurumuResponse(
                otel.getId(),
                otel.getSatinAlinanKoltukSayisi(),
                kullanilanKoltukSayisi(otel.getId()),
                otel.getUyelikDurumu(),
                premiumMu(otel)
        );
    }

    /**
     * POST /api/otel/{id}/koltuk-satin-alma. Yatay yetki: /uyum-orani ile BIREBIR
     * ayni kural - sadece kendi otelinin calisani cagirabilir.
     */
    public KoltukSatinAlmaResponse koltukSatinAlBaslat(Long otelId, int koltukSayisi, User currentUser) {
        kapanisKanitiService.otelErisimYetkisiDogrula(otelId, currentUser);
        Otel otel = otelService.getEntity(otelId);
        return stripeOdemeServisi.checkoutOturumuOlustur(otel, koltukSayisi);
    }

    /**
     * GET /api/otel/{id}/uyelik-durumu. Ayni yatay yetki kurali.
     */
    public UyelikDurumuResponse uyelikDurumuGetir(Long otelId, User currentUser) {
        kapanisKanitiService.otelErisimYetkisiDogrula(otelId, currentUser);
        Otel otel = otelService.getEntity(otelId);
        return durumGetir(otel);
    }

    /**
     * DEMO/SUNUM ARACI - odeme akisindan TAMAMEN bagimsiz, gercek bir admin-rol
     * sistemi DEGILDIR. Projede henuz bir "admin kullanici" kavrami yok; bunun yerine
     * paylasilan gizli anahtar (X-Admin-Key) ile korunur. Uretime gecmeden once duzgun
     * bir yetkilendirme sistemiyle (ör. ayri bir ADMIN rolu + gercek kullanici hesabi)
     * DEGISTIRILMELIDIR.
     */
    @Transactional
    public void premiumDurumAyarla(Long otelId, boolean premium, String verilenAdminAnahtari) {
        adminAnahtariDogrula(verilenAdminAnahtari);

        Otel otel = otelService.getEntity(otelId);
        otel.setManuelPremiumMu(premium);
        otelRepository.save(otel);
    }

    private void adminAnahtariDogrula(String verilenAnahtar) {
        if (yapilandirilmisAdminAnahtari == null || yapilandirilmisAdminAnahtari.isBlank()) {
            throw new YetkisizErisimException("Admin API anahtari yapilandirilmamis");
        }
        if (verilenAnahtar == null || verilenAnahtar.isBlank()) {
            throw new YetkisizErisimException("X-Admin-Key basligi gerekli");
        }

        // Zamanlama saldirisina (timing attack) karsi SABIT ZAMANLI karsilastirma -
        // String.equals kisa devre yapip anahtarin ilk kac karakterinin dogru oldugunu
        // yanit suresinden sizdirabilir, MessageDigest.isEqual bunu onler.
        boolean esit = MessageDigest.isEqual(
                verilenAnahtar.getBytes(StandardCharsets.UTF_8),
                yapilandirilmisAdminAnahtari.getBytes(StandardCharsets.UTF_8)
        );
        if (!esit) {
            throw new YetkisizErisimException("Gecersiz admin anahtari");
        }
    }
}
