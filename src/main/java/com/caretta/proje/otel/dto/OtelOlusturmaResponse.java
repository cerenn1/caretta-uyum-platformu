package com.caretta.proje.otel.dto;

/**
 * SADECE otel olusturma (POST /api/oteller) cevabinda kullanilir. davetKodu bu kayitta
 * bir defaya mahsus gosterilir; herkese acik olan GET /api/oteller icin bu DTO KULLANILMAZ,
 * onun yerine kodsuz OtelResponse donulur.
 */
public record OtelOlusturmaResponse(
        Long id,
        String ad,
        Double latitude,
        Double longitude,
        String davetKodu
) {
}
