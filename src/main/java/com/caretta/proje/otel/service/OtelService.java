package com.caretta.proje.otel.service;

import com.caretta.proje.common.exception.ResourceNotFoundException;
import com.caretta.proje.otel.dto.OtelOlusturmaResponse;
import com.caretta.proje.otel.dto.OtelRequest;
import com.caretta.proje.otel.dto.OtelResponse;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
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
                // davetKodu ile AYNI mantik: yeni bir otel hicbir zaman bu alanlari null
                // birakmaz, backfill runner sadece OZELLIK EKLENMEDEN ONCE olusturulmus
                // gecmis kayitlar icindir (bkz. Otel#VARSAYILAN_DENEME_KOLTUK_SAYISI).
                .satinAlinanKoltukSayisi(Otel.VARSAYILAN_DENEME_KOLTUK_SAYISI)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
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
