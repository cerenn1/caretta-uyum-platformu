package com.caretta.proje.yuvatakip.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.otel.service.FotografDepolamaServisi;
import com.caretta.proje.otel.service.OtelService;
import com.caretta.proje.otel.service.OtelYoneticiService;
import com.caretta.proje.puansistemi.service.PuanService;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiRequest;
import com.caretta.proje.yuvatakip.entity.YuvaDurumu;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Birim testleri - Spring context ayaga kaldirilmiyor, saf Mockito.
 * Odak: yuva kaydi eklenince puanlamanin dogru sekilde tetiklenmesi.
 * Harita bonusu ayri bir satir olmali (10 + 5, tek satirda 15 degil).
 */
@ExtendWith(MockitoExtension.class)
class YuvaKaydiServiceTest {

    @Mock
    private YuvaKaydiRepository yuvaKaydiRepository;

    @Mock
    private PuanService puanService;

    @Mock
    private OtelYoneticiService otelYoneticiService;

    @Mock
    private OtelService otelService;

    @Mock
    private FotografDepolamaServisi fotografDepolamaServisi;

    private YuvaKaydiService yuvaKaydiService;

    private User kullanici() {
        return User.builder()
                .id(1L)
                .email("kullanici@example.com")
                .password("hash")
                .role(Rol.KULLANICI)
                .build();
    }

    private YuvaKaydiRequest request(Boolean haritadanSecildiMi) {
        return new YuvaKaydiRequest(
                36.85, 30.7, LocalDate.of(2026, 8, 20), YuvaDurumu.AKTIF, "not", haritadanSecildiMi);
    }

    @Test
    void ekle_HaritaBonusuOlmadanSadeceTemelPuanEklenir() {
        yuvaKaydiService = new YuvaKaydiService(yuvaKaydiRepository, puanService, otelYoneticiService, otelService, fotografDepolamaServisi);
        User kullanici = kullanici();

        yuvaKaydiService.ekle(request(null), null, kullanici);

        verify(puanService).puanEkle(kullanici, 10, "YUVA_KAYDI_EKLENDI");
        verifyNoMoreInteractions(puanService);
    }

    @Test
    void ekle_HaritadanSecildiMiFalseIseSadeceTemelPuanEklenir() {
        yuvaKaydiService = new YuvaKaydiService(yuvaKaydiRepository, puanService, otelYoneticiService, otelService, fotografDepolamaServisi);
        User kullanici = kullanici();

        yuvaKaydiService.ekle(request(false), null, kullanici);

        verify(puanService).puanEkle(kullanici, 10, "YUVA_KAYDI_EKLENDI");
        verifyNoMoreInteractions(puanService);
    }

    @Test
    void ekle_HaritadanSecildiMiTrueIseHemTemelHemBonusPuaniEklenir() {
        yuvaKaydiService = new YuvaKaydiService(yuvaKaydiRepository, puanService, otelYoneticiService, otelService, fotografDepolamaServisi);
        User kullanici = kullanici();

        yuvaKaydiService.ekle(request(true), null, kullanici);

        verify(puanService).puanEkle(kullanici, 10, "YUVA_KAYDI_EKLENDI");
        verify(puanService).puanEkle(kullanici, 5, "HARITADAN_KONUM_SECILDI_BONUS");
        verifyNoMoreInteractions(puanService);
    }
}
