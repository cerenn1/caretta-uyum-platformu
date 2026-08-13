package com.caretta.proje.yuvatakip.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiRequest;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiResponse;
import com.caretta.proje.yuvatakip.service.YuvaKaydiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/yuva-kayitlari")
@RequiredArgsConstructor
public class YuvaKaydiController {

    private final YuvaKaydiService yuvaKaydiService;

    @PostMapping
    public ResponseEntity<YuvaKaydiResponse> ekle(@Valid @RequestBody YuvaKaydiRequest request,
                                                    @AuthenticationPrincipal User currentUser) {
        YuvaKaydiResponse response = yuvaKaydiService.ekle(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<YuvaKaydiResponse>> listele(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(yuvaKaydiService.listele(currentUser));
    }
}
