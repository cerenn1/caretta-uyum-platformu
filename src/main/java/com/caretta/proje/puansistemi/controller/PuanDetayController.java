package com.caretta.proje.puansistemi.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.puansistemi.service.PuanDetayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PuanDetayController {

    private final PuanDetayService puanDetayService;

    // Parametre YOK: butun veri @AuthenticationPrincipal ile token'dan gelen
    // kullaniciya gore turetilir, istemciden id kabul edilmez (bkz. PanelOzetiController).
    // Ekstra bir SecurityConfig kurali gerekmez - varsayilan .anyRequest().authenticated()
    // bu endpoint'i zaten korur.
    @GetMapping("/api/puan-detay")
    public ResponseEntity<PuanDetayResponse> detay(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(puanDetayService.detayGetir(currentUser));
    }
}
