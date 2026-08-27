package com.caretta.proje.puansistemi.dto;

import java.util.List;

/**
 * /api/puan-siralamasi cevabi. kullanicininKendiSirasi - istek yapan kullanici ilk
 * 10'da degilse kendi sirasini/puanini/rozetini ayrica gosterir; ilk 10'daysa null.
 */
public record SiralamaResponse(List<SiralamaSatiri> ilkOnlar, SiralamaSatiri kullanicininKendiSirasi) {
}
