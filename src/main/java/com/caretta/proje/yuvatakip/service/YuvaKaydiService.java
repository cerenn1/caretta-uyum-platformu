package com.caretta.proje.yuvatakip.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.service.PuanService;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiRequest;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiResponse;
import com.caretta.proje.yuvatakip.entity.Mevsim;
import com.caretta.proje.yuvatakip.entity.YuvaKaydi;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YuvaKaydiService {

    private final YuvaKaydiRepository yuvaKaydiRepository;
    private final PuanService puanService;

    // GUVENLIK: yuva kaydi + puan eklemeleri TEK transaction'da atomik olsun diye
    // @Transactional eklendi - aksi halde kayit basariyla kaydedilip puan eklemesi
    // (veya tam tersi) yarim kalirsa veri tutarsizligi olusabilirdi.
    @Transactional
    public YuvaKaydiResponse ekle(YuvaKaydiRequest request, User currentUser) {
        YuvaKaydi kayit = YuvaKaydi.builder()
                .user(currentUser)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .tarih(request.tarih())
                .durum(request.durum())
                .notlar(request.notlar())
                .build();

        yuvaKaydiRepository.save(kayit);

        // Puanlama - GUVENLIK: puan degerleri burada sabit olarak belirlenir, istemciden
        // gelen request'te "puan" diye bir alan yoktur/olsa da dikkate alinmaz. Harita
        // bonusu ayri bir satir olarak eklenir (10 + 5, tek satirda 15 degil) ki
        // "nasil kazanildigi" ayri ayri denetlenebilsin.
        puanService.puanEkle(currentUser, 10, "YUVA_KAYDI_EKLENDI");
        if (Boolean.TRUE.equals(request.haritadanSecildiMi())) {
            puanService.puanEkle(currentUser, 5, "HARITADAN_KONUM_SECILDI_BONUS");
        }

        return toResponse(kayit);
    }

    public List<YuvaKaydiResponse> listele(User currentUser) {
        return yuvaKaydiRepository.findByUserIdOrderByTarihDesc(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private YuvaKaydiResponse toResponse(YuvaKaydi kayit) {
        return new YuvaKaydiResponse(
                kayit.getId(),
                kayit.getLatitude(),
                kayit.getLongitude(),
                kayit.getTarih(),
                kayit.getDurum(),
                kayit.getNotlar(),
                kayit.getCreatedAt(),
                Mevsim.hesapla(kayit.getTarih())
        );
    }
}
