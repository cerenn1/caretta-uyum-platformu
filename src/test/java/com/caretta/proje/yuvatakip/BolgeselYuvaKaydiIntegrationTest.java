package com.caretta.proje.yuvatakip;

import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import com.caretta.proje.yuvatakip.entity.YuvaDurumu;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "Otel yoneticisi, kendi otelinin bolgesindeki (7km) TUM kullanicilarin yuva
 * kayitlarini gorebilir" ozelliginin ucdan uca guvenlik ve dogruluk testleri.
 *
 * En kritik test: dogru yoneticiye donen JSON'da kaydi giren kullanicinin
 * kimligine ait HICBIR IZ (email vb.) olmamali.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BolgeselYuvaKaydiIntegrationTest {

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
        otelA = otelRepository.save(Otel.builder()
                .ad("Bolgesel Test Otel A " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(10)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
                .build());

        otelB = otelRepository.save(Otel.builder()
                .ad("Bolgesel Test Otel B " + UUID.randomUUID())
                .latitude(37.5)
                .longitude(31.5)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(10)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
                .build());
    }

    private String rastgeleTestDavetKodu() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record KayitSonucu(String email, String password, String token) {
    }

    private KayitSonucu kaydolOtelli(String rol, Otel otel) throws Exception {
        String email = "bolgesel_" + UUID.randomUUID() + "@example.com";
        String password = "password123";
        String body = """
                {"email":"%s","password":"%s","role":"%s","otelId":%d,"otelDavetKodu":"%s"}
                """.formatted(email, password, rol, otel.getId(), otel.getDavetKodu());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new KayitSonucu(email, password, json.get("token").asText());
    }

    private KayitSonucu kaydolBasit() throws Exception {
        String email = "bolgesel_gozlemci_" + UUID.randomUUID() + "@example.com";
        String password = "password123";
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new KayitSonucu(email, password, json.get("token").asText());
    }

    private void yuvaKaydiEkle(String token, double lat, double lon) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/yuva-kayitlari")
                        .param("latitude", String.valueOf(lat))
                        .param("longitude", String.valueOf(lon))
                        .param("tarih", "2026-06-15")
                        .param("durum", YuvaDurumu.values()[0].name())
                        .param("notlar", "test notu")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    // ---------- Yatay yetki ----------

    @Test
    void tokensizIstek401Veya403Doner() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/otel/{id}/bolgesel-yuva-kayitlari", otelA.getId())).andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void otelCalisaniCagiramaz_403Doner() throws Exception {
        KayitSonucu calisan = kaydolOtelli("OTEL_CALISANI", otelA);

        mockMvc.perform(get("/api/otel/{id}/bolgesel-yuva-kayitlari", otelA.getId())
                        .header("Authorization", "Bearer " + calisan.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void baskaOtelinYoneticisi403Doner() throws Exception {
        KayitSonucu otelAYoneticisi = kaydolOtelli("OTEL_YONETICISI", otelA);

        mockMvc.perform(get("/api/otel/{id}/bolgesel-yuva-kayitlari", otelB.getId())
                        .header("Authorization", "Bearer " + otelAYoneticisi.token()))
                .andExpect(status().isForbidden());
    }

    // ---------- Yaricap + kimlik sizintisi ----------

    @Test
    void yakinKayitVarUzakKayitYok_veKimlikSizintisiYok() throws Exception {
        KayitSonucu yonetici = kaydolOtelli("OTEL_YONETICISI", otelA);
        KayitSonucu gozlemci = kaydolBasit();

        // otelA: 36.85, 30.7. Yakin kayit (~1km icinde), uzak kayit otelB civarinda (~100km+).
        yuvaKaydiEkle(gozlemci.token(), 36.855, 30.705);
        yuvaKaydiEkle(gozlemci.token(), otelB.getLatitude(), otelB.getLongitude());

        MvcResult result = mockMvc.perform(get("/api/otel/{id}/bolgesel-yuva-kayitlari", otelA.getId())
                        .header("Authorization", "Bearer " + yonetici.token()))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);

        assertThat(json.isArray()).isTrue();

        boolean yakinKayitVar = false;
        for (JsonNode node : json) {
            if (Math.abs(node.get("latitude").asDouble() - 36.855) < 0.0001) {
                yakinKayitVar = true;
            }
            assertThat(Math.abs(node.get("latitude").asDouble() - otelB.getLatitude())).isGreaterThan(0.01);
        }
        assertThat(yakinKayitVar).as("7km icindeki kayit listede olmali").isTrue();

        // EN KRITIK TEST: kaydi giren kullanicinin kimligine dair HICBIR iz olmamali.
        assertThat(responseBody)
                .as("kaydeden kullanicinin email'i, id'si veya ismi KESINLIKLE sizmamali")
                .doesNotContain(gozlemci.email())
                .doesNotContain("password")
                .doesNotContain("\"user\"")
                .doesNotContain("kullaniciId")
                .contains("Sahil Gönüllüsü");
    }
}
