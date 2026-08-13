package com.caretta.proje.otel.controller;

import com.caretta.proje.otel.dto.OtelRequest;
import com.caretta.proje.otel.dto.OtelResponse;
import com.caretta.proje.otel.service.OtelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/oteller")
@RequiredArgsConstructor
public class OtelController {

    private final OtelService otelService;

    @PostMapping
    public ResponseEntity<OtelResponse> ekle(@Valid @RequestBody OtelRequest request) {
        OtelResponse response = otelService.ekle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OtelResponse>> listele() {
        return ResponseEntity.ok(otelService.listele());
    }
}
