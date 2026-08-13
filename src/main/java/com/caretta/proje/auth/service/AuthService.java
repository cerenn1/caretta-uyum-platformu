package com.caretta.proje.auth.service;

import com.caretta.proje.auth.dto.AuthResponse;
import com.caretta.proje.auth.dto.LoginRequest;
import com.caretta.proje.auth.dto.RegisterRequest;
import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.auth.security.JwtService;
import com.caretta.proje.common.exception.DuplicateResourceException;
import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.service.OtelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtelService otelService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Bu email adresi zaten kayitli: " + request.email());
        }

        Rol rol = request.role() != null ? request.role() : Rol.KULLANICI;
        Otel otel = null;

        if (rol == Rol.OTEL_CALISANI) {
            if (request.otelId() == null) {
                throw new GecersizIstekException("Otel calisani kaydi icin otelId zorunlu");
            }
            otel = otelService.getEntity(request.otelId());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(rol)
                .otel(otel)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole(), otel != null ? otel.getId() : null);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Kimlik dogrulama sonrasi kullanici bulunamadi"));

        String token = jwtService.generateToken(user);
        Long otelId = user.getOtel() != null ? user.getOtel().getId() : null;
        return new AuthResponse(token, user.getEmail(), user.getRole(), otelId);
    }
}
