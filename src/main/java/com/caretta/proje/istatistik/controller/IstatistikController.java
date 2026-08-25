package com.caretta.proje.istatistik.controller;

import com.caretta.proje.istatistik.dto.IstatistikResponse;
import com.caretta.proje.istatistik.dto.KapsamAlaniResponse;
import com.caretta.proje.istatistik.service.IstatistikService;
import com.caretta.proje.istatistik.service.KapsamAlaniService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IstatistikController {

    private final IstatistikService istatistikService;
    private final KapsamAlaniService kapsamAlaniService;

    // Parametre YOK: kisisel/tekil hicbir veri istenmiyor, tamamen agregat sayilar
    // donuyor. Yetkilendirme icin bkz. SecurityConfig - bu endpoint bilerek permitAll.
    @GetMapping("/api/istatistikler")
    public ResponseEntity<IstatistikResponse> getir() {
        return ResponseEntity.ok(istatistikService.hesapla());
    }

    // "Etkimiz / Kapsam Alanimiz" bolumu icin: resmi kumsal listesi + Mavi Bayrak
    // sabit verisi + platformun otomatik bolge dagilimi. Parametre YOK, kisisel
    // veri icermiyor - bu yuzden /api/istatistikler ile AYNI gerekceyle bilerek
    // permitAll (bkz. SecurityConfig).
    @GetMapping("/api/kapsam-alani")
    public ResponseEntity<KapsamAlaniResponse> kapsamAlani() {
        return ResponseEntity.ok(kapsamAlaniService.hesapla());
    }
}
