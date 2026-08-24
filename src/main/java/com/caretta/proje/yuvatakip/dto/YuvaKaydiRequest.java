package com.caretta.proje.yuvatakip.dto;

import com.caretta.proje.yuvatakip.entity.YuvaDurumu;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record YuvaKaydiRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull LocalDate tarih,
        @NotNull YuvaDurumu durum,
        String notlar,

        // Opsiyonel: konum haritadan mi secildi (mobildeki "Haritadan Sec" ozelligi)
        // yoksa GPS/manuel mi girildi. Eski istemciler/web formu bu alani hic
        // gondermez, o durumda null gelir ve null = false gibi davranilir
        // (bkz. YuvaKaydiService.ekle - harita bonus puani).
        Boolean haritadanSecildiMi
) {
}
