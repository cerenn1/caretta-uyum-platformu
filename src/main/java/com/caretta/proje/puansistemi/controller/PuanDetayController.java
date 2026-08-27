package com.caretta.proje.puansistemi.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.puansistemi.dto.SiralamaResponse;
import com.caretta.proje.puansistemi.service.PuanDetayService;
import com.caretta.proje.puansistemi.service.PuanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PuanDetayController {

    private final PuanDetayService puanDetayService;
    private final PuanService puanService;

    // Parametre YOK: butun veri @AuthenticationPrincipal ile token'dan gelen
    // kullaniciya gore turetilir, istemciden id kabul edilmez (bkz. PanelOzetiController).
    // Ekstra bir SecurityConfig kurali gerekmez - varsayilan .anyRequest().authenticated()
    // bu endpoint'i zaten korur.
    @GetMapping("/api/puan-detay")
    public ResponseEntity<PuanDetayResponse> detay(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(puanDetayService.detayGetir(currentUser));
    }

    // Herhangi bir rol (KULLANICI/OTEL_CALISANI farki yok) kendi katki sertifikasini
    // indirebilir. URL'de id YOK, dosya adinda da serbest metin (email vb.) YOK -
    // UyumRaporuController'daki ayni ilke (bkz. o dosyadaki yorum).
    @GetMapping("/api/katki-sertifikasi")
    public ResponseEntity<byte[]> sertifika(@AuthenticationPrincipal User currentUser) {
        byte[] pdf = puanDetayService.sertifikaUret(currentUser);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"katki-sertifikasi.pdf\"")
                .body(pdf);
    }

    // Liderlik tablosu - herhangi bir rol (KULLANICI/OTEL_CALISANI farki yok), TEK bir
    // birlesik siralamada yer alir. permitAll YOK - sadece giris yapmis kullanicilar
    // gorebilir, varsayilan .anyRequest().authenticated() zaten korur.
    @GetMapping("/api/puan-siralamasi")
    public ResponseEntity<SiralamaResponse> siralama(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(puanService.siralamaGetir(currentUser));
    }
}
