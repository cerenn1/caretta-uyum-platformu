package com.caretta.proje.uyelik.dto;

import com.caretta.proje.uyelik.entity.UyelikDurumu;

public record UyelikDurumuResponse(
        Long otelId,
        Integer satinAlinanKoltukSayisi,
        long kullanilanKoltukSayisi,
        UyelikDurumu uyelikDurumu,
        boolean premiumMu,
        // GUVENLIK NOTU: bu alan sizinti degildir - endpoint zaten yatay yetki kontrolunden
        // (bkz. UyelikService#uyelikDurumuGetir -> otelErisimYetkisiDogrula) gectigi icin
        // SADECE o otelin kendi calisani/yoneticisi davet kodunu gorebilir.
        String davetKodu
) {
}
