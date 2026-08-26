package com.caretta.proje.auth;

import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auth akisi uctan uca testi: kayit -> giris -> token ile korumali endpoint.
 *
 * Bu test, mevcut lokal docker-compose Postgres veritabanina (localhost:5433) baglanir
 * (bkz. application-test.properties). Paylasilan bir veritabani oldugu icin @Transactional
 * kullanilir; her test metodu kendi islemini (transaction) acar ve sonunda otomatik
 * rollback edilir, boylece test verisi kalici olarak veritabaninda birikmez.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtelRepository otelRepository;

    private String uniqueEmail(String prefix) {
        return prefix + "_" + UUID.randomUUID() + "@example.com";
    }

    private String rastgeleTestDavetKodu() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private Otel otelOlustur(int satinAlinanKoltukSayisi) {
        return otelRepository.save(Otel.builder()
                .ad("Yonetici Testi Otel " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(satinAlinanKoltukSayisi)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
                .build());
    }

    private MvcResult kaydolIste(String rol, Otel otel) throws Exception {
        String email = uniqueEmail("koltuk");
        String body = """
                {"email":"%s","password":"password123","role":"%s","otelId":%d,"otelDavetKodu":"%s"}
                """.formatted(email, rol, otel.getId(), otel.getDavetKodu());

        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
    }

    @Test
    void kayitGirisVeKorumaliEndpointeErisim_basariliOlmali() throws Exception {
        String email = uniqueEmail("auth-flow");
        String password = "password123";

        // 1) Kayit ol -> 201 ve token donmeli
        String registerBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("KULLANICI"))
                .andReturn();

        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        assertThat(registerJson.get("token").asText()).isNotBlank();

        // 2) Giris yap -> 200 ve token donmeli
        String loginBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();
        assertThat(token).isNotBlank();

        // 3) Token ile korumali endpoint'e yuva kaydi ekle -> 201
        String yuvaKaydiBody = """
                {"latitude":36.85,"longitude":30.7,"tarih":"2026-08-18","durum":"AKTIF","notlar":"entegrasyon testi"}
                """;

        mockMvc.perform(post("/api/yuva-kayitlari")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(yuvaKaydiBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notlar").value("entegrasyon testi"));

        // 4) Eklenen kaydi listele -> eklenen kayit listede olmali
        mockMvc.perform(get("/api/yuva-kayitlari")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notlar").value("entegrasyon testi"));
    }

    @Test
    void korumaliEndpoint_tokenOlmadanCagrilirsaReddedilmeli() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/yuva-kayitlari"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).as("token olmadan korumali endpoint 401 ya da 403 donmeli").isIn(401, 403);
    }

    @Test
    void ayniEmailIleTekrarKayit_conflictDonmeli() throws Exception {
        String email = uniqueEmail("dup-check");
        String body = """
                {"email":"%s","password":"password123"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ---------- OTEL_YONETICISI kaydi + koltuk siniri (bkz. AuthService#register) ----------

    @Test
    void otelYoneticisiKaydi_otelIdVeDavetKoduIleBasariliOlur() throws Exception {
        Otel otel = otelOlustur(1);

        MvcResult result = kaydolIste("OTEL_YONETICISI", otel);

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("role").asText()).isEqualTo("OTEL_YONETICISI");
        assertThat(json.get("otelId").asLong()).isEqualTo(otel.getId());
    }

    @Test
    void koltukSiniriDoluOtelde_yeniOtelCalisaniKaydi400IleReddedilir() throws Exception {
        // Koltuk sinirini 1 yapiyoruz, ilk OTEL_CALISANI kaydiyla koltuk doluyor.
        Otel otel = otelOlustur(1);
        MvcResult ilkCalisanSonucu = kaydolIste("OTEL_CALISANI", otel);
        assertThat(ilkCalisanSonucu.getResponse().getStatus()).isEqualTo(201);

        MvcResult ikinciCalisanSonucu = kaydolIste("OTEL_CALISANI", otel);

        assertThat(ikinciCalisanSonucu.getResponse().getStatus())
                .as("koltuk siniri dolmus bir otelde ikinci OTEL_CALISANI kaydi 400 ile reddedilmeli")
                .isEqualTo(400);
        JsonNode json = objectMapper.readTree(ikinciCalisanSonucu.getResponse().getContentAsString());
        assertThat(json.get("message").asText()).contains("Koltuk siniri doldu");
    }

    @Test
    void koltukSiniriDoluOtelde_OtelYoneticisiKaydiKoltukSinirindanEtkilenmez() throws Exception {
        // Ayni senaryo: koltuk siniri 1, zaten 1 OTEL_CALISANI var. Ama OTEL_YONETICISI
        // kaydi koltuk sayilmadigi icin (UyelikService#kullanilanKoltukSayisi sadece
        // OTEL_CALISANI sayar) koltuk siniri dolu olsa da BASARILI olmali.
        Otel otel = otelOlustur(1);
        MvcResult calisanSonucu = kaydolIste("OTEL_CALISANI", otel);
        assertThat(calisanSonucu.getResponse().getStatus()).isEqualTo(201);

        MvcResult yoneticiSonucu = kaydolIste("OTEL_YONETICISI", otel);

        assertThat(yoneticiSonucu.getResponse().getStatus())
                .as("OTEL_YONETICISI kaydi koltuk sinirindan etkilenmemeli")
                .isEqualTo(201);
        JsonNode json = objectMapper.readTree(yoneticiSonucu.getResponse().getContentAsString());
        assertThat(json.get("role").asText()).isEqualTo("OTEL_YONETICISI");
    }
}
