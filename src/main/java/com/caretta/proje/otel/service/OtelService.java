package com.caretta.proje.otel.service;

import com.caretta.proje.common.exception.ResourceNotFoundException;
import com.caretta.proje.otel.dto.OtelOlusturmaResponse;
import com.caretta.proje.otel.dto.OtelRequest;
import com.caretta.proje.otel.dto.OtelResponse;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OtelService {

    private final OtelRepository otelRepository;
    private final DavetKoduUretici davetKoduUretici;

    public OtelOlusturmaResponse ekle(OtelRequest request) {
        Otel otel = Otel.builder()
                .ad(request.ad())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .davetKodu(davetKoduUretici.uret())
                .build();

        otelRepository.save(otel);
        return new OtelOlusturmaResponse(otel.getId(), otel.getAd(), otel.getLatitude(), otel.getLongitude(), otel.getDavetKodu());
    }

    public List<OtelResponse> listele() {
        return otelRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Otel getEntity(Long id) {
        return otelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel bulunamadi: " + id));
    }

    private OtelResponse toResponse(Otel otel) {
        return new OtelResponse(otel.getId(), otel.getAd(), otel.getLatitude(), otel.getLongitude());
    }
}
