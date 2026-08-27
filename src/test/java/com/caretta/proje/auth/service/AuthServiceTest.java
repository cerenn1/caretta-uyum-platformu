package com.caretta.proje.auth.service;

import com.caretta.proje.auth.dto.GoogleGirisRequest;
import com.caretta.proje.auth.dto.GoogleGirisResponse;
import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.common.exception.GecersizIstekException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private com.caretta.proje.auth.security.JwtService jwtService;
    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock
    private com.caretta.proje.otel.service.OtelService otelService;
    @Mock
    private com.caretta.proje.uyelik.service.UyelikService uyelikService;
    @Mock
    private GoogleTokenService googleTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void gecersizIdTokenGecersizIstekFirlatir() {
        when(googleTokenService.dogrulaVeEmailGetir(anyString()))
                .thenThrow(new GecersizIstekException("Gecersiz Google ID token"));

        GoogleGirisRequest request = new GoogleGirisRequest("uydurma-token", null, null, null);

        assertThatThrownBy(() -> authService.googleIleGiris(request))
                .isInstanceOf(GecersizIstekException.class);
    }

    @Test
    void mevcutKullaniciIcinDogrudanTokenDoner() {
        String email = "kayitli@ornek.com";
        when(googleTokenService.dogrulaVeEmailGetir("gecerli-token")).thenReturn(email);

        User user = User.builder().id(1L).email(email).password("hash").role(Rol.KULLANICI).build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        GoogleGirisRequest request = new GoogleGirisRequest("gecerli-token", null, null, null);
        GoogleGirisResponse response = authService.googleIleGiris(request);

        assertThat(response.yeniKullaniciMi()).isFalse();
        assertThat(response.authResponse().token()).isEqualTo("jwt-token");
        verify(userRepository, never()).save(any());
    }

    @Test
    void yeniKullaniciVeRolYokIseTekrarCagriIstenir() {
        String email = "yeni@ornek.com";
        when(googleTokenService.dogrulaVeEmailGetir("gecerli-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        GoogleGirisRequest request = new GoogleGirisRequest("gecerli-token", null, null, null);
        GoogleGirisResponse response = authService.googleIleGiris(request);

        assertThat(response.yeniKullaniciMi()).isTrue();
        assertThat(response.authResponse()).isNull();
        verify(userRepository, never()).save(any());
    }
}
