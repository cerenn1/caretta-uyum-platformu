package com.caretta.proje.auth.dto;

import com.caretta.proje.auth.entity.Rol;
import jakarta.validation.constraints.NotBlank;

public record GoogleGirisRequest(
        @NotBlank String idToken,
        Rol role,
        Long otelId,
        String otelDavetKodu
) {
}
