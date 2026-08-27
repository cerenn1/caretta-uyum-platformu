package com.caretta.proje.auth.service;

import com.caretta.proje.common.exception.GecersizIstekException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Objects;

/**
 * Google ID token dogrulamasini AuthService'den ayirir - boylece testte
 * GoogleIdTokenVerifier'a bagli olmadan AuthService mock'lanabilir.
 */
@Service
public class GoogleTokenService {

    private final String clientId;
    private volatile GoogleIdTokenVerifier verifier;

    public GoogleTokenService(@Value("${google.client-id:}") String clientId) {
        this.clientId = clientId;
    }

    /**
     * Verilen ID token'i dogrular ve icindeki email'i doner. Gecersiz/sahte token,
     * bos client-id konfigurasyonu veya audience uyusmazliginda GecersizIstekException firlatir.
     */
    public String dogrulaVeEmailGetir(String idTokenStr) {
        if (clientId == null || clientId.isBlank()) {
            throw new GecersizIstekException("Google ile giris su an yapilandirilmamis");
        }
        GoogleIdToken idToken;
        try {
            idToken = getVerifier().verify(idTokenStr);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new GecersizIstekException("Gecersiz Google ID token");
        } catch (java.io.IOException e) {
            throw new GecersizIstekException("Google ID token dogrulanamadi");
        }

        if (idToken == null) {
            throw new GecersizIstekException("Gecersiz Google ID token");
        }

        String email = idToken.getPayload().getEmail();
        if (email == null || email.isBlank()) {
            throw new GecersizIstekException("Google hesabinda email bilgisi bulunamadi");
        }
        return email;
    }

    private GoogleIdTokenVerifier getVerifier() {
        GoogleIdTokenVerifier v = verifier;
        if (v == null) {
            synchronized (this) {
                v = verifier;
                if (v == null) {
                    v = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                            .setAudience(Collections.singletonList(clientId))
                            .build();
                    verifier = v;
                }
            }
        }
        return Objects.requireNonNull(v);
    }
}
