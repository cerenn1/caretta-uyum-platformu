package com.caretta.proje.uyelik.controller;

import com.caretta.proje.uyelik.service.StripeOdemeServisi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeOdemeServisi stripeOdemeServisi;

    // DIKKAT: @RequestBody String payload HAM govdeyi (parse edilmemis) yakalar.
    // Stripe imza dogrulamasi (HMAC-SHA256) tam olarak gonderilen baytlar uzerinden
    // yapilir - govde herhangi bir sekilde (ör. bir DTO'ya) parse edilip tekrar
    // serialize edilirse imza uyusmaz. Bu endpoint kimlik dogrulamasi GEREKTIRMEZ
    // (bkz. SecurityConfig) - guvenligi Stripe-Signature basligindaki imza saglar.
    @PostMapping("/api/stripe/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                           @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeOdemeServisi.webhookIsle(payload, sigHeader);
        return ResponseEntity.ok("ok");
    }
}
