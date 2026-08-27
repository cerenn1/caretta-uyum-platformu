package com.caretta.proje.yuvatakip.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiRequest;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiResponse;
import com.caretta.proje.yuvatakip.entity.YuvaDurumu;
import com.caretta.proje.yuvatakip.service.YuvaKaydiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/yuva-kayitlari")
@RequiredArgsConstructor
public class YuvaKaydiController {

    private final YuvaKaydiService yuvaKaydiService;

    // multipart/form-data - fotograf OPSIYONEL. Onceki JSON (application/json) istekleri
    // artik desteklenmiyor; istemcilerin multipart form gonderecek sekilde guncellenmesi
    // gerekir (bkz. commit notlari).
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<YuvaKaydiResponse> ekle(@RequestParam Double latitude,
                                                    @RequestParam Double longitude,
                                                    @RequestParam LocalDate tarih,
                                                    @RequestParam YuvaDurumu durum,
                                                    @RequestParam(required = false) String notlar,
                                                    @RequestParam(required = false) Boolean haritadanSecildiMi,
                                                    @RequestParam(required = false) MultipartFile fotograf,
                                                    @AuthenticationPrincipal User currentUser) {
        YuvaKaydiRequest request = new YuvaKaydiRequest(latitude, longitude, tarih, durum, notlar, haritadanSecildiMi);
        YuvaKaydiResponse response = yuvaKaydiService.ekle(request, fotograf, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<YuvaKaydiResponse>> listele(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(yuvaKaydiService.listele(currentUser));
    }
}
