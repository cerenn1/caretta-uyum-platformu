package com.caretta.proje.auth.dto;

import com.caretta.proje.auth.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Sifre en az 8 karakter olmali") String password,
        Rol role,
        Long otelId,
        String otelDavetKodu
) {
}
