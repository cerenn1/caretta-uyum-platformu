package com.caretta.proje.uyelik;

import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Koltuk bazli uyelik modulunun uctan uca yetkilendirme testleri. OtelYetkilendirmeIntegrationTest
 * ile AYNI desen: paylasilan dev veritabani + @Transactional rollback.
 *
 * Gercek Stripe API'sine baglanmayi GEREKTIREN akislar (basarili checkout olusturma)
 * BILEREC test EDILMEZ - kullanicinin henuz gercek bir Stripe test anahtari yok. Burada
 * test edilen tum senaryolar (yatay yetki, rol kontrolu, admin anahtari, webhook imzasi)
 * Stripe'a hic istek gitmeden, yetkilendirme asamasinda sonuclanir.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UyelikIntegrationTest {

    private static final String ADMIN_KEY = "test-ortami-admin-anahtari-1234567890"; // application-test.properties

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtelRepository otelRepository;

    private Otel otelA;
    private Otel otelB;

    @BeforeEach
    void setUp() {
        // NOT: dogrudan otelRepository.save(...) ile olusturuluyor, OtelService.ekle()
        // BYPASS ediliyor - bu yuzden davetKodu gibi uyelik alanlari da (normalde
        // OtelService.ekle() tarafindan doldurulur) burada ELLE verilmeli.
        otelA = otelRepository.save(Otel.builder()
                .ad("Uyelik Testi Otel A " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(Otel.VARSAYILAN_DENEME_KOLTUK_SAYISI)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
                .build());

        otelB = otelRepository.save(Otel.builder()
                .ad("Uyelik Testi Otel B " + UUID.randomUUID())
                .latitude(37.0)
                .longitude(31.0)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(Otel.VARSAYILAN_DENEME_KOLTUK_SAYISI)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
                .build());
    }

    private String rastgeleTestDavetKodu() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String otelCalisaniKaydolVeTokenAl(Otel otel) throws Exception {
        String email = "calisan_" + UUID.randomUUID() + "@example.com";
        String body = """
                {"email":"%s","password":"password123","role":"OTEL_CALISANI","otelId":%d,"otelDavetKodu":"%s"}
                """.formatted(email, otel.getId(), otel.getDavetKodu());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private String normalKullaniciKaydolVeTokenAl() throws Exception {
        String email = "kullanici_" + UUID.randomUUID() + "@example.com";
        String body = """
                {"email":"%s","password":"password123"}
                """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    // ---------- POST /api/otel/{id}/koltuk-satin-alma ----------

    @Test
    void koltukSatinAlma_baskaOtelinCalisaniIle403Doner() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(post("/api/otel/{id}/koltuk-satin-alma", otelB.getId())
                        .header("Authorization", "Bearer " + otelATokeni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"koltukSayisi\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void koltukSatinAlma_tokensizIstek401Veya403Doner() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/otel/{id}/koltuk-satin-alma", otelA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"koltukSayisi\":5}"))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("token olmadan cagrilinca kimlik dogrulama gerekli hatasi donmeli")
                .isIn(401, 403);
    }

    @Test
    void koltukSatinAlma_normalKullaniciCagiramaz_403Doner() throws Exception {
        String token = normalKullaniciKaydolVeTokenAl();

        mockMvc.perform(post("/api/otel/{id}/koltuk-satin-alma", otelA.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"koltukSayisi\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void koltukSatinAlma_gecersizKoltukSayisiIle400Doner() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(post("/api/otel/{id}/koltuk-satin-alma", otelA.getId())
                        .header("Authorization", "Bearer " + otelATokeni)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"koltukSayisi\":0}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /api/otel/{id}/uyelik-durumu ----------

    @Test
    void uyelikDurumu_baskaOtelinCalisaniIle403Doner() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(get("/api/otel/{id}/uyelik-durumu", otelB.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isForbidden());
    }

    @Test
    void uyelikDurumu_kendiOtelininCalisaniIle200Doner() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(get("/api/otel/{id}/uyelik-durumu", otelA.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otelId").value(otelA.getId()))
                .andExpect(jsonPath("$.uyelikDurumu").value("DENEME"))
                .andExpect(jsonPath("$.premiumMu").value(false));
    }

    // ---------- POST /api/admin/otel/{id}/premium-durum ----------

    @Test
    void adminPremiumDurum_eksikAnahtarla403Doner() throws Exception {
        mockMvc.perform(post("/api/admin/otel/{id}/premium-durum", otelA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"premium\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPremiumDurum_yanlisAnahtarla403Doner() throws Exception {
        mockMvc.perform(post("/api/admin/otel/{id}/premium-durum", otelA.getId())
                        .header("X-Admin-Key", "bu-kesinlikle-yanlis-bir-anahtar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"premium\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPremiumDurum_dogruAnahtarlaGuncellenirVeUyelikDurumundaGorunur() throws Exception {
        mockMvc.perform(post("/api/admin/otel/{id}/premium-durum", otelA.getId())
                        .header("X-Admin-Key", ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"premium\":true}"))
                .andExpect(status().isNoContent());

        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(get("/api/otel/{id}/uyelik-durumu", otelA.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.premiumMu").value(true));
    }

    @Test
    void adminPremiumDurum_JWTOlmadanDaErisilebilir_kimlikDogrulamaGerektirmez() throws Exception {
        // Bu endpoint BILINCLI olarak JWT gerektirmiyor - guvenligini X-Admin-Key sagliyor.
        mockMvc.perform(post("/api/admin/otel/{id}/premium-durum", otelA.getId())
                        .header("X-Admin-Key", ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"premium\":false}"))
                .andExpect(status().isNoContent());
    }

    // ---------- POST /api/stripe/webhook ----------

    @Test
    void stripeWebhook_gecersizImzaIle400Doner() throws Exception {
        String body = """
                {"id":"evt_test_gecersiz","object":"event","type":"checkout.session.completed","data":{"object":{"id":"cs_test_x","object":"checkout.session"}}}
                """;

        mockMvc.perform(post("/api/stripe/webhook")
                        .header("Stripe-Signature", "t=1700000000,v1=bu-kesinlikle-gecersiz-bir-imza")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
