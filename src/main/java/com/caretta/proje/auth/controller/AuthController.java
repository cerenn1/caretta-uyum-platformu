package com.caretta.proje.auth.controller;

import com.caretta.proje.auth.dto.AuthResponse;
import com.caretta.proje.auth.dto.GoogleGirisRequest;
import com.caretta.proje.auth.dto.GoogleGirisResponse;
import com.caretta.proje.auth.dto.LoginRequest;
import com.caretta.proje.auth.dto.RegisterRequest;
import com.caretta.proje.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<GoogleGirisResponse> googleIleGiris(@Valid @RequestBody GoogleGirisRequest request) {
        GoogleGirisResponse response = authService.googleIleGiris(request);
        return ResponseEntity.ok(response);
    }
}
