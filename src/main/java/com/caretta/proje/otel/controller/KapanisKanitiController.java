package com.caretta.proje.otel.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.otel.dto.KapanisKanitiResponse;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.service.KapanisKanitiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class KapanisKanitiController {

    private final KapanisKanitiService kapanisKanitiService;

    @PostMapping(value = "/api/kapanis-kaniti", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KapanisKanitiResponse> yukle(@RequestParam("fotograf") MultipartFile fotograf,
                                                         @AuthenticationPrincipal User currentUser) {
        KapanisKanitiResponse response = kapanisKanitiService.yukle(fotograf, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/otel/{id}/uyum-orani")
    public ResponseEntity<UyumOraniResponse> uyumOrani(@PathVariable("id") Long otelId) {
        return ResponseEntity.ok(kapanisKanitiService.uyumOraniHesapla(otelId));
    }
}
