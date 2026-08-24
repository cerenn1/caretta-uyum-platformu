package com.caretta.proje.istatistik.controller;

import com.caretta.proje.istatistik.dto.IstatistikResponse;
import com.caretta.proje.istatistik.service.IstatistikService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IstatistikController {

    private final IstatistikService istatistikService;

    // Parametre YOK: kisisel/tekil hicbir veri istenmiyor, tamamen agregat sayilar
    // donuyor. Yetkilendirme icin bkz. SecurityConfig - bu endpoint bilerek permitAll.
    @GetMapping("/api/istatistikler")
    public ResponseEntity<IstatistikResponse> getir() {
        return ResponseEntity.ok(istatistikService.hesapla());
    }
}
