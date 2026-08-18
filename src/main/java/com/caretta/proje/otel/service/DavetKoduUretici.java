package com.caretta.proje.otel.service;

import com.caretta.proje.otel.repository.OtelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Otel davet kodu uretir. Kod, otel calisani kaydinda kimlik dogrulama gorevi gordugu
 * icin java.util.Random DEGIL, kriptografik olarak guvenli SecureRandom kullanilir.
 *
 * Alfabede karisan karakterler (I/1/O/0) yok, kullanici kodu elle yazacak.
 */
@Component
@RequiredArgsConstructor
public class DavetKoduUretici {

    private static final String ALFABE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int KOD_UZUNLUGU = 8;
    private static final int MAKS_DENEME = 10;

    private final OtelRepository otelRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String uret() {
        for (int deneme = 0; deneme < MAKS_DENEME; deneme++) {
            String aday = rastgeleKod();
            if (!otelRepository.existsByDavetKodu(aday)) {
                return aday;
            }
        }
        throw new IllegalStateException("Benzersiz davet kodu uretilemedi, tekrar deneyin");
    }

    private String rastgeleKod() {
        StringBuilder sb = new StringBuilder(KOD_UZUNLUGU);
        for (int i = 0; i < KOD_UZUNLUGU; i++) {
            sb.append(ALFABE.charAt(secureRandom.nextInt(ALFABE.length())));
        }
        return sb.toString();
    }
}
