package com.caretta.proje.auth.service;

import com.caretta.proje.auth.dto.AuthResponse;
import com.caretta.proje.auth.dto.GoogleGirisRequest;
import com.caretta.proje.auth.dto.GoogleGirisResponse;
import com.caretta.proje.auth.dto.LoginRequest;
import com.caretta.proje.auth.dto.RegisterRequest;
import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.auth.security.JwtService;
import com.caretta.proje.common.exception.DuplicateResourceException;
import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.common.exception.ResourceNotFoundException;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.service.OtelService;
import com.caretta.proje.uyelik.service.UyelikService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtelService otelService;
    private final UyelikService uyelikService;
    private final GoogleTokenService googleTokenService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Bu email adresi zaten kayitli: " + request.email());
        }

        Rol rol = request.role() != null ? request.role() : Rol.KULLANICI;
        Otel otel = rolVeOteliDogrula(rol, request.otelId(), request.otelDavetKodu());

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

    /**
     * Google Identity Services'ten alinan ID token'i dogrular. Kullanici zaten
     * kayitliysa direkt JWT doner. Yeni kullaniciysa ve role henuz gonderilmediyse
     * client'in rol secim formu gostermesi icin authResponse=null doner - role
     * gonderildiginde register() ile AYNI rol/otel/koltuk dogrulamasi uygulanir ve
     * kullanici, hic kullanilmayacak rastgele bir sifreyle olusturulur (yalniz Google
     * ile giris yapabilecek).
     */
    public GoogleGirisResponse googleIleGiris(GoogleGirisRequest request) {
        String email = googleTokenService.dogrulaVeEmailGetir(request.idToken());

        return userRepository.findByEmail(email)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    Long otelId = user.getOtel() != null ? user.getOtel().getId() : null;
                    AuthResponse authResponse = new AuthResponse(token, user.getEmail(), user.getRole(), otelId);
                    return new GoogleGirisResponse(false, email, authResponse);
                })
                .orElseGet(() -> {
                    if (request.role() == null) {
                        return new GoogleGirisResponse(true, email, null);
                    }

                    Otel otel = rolVeOteliDogrula(request.role(), request.otelId(), request.otelDavetKodu());

                    User user = User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(rastgeleSifreUret()))
                            .role(request.role())
                            .otel(otel)
                            .build();

                    userRepository.save(user);

                    String token = jwtService.generateToken(user);
                    AuthResponse authResponse = new AuthResponse(token, user.getEmail(), user.getRole(),
                            otel != null ? otel.getId() : null);
                    return new GoogleGirisResponse(false, email, authResponse);
                });
    }

    private String rastgeleSifreUret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * register() ve googleIleGiris() TARAFINDAN ORTAK kullanilir - rol OTEL_CALISANI/
     * OTEL_YONETICISI ise otelId+davet kodu dogrulanir, OTEL_CALISANI icin ayrica
     * koltuk siniri zorlanir. KULLANICI rolunde otel yok, null doner.
     */
    private Otel rolVeOteliDogrula(Rol rol, Long otelId, String otelDavetKodu) {
        if (rol != Rol.OTEL_CALISANI && rol != Rol.OTEL_YONETICISI) {
            return null;
        }

        if (otelId == null) {
            throw new GecersizIstekException("Otel calisani/yoneticisi kaydi icin otelId zorunlu");
        }
        if (otelDavetKodu == null || otelDavetKodu.isBlank()) {
            throw new GecersizIstekException("Otel calisani/yoneticisi kaydi icin davet kodu zorunlu");
        }
        Otel otel = dogrulanmisOteliGetir(otelId, otelDavetKodu);

        // Koltuk siniri SADECE OTEL_CALISANI kaydinda zorlanir - yoneticiler koltuktan
        // SAYILMAZ (UyelikService#kullanilanKoltukSayisi zaten sadece OTEL_CALISANI
        // rolundekileri sayiyor), bu yuzden yonetici kaydi bu sinirdan HIC etkilenmez.
        if (rol == Rol.OTEL_CALISANI) {
            long kullanilan = uyelikService.kullanilanKoltukSayisi(otel.getId());
            int satinAlinan = otel.getSatinAlinanKoltukSayisi() != null ? otel.getSatinAlinanKoltukSayisi() : 0;
            if (kullanilan >= satinAlinan) {
                throw new GecersizIstekException(
                        "Koltuk siniri doldu (" + kullanilan + "/" + satinAlinan + "). Yeni calisan eklemek icin otel yoneticisi koltuk satin almali.");
            }
        }

        return otel;
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

    /**
     * Otel id + davet kodu ciftini dogrular. Guvenlik geregi (bilgi sizintisini onlemek icin)
     * otel bulunamadi, kod eslesmedi veya otelin henuz kodu yoksa (backfill calismamis olabilir)
     * HEPSI icin AYNI hata firlatilir - aksi halde saldirgan hangi otelId'lerin var oldugunu
     * kod deneyerek anlayabilir.
     */
    private Otel dogrulanmisOteliGetir(Long otelId, String girilenKod) {
        Otel otel;
        try {
            otel = otelService.getEntity(otelId);
        } catch (ResourceNotFoundException e) {
            throw new GecersizIstekException("Otel id veya davet kodu hatali");
        }

        String beklenenKod = otel.getDavetKodu();
        if (beklenenKod == null || !beklenenKod.equalsIgnoreCase(girilenKod.trim())) {
            throw new GecersizIstekException("Otel id veya davet kodu hatali");
        }

        return otel;
    }
}
