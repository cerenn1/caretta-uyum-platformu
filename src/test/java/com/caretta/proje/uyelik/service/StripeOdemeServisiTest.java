package com.caretta.proje.uyelik.service;

import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.uyelik.entity.KoltukSatinAlma;
import com.caretta.proje.uyelik.entity.SatinAlmaDurumu;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import com.caretta.proje.uyelik.repository.KoltukSatinAlmaRepository;
import com.stripe.Stripe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GERCEK Stripe API'sine hicbir istek atilmaz - bu testler tamamen offline calisir.
 * Webhook imza dogrulamasi (Webhook.constructEvent), Stripe'in kendi kutuphanesinin
 * beklendigi gibi HMAC-SHA256 tabanli "t=<timestamp>,v1=<imza>" formatini kullandigini
 * dogrulamak icin AYNI algoritma test icinde sentetik olarak uygulanir (bkz.
 * imzaUret). Boylece gercek bir Stripe hesabi/webhook-secret'i OLMADAN da imza
 * dogrulama mantiginin dogru/yanlis imzayi ayirt ettigi kanitlanir.
 */
@ExtendWith(MockitoExtension.class)
class StripeOdemeServisiTest {

    private static final String WEBHOOK_SECRET = "whsec_test_sadece_bu_test_icin_uydurulmus_gizli_anahtar";

    @Mock
    private KoltukSatinAlmaRepository koltukSatinAlmaRepository;

    @Mock
    private OtelRepository otelRepository;

    private StripeOdemeServisi stripeOdemeServisi;

    @BeforeEach
    void setUp() {
        stripeOdemeServisi = new StripeOdemeServisi(koltukSatinAlmaRepository, otelRepository);
        ReflectionTestUtils.setField(stripeOdemeServisi, "webhookSecret", WEBHOOK_SECRET);
    }

    private Otel otel(int mevcutKoltuk, UyelikDurumu durum) {
        return Otel.builder()
                .id(1L)
                .ad("Test Otel")
                .latitude(36.85)
                .longitude(30.7)
                .satinAlinanKoltukSayisi(mevcutKoltuk)
                .uyelikDurumu(durum)
                .build();
    }

    private KoltukSatinAlma satinAlma(String sessionId, SatinAlmaDurumu durum, Otel otel, int koltukSayisi) {
        return KoltukSatinAlma.builder()
                .id(99L)
                .otel(otel)
                .koltukSayisi(koltukSayisi)
                .stripeCheckoutSessionId(sessionId)
                .durum(durum)
                .build();
    }

    /**
     * Stripe'in genel webhook imzalama semasi: signed_payload = "{timestamp}.{payload}",
     * imza = hex(HMAC-SHA256(webhookSecret, signed_payload)), header = "t={timestamp},v1={imza}".
     */
    private String imzaliBaslikUret(String payload, String secret, long timestamp) {
        try {
            String signedPayload = timestamp + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmacBytes = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hmacBytes) {
                hex.append(String.format("%02x", b));
            }
            return "t=" + timestamp + ",v1=" + hex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String checkoutSessionCompletedPayload(String sessionId) {
        return """
                {
                  "id": "evt_test_1",
                  "object": "event",
                  "api_version": "%s",
                  "created": %d,
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "%s",
                      "object": "checkout.session"
                    }
                  }
                }
                """.formatted(Stripe.API_VERSION, Instant.now().getEpochSecond(), sessionId);
    }

    @Test
    void webhookIsle_gecerliImzaVeCheckoutSessionCompletedIleKoltukSayisiArtar() {
        String sessionId = "cs_test_abc123";
        String payload = checkoutSessionCompletedPayload(sessionId);
        String sigHeader = imzaliBaslikUret(payload, WEBHOOK_SECRET, Instant.now().getEpochSecond());

        Otel otel = otel(2, UyelikDurumu.DENEME);
        KoltukSatinAlma satinAlma = satinAlma(sessionId, SatinAlmaDurumu.BEKLIYOR, otel, 5);

        when(koltukSatinAlmaRepository.findByStripeCheckoutSessionId(sessionId))
                .thenReturn(Optional.of(satinAlma));

        stripeOdemeServisi.webhookIsle(payload, sigHeader);

        assertThat(satinAlma.getDurum()).isEqualTo(SatinAlmaDurumu.TAMAMLANDI);
        assertThat(satinAlma.getTamamlanmaZamani()).isNotNull();
        assertThat(otel.getSatinAlinanKoltukSayisi()).isEqualTo(7); // 2 (mevcut) + 5 (satin alinan)
        assertThat(otel.getUyelikDurumu()).isEqualTo(UyelikDurumu.AKTIF);

        verify(koltukSatinAlmaRepository).saveAndFlush(satinAlma);
        verify(otelRepository).save(otel);
    }

    @Test
    void webhookIsle_ayniSessionIkinciKezGelirseKoltukSayisiTekrarArtmaz_idempotent() {
        String sessionId = "cs_test_zaten_tamamlandi";
        String payload = checkoutSessionCompletedPayload(sessionId);
        String sigHeader = imzaliBaslikUret(payload, WEBHOOK_SECRET, Instant.now().getEpochSecond());

        Otel otel = otel(7, UyelikDurumu.AKTIF); // önceki webhook zaten islenmis, 7 koltuk var
        KoltukSatinAlma zatenTamamlanmisSatinAlma =
                satinAlma(sessionId, SatinAlmaDurumu.TAMAMLANDI, otel, 5);

        when(koltukSatinAlmaRepository.findByStripeCheckoutSessionId(sessionId))
                .thenReturn(Optional.of(zatenTamamlanmisSatinAlma));

        stripeOdemeServisi.webhookIsle(payload, sigHeader);

        assertThat(otel.getSatinAlinanKoltukSayisi())
                .as("zaten TAMAMLANDI olan bir satin alma tekrar islenmemeli, koltuk sayisi degismemeli")
                .isEqualTo(7);
        verify(otelRepository, never()).save(any());
        verify(koltukSatinAlmaRepository, never()).save(any());
    }

    @Test
    void webhookIsle_esZamanliWebhookOptimisticLockCelismesiIleKoltukSayisiArtmazVeHataFirlamaz() {
        // Bu senaryo, ayni webhook'un NEREDEYSE ESZAMANLI iki kez gelmesini simule eder:
        // baska bir istek (thread) bu kaydi TAM saveAndFlush() cagirdigimiz anda bizden
        // once TAMAMLANDI'ya cevirip commit etmis olsun - repository.saveAndFlush(...)
        // bu durumda Hibernate'in firlatacagi ObjectOptimisticLockingFailureException'i
        // yansitir. webhookIsle bu istisnayi yakalayip SESSIZCE (hata firlatmadan) donmeli
        // ve Otel guncellemesine HIC ULASMAMALI (otelRepository.save asla cagrilmamali).
        String sessionId = "cs_test_esz_race_condition";
        String payload = checkoutSessionCompletedPayload(sessionId);
        String sigHeader = imzaliBaslikUret(payload, WEBHOOK_SECRET, Instant.now().getEpochSecond());

        Otel otel = otel(2, UyelikDurumu.DENEME);
        KoltukSatinAlma satinAlma = satinAlma(sessionId, SatinAlmaDurumu.BEKLIYOR, otel, 5);

        when(koltukSatinAlmaRepository.findByStripeCheckoutSessionId(sessionId))
                .thenReturn(Optional.of(satinAlma));
        when(koltukSatinAlmaRepository.saveAndFlush(satinAlma))
                .thenThrow(new ObjectOptimisticLockingFailureException(KoltukSatinAlma.class, satinAlma.getId()));

        stripeOdemeServisi.webhookIsle(payload, sigHeader);

        assertThat(otel.getSatinAlinanKoltukSayisi())
                .as("optimistic lock celismesinde koltuk sayisi BIR KEZ bile artmamali")
                .isEqualTo(2);
        verify(otelRepository, never()).save(any());
    }

    @Test
    void webhookIsle_gecersizImzaGecersizIstekExceptionFirlatir() {
        String sessionId = "cs_test_gecersiz_imza";
        String payload = checkoutSessionCompletedPayload(sessionId);
        // Yanlis bir secret ile uretilmis imza - dogrulama BASARISIZ olmali.
        String yanlisImza = imzaliBaslikUret(payload, "yanlis-webhook-secret", Instant.now().getEpochSecond());

        assertThatThrownBy(() -> stripeOdemeServisi.webhookIsle(payload, yanlisImza))
                .isInstanceOf(GecersizIstekException.class);

        verify(koltukSatinAlmaRepository, never()).findByStripeCheckoutSessionId(any());
    }

    @Test
    void webhookIsle_ilgisizEventTuruSessizceYokSayilir() {
        String payload = """
                {
                  "id": "evt_test_2",
                  "object": "event",
                  "api_version": "%s",
                  "created": %d,
                  "type": "payment_intent.created",
                  "data": {
                    "object": {
                      "id": "pi_test_123",
                      "object": "payment_intent"
                    }
                  }
                }
                """.formatted(Stripe.API_VERSION, Instant.now().getEpochSecond());
        String sigHeader = imzaliBaslikUret(payload, WEBHOOK_SECRET, Instant.now().getEpochSecond());

        stripeOdemeServisi.webhookIsle(payload, sigHeader);

        verify(koltukSatinAlmaRepository, never()).findByStripeCheckoutSessionId(any());
    }

    @Test
    void webhookIsle_bilinmeyenSessionIdIcinHataFirlatmaz() {
        // Stripe'a HER ZAMAN 200 donulmeli - aksi halde Stripe ayni webhook'u tekrar
        // tekrar dener. Bilinmeyen bir session id icin sadece sessizce loglanip cikilir.
        String sessionId = "cs_test_bilinmeyen";
        String payload = checkoutSessionCompletedPayload(sessionId);
        String sigHeader = imzaliBaslikUret(payload, WEBHOOK_SECRET, Instant.now().getEpochSecond());

        when(koltukSatinAlmaRepository.findByStripeCheckoutSessionId(sessionId)).thenReturn(Optional.empty());

        stripeOdemeServisi.webhookIsle(payload, sigHeader);

        verify(otelRepository, never()).save(any());
    }
}
