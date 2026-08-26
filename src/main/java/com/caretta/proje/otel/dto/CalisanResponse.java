package com.caretta.proje.otel.dto;

import java.time.LocalDateTime;

// DIKKAT: User entity'sinde password alani var ama buraya KESINLIKLE eklenmez -
// bu DTO otel yoneticisi panelinde calisan listesini donerken kullanilir.
public record CalisanResponse(
        Long id,
        String email,
        boolean aktif,
        LocalDateTime createdAt
) {
}
