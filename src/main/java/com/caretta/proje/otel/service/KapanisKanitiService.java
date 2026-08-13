package com.caretta.proje.otel.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.common.exception.DuplicateResourceException;
import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.common.exception.YetkisizErisimException;
import com.caretta.proje.otel.dto.KapanisKanitiResponse;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.entity.KapanisKaniti;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KapanisKanitiService {

    private static final Set<String> IZIN_VERILEN_TIPLER = Set.of("image/jpeg", "image/png");
    private static final long UYUM_ORANI_DONEM_GUN_SAYISI = 30;

    private final KapanisKanitiRepository kapanisKanitiRepository;
    private final FotografDepolamaServisi fotografDepolamaServisi;
    private final OtelService otelService;

    public KapanisKanitiResponse yukle(MultipartFile fotograf, User currentUser) {
        if (currentUser.getRole() != Rol.OTEL_CALISANI || currentUser.getOtel() == null) {
            throw new YetkisizErisimException("Sadece bir otele bagli otel calisanlari kapanis kaniti yukleyebilir");
        }

        if (fotograf == null || fotograf.isEmpty()) {
            throw new GecersizIstekException("Yuklenecek bir fotograf secmelisiniz");
        }

        String contentType = fotograf.getContentType();
        if (contentType == null || !IZIN_VERILEN_TIPLER.contains(contentType)) {
            throw new GecersizIstekException("Sadece JPG/PNG resim dosyalari kabul edilir");
        }

        Long otelId = currentUser.getOtel().getId();
        LocalDate bugun = LocalDate.now();

        if (kapanisKanitiRepository.existsByOtelIdAndTarih(otelId, bugun)) {
            throw new DuplicateResourceException("Bugun icin kapanis kaniti zaten yuklenmis");
        }

        String fotografYolu = fotografDepolamaServisi.kaydet(fotograf, otelId, bugun);

        KapanisKaniti kayit = KapanisKaniti.builder()
                .otel(currentUser.getOtel())
                .kullanici(currentUser)
                .tarih(bugun)
                .fotografYolu(fotografYolu)
                .build();

        kapanisKanitiRepository.save(kayit);
        return toResponse(kayit);
    }

    public UyumOraniResponse uyumOraniHesapla(Long otelId) {
        Otel otel = otelService.getEntity(otelId);

        LocalDate bugun = LocalDate.now();
        LocalDate baslangic = bugun.minusDays(UYUM_ORANI_DONEM_GUN_SAYISI - 1);

        long kanitliGun = kapanisKanitiRepository.countByOtelIdAndTarihBetween(otelId, baslangic, bugun);
        double uyumOrani = Math.round((kanitliGun / (double) UYUM_ORANI_DONEM_GUN_SAYISI) * 1000.0) / 10.0;

        return new UyumOraniResponse(
                otel.getId(),
                otel.getAd(),
                baslangic,
                bugun,
                UYUM_ORANI_DONEM_GUN_SAYISI,
                kanitliGun,
                uyumOrani
        );
    }

    private KapanisKanitiResponse toResponse(KapanisKaniti kayit) {
        return new KapanisKanitiResponse(
                kayit.getId(),
                kayit.getOtel().getId(),
                kayit.getTarih(),
                kayit.getFotografYolu(),
                kayit.getOlusturulmaZamani()
        );
    }
}
