package com.caretta.proje.panel.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.panel.dto.PanelOzetiResponse;
import com.caretta.proje.panel.service.PanelOzetiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PanelOzetiController {

    private final PanelOzetiService panelOzetiService;

    // Parametre YOK: butun veri @AuthenticationPrincipal ile token'dan gelen
    // kullaniciya gore turetilir, istemciden id kabul edilmez.
    @GetMapping("/api/panel-ozeti")
    public ResponseEntity<PanelOzetiResponse> ozet(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(panelOzetiService.ozetGetir(currentUser));
    }
}
