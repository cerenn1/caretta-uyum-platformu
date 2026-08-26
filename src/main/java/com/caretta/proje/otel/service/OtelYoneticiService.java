package com.caretta.proje.otel.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.common.exception.ResourceNotFoundException;
import com.caretta.proje.common.exception.YetkisizErisimException;
import com.caretta.proje.otel.dto.CalisanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Otel yoneticisinin kendi otelindeki OTEL_CALISANI hesaplarini yonetmesi
 * (listeleme, aktif/pasif yapma) icin orkestrasyon servisi. Yatay yetki mantigi
 * KapanisKanitiService#otelErisimYetkisiDogrula ile AYNI desen - tek fark rol
 * kontrolunun OTEL_YONETICISI olmasi.
 */
@Service
@RequiredArgsConstructor
public class OtelYoneticiService {

    private final UserRepository userRepository;

    /**
     * Cagiran kullanicinin gercekten bu otelin yoneticisi olup olmadigini dogrular.
     * Otel var mi diye BAKMADAN once cagrilmali; aksi halde "var olmayan otelId -> 404,
     * baska otelin id'si -> 403" farki saldirgana hangi otelId'lerin gercekte var
     * oldugunu sizdirir (bkz. KapanisKanitiService#otelErisimYetkisiDogrula).
     */
    public void yoneticiErisimYetkisiDogrula(Long otelId, User currentUser) {
        if (currentUser.getRole() != Rol.OTEL_YONETICISI
                || currentUser.getOtel() == null
                || !currentUser.getOtel().getId().equals(otelId)) {
            throw new YetkisizErisimException("Sadece kendi otelinizin yoneticisi bu islemi yapabilir");
        }
    }

    public List<CalisanResponse> calisanlariListele(Long otelId, User currentUser) {
        yoneticiErisimYetkisiDogrula(otelId, currentUser);
        return userRepository.findByOtelIdAndRoleOrderByCreatedAtDesc(otelId, Rol.OTEL_CALISANI)
                .stream().map(this::toCalisanResponse).toList();
    }

    @Transactional
    public void calisanDurumunuDegistir(Long otelId, Long calisanId, boolean yeniDurum, User currentUser) {
        yoneticiErisimYetkisiDogrula(otelId, currentUser);
        // otelId+role sorgunun KENDISINE dahil edilir - "var olan ama baska otele ait" bir
        // id ile "hic var olmayan" bir id arasindaki fark istemciye SIZMAZ, ikisi de ayni
        // ResourceNotFoundException ile sonuclanir.
        User calisan = userRepository.findByIdAndOtelIdAndRole(calisanId, otelId, Rol.OTEL_CALISANI)
                .orElseThrow(() -> new ResourceNotFoundException("Bu otele bagli boyle bir calisan bulunamadi"));
        calisan.setAktif(yeniDurum);
        userRepository.save(calisan);
    }

    private CalisanResponse toCalisanResponse(User user) {
        return new CalisanResponse(user.getId(), user.getEmail(), user.isEnabled(), user.getCreatedAt());
    }
}
