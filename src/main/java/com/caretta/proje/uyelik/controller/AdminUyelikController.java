package com.caretta.proje.uyelik.controller;

import com.caretta.proje.uyelik.dto.KapanisKanitiDoldurmaRequest;
import com.caretta.proje.uyelik.dto.KapanisKanitiDoldurmaResponse;
import com.caretta.proje.uyelik.dto.KoltukSayisiAyarlaRequest;
import com.caretta.proje.uyelik.dto.PremiumDurumRequest;
import com.caretta.proje.uyelik.service.UyelikService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * DEMO/SUNUM ARACI - odeme akisindan TAMAMEN bagimsiz, basit bir manuel premium
 * isaretleme endpoint'i (ör. sunumda odeme yapmadan premium gostermek icin).
 *
 * Bu GERCEK bir admin-rol/yetkilendirme sistemi DEGILDIR: projede henuz bir "admin
 * kullanici" kavrami yok, bu yuzden paylasilan bir gizli anahtar (X-Admin-Key,
 * .env'deki ADMIN_API_KEY ile karsilastirilir) ile korunuyor. Uretime gecmeden once
 * duzgun bir yetkilendirme sistemiyle (gercek ADMIN rolu + kullanici hesabi)
 * DEGISTIRILMELIDIR - bkz. UyelikService#premiumDurumAyarla.
 */
@RestController
@RequiredArgsConstructor
public class AdminUyelikController {

    private final UyelikService uyelikService;

    @PostMapping("/api/admin/otel/{id}/premium-durum")
    public ResponseEntity<Void> premiumDurumAyarla(@PathVariable("id") Long id,
                                                     @RequestBody PremiumDurumRequest body,
                                                     @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        uyelikService.premiumDurumAyarla(id, body.premium(), adminKey);
        return ResponseEntity.noContent().build();
    }

    /**
     * DEMO/SUNUM ARACI - premium-durum ile AYNI gerekce: Stripe koltuk satin alma
     * akisini BYPASS edip koltuk sayisini dogrudan ayarlar (bkz. UyelikService).
     */
    @PostMapping("/api/admin/otel/{id}/koltuk-sayisi")
    public ResponseEntity<Void> koltukSayisiAyarla(@PathVariable("id") Long id,
                                                     @Valid @RequestBody KoltukSayisiAyarlaRequest body,
                                                     @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        uyelikService.koltukSayisiAyarla(id, body.satinAlinanKoltukSayisi(), adminKey);
        return ResponseEntity.noContent().build();
    }

    /**
     * DEMO/SUNUM ARACI - gercek dosya YAZMADAN, gecmise donuk sabit placeholder'li
     * kapanis kaniti kayitlari olusturur (bkz. UyelikService#kapanisKanitiDoldur).
     */
    @PostMapping("/api/admin/otel/{id}/kapanis-kaniti-doldur")
    public ResponseEntity<KapanisKanitiDoldurmaResponse> kapanisKanitiDoldur(@PathVariable("id") Long id,
                                                                               @Valid @RequestBody KapanisKanitiDoldurmaRequest body,
                                                                               @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        return ResponseEntity.ok(uyelikService.kapanisKanitiDoldur(id, body.gunSayisi(), adminKey));
    }
}
