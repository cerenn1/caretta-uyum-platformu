package com.caretta.proje.raporlama.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.raporlama.dto.UyumRaporuSonucu;
import com.caretta.proje.raporlama.service.UyumRaporuService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class UyumRaporuController {

    private final UyumRaporuService uyumRaporuService;

    // Yetkilendirme /api/otel/{id}/uyum-orani ile birebir ayni: controller'da rol
    // kontrolu yok, is mantigi (kendi oteli mi?) servise devredilir.
    @GetMapping("/api/otel/{id}/uyum-raporu")
    public ResponseEntity<byte[]> uyumRaporu(
            @PathVariable("id") Long otelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baslangic,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bitis,
            @AuthenticationPrincipal User currentUser) {

        UyumRaporuSonucu sonuc = uyumRaporuService.raporUret(otelId, baslangic, bitis, currentUser);

        // Dosya adinda SADECE sayisal otelId ve tarih kullanilir; otel adi gibi
        // serbest metin KOYULMAZ (header injection / bilgi sizintisi onlemi).
        String dosyaAdi = "uyum-raporu-" + otelId + "-" + sonuc.donemBitis() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dosyaAdi + "\"")
                .body(sonuc.pdfBaytlari());
    }
}
