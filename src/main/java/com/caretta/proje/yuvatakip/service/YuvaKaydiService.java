package com.caretta.proje.yuvatakip.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiRequest;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiResponse;
import com.caretta.proje.yuvatakip.entity.YuvaKaydi;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YuvaKaydiService {

    private final YuvaKaydiRepository yuvaKaydiRepository;

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
                kayit.getCreatedAt()
        );
    }
}
