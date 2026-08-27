package com.caretta.proje.yuvatakip.dto;

import com.caretta.proje.yuvatakip.entity.Mevsim;
import com.caretta.proje.yuvatakip.entity.YuvaDurumu;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Otel yoneticisinin, kendi otelinin bolgesindeki (7km) TUM kullanicilarin
 * girdigi yuva kayitlarini gorebildigi endpoint icin cevap DTO'su.
 *
 * GUVENLIK: kaydi giren kullaniciya ait HICBIR tanimlayici bilgi (email, id,
 * isim vb.) burada YOKTUR - kaydedenEtiketi her zaman sabit "Sahil Gönüllüsü"
 * degeridir, kim oldugu asla belli edilmez.
 */
public record BolgeselYuvaKaydiResponse(
        Long id,
        Double latitude,
        Double longitude,
        LocalDate tarih,
        YuvaDurumu durum,
        String notlar,
        LocalDateTime createdAt,
        Mevsim mevsim,
        String kaydedenEtiketi
) {
}
