package com.caretta.proje.auth.dto;

import com.caretta.proje.auth.entity.Rol;

public record AuthResponse(String token, String email, Rol role, Long otelId) {
}
