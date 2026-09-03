package com.caretta.proje.uyelik.controller;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.uyelik.dto.KoltukSatinAlmaRequest;
import com.caretta.proje.uyelik.dto.KoltukSatinAlmaResponse;
import com.caretta.proje.uyelik.dto.UyelikDurumuResponse;
import com.caretta.proje.uyelik.service.UyelikService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UyelikController {

    private final UyelikService uyelikService;

    // Yatay yetki kontrolu (sadece kendi otelinin yoneticisi cagirabilir) servis
    // katmaninda yapilir (bkz. UyelikService#koltukSatinAlBaslat), burada sadece
    // rol kontrolu var - controller'da is mantigi yazilmaz.
    //
    // NOT: koltuk satin alma bir abonelik/faturalama islemidir, bu yuzden
    // OTEL_YONETICISI'ne aciktir (OTEL_CALISANI'ye DEGIL) - frontend'de de bu
    // form sadece yonetici paneline konulmustu (bkz. index.html
    // #koltuk-satin-alma-form, #yonetici-panel-section icinde). Onceden buraya
    // yanlislikla KapanisKanitiController'daki OTEL_CALISANI kontrolu kopyalanmisti;
    // bu, arayuzde butonu goren yoneticinin tikladiginda HER ZAMAN 403 almasina
    // (ve hicbir calisanin butonu hic gorememesine) yol aciyordu.
    @PreAuthorize("hasRole('OTEL_YONETICISI')")
    @PostMapping("/api/otel/{id}/koltuk-satin-alma")
    public ResponseEntity<KoltukSatinAlmaResponse> koltukSatinAl(@PathVariable("id") Long id,
                                                                  @Valid @RequestBody KoltukSatinAlmaRequest request,
                                                                  @AuthenticationPrincipal User currentUser) {
        KoltukSatinAlmaResponse response = uyelikService.koltukSatinAlBaslat(id, request.koltukSayisi(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/otel/{id}/uyelik-durumu")
    public ResponseEntity<UyelikDurumuResponse> uyelikDurumu(@PathVariable("id") Long id,
                                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(uyelikService.uyelikDurumuGetir(id, currentUser));
    }
}
