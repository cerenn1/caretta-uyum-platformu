package com.caretta.proje.otel.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.otel.dto.CalisanDurumRequest;
import com.caretta.proje.otel.dto.CalisanResponse;
import com.caretta.proje.otel.service.OtelYoneticiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OtelYoneticiController {

    private final OtelYoneticiService otelYoneticiService;

    // Yatay yetki kontrolu (sadece kendi otelinin yoneticisi cagirabilir) servis
    // katmaninda yapilir (bkz. OtelYoneticiService#yoneticiErisimYetkisiDogrula),
    // burada sadece rol kontrolu var - controller'da is mantigi yazilmaz.
    @PreAuthorize("hasRole('OTEL_YONETICISI')")
    @GetMapping("/api/otel/{id}/calisanlar")
    public ResponseEntity<List<CalisanResponse>> calisanlariListele(@PathVariable("id") Long id,
                                                                      @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(otelYoneticiService.calisanlariListele(id, currentUser));
    }

    @PreAuthorize("hasRole('OTEL_YONETICISI')")
    @PatchMapping("/api/otel/{id}/calisanlar/{calisanId}/durum")
    public ResponseEntity<Void> calisanDurumunuDegistir(@PathVariable("id") Long id,
                                                          @PathVariable("calisanId") Long calisanId,
                                                          @Valid @RequestBody CalisanDurumRequest body,
                                                          @AuthenticationPrincipal User currentUser) {
        otelYoneticiService.calisanDurumunuDegistir(id, calisanId, body.aktif(), currentUser);
        return ResponseEntity.noContent().build();
    }
}
