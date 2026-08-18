package com.caretta.proje.otel;

import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
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
 * En kritik yetkilendirme testi: bir otel calisaninin BASKA bir otelin uyum oranini
 * gormeye calismasi 403 ile reddedilmeli (yatay yetki / IDOR kontrolu).
 *
 * Paylasilan dev veritabanini kullanir (bkz. application-test.properties); @Transactional
 * sayesinde her test sonunda rollback yapilir, kalici veri birikmez.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OtelYetkilendirmeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtelRepository otelRepository;

    @Autowired
    private UserRepository userRepository;

    private Otel otelA;
    private Otel otelB;

    @BeforeEach
    void setUp() {
        otelA = otelRepository.save(Otel.builder()
                .ad("Yetki Testi Otel A " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .build());

        otelB = otelRepository.save(Otel.builder()
                .ad("Yetki Testi Otel B " + UUID.randomUUID())
                .latitude(37.0)
                .longitude(31.0)
                .davetKodu(rastgeleTestDavetKodu())
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

    @Test
    void otelCalisaniBaskaOtelinUyumOraniniGoremez_403Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(get("/api/otel/{id}/uyum-orani", otelB.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isForbidden());
    }

    @Test
    void otelCalisaniKendiOtelininUyumOraniniGorebilir_200Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(get("/api/otel/{id}/uyum-orani", otelA.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otelId").value(otelA.getId()));
    }

    @Test
    void oteleBagliOlmayanNormalKullaniciUyumOraniniGoremez_403Donmeli() throws Exception {
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
        String token = json.get("token").asText();

        mockMvc.perform(get("/api/otel/{id}/uyum-orani", otelA.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void normalKullaniciKapanisKanitiYukleyemez_403Donmeli() throws Exception {
        String email = "kullanici2_" + UUID.randomUUID() + "@example.com";
        String body = """
                {"email":"%s","password":"password123"}
                """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = json.get("token").asText();

        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        MvcResult uploadResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/kapanis-kaniti")
                                .file("fotograf", pngBytes)
                                .header("Authorization", "Bearer " + token))
                .andReturn();

        assertThat(uploadResult.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void otelCalisaniKaydinda_davetKoduEksikse400Donmeli() throws Exception {
        String email = "calisan_" + UUID.randomUUID() + "@example.com";
        String body = """
                {"email":"%s","password":"password123","role":"OTEL_CALISANI","otelId":%d}
                """.formatted(email, otelA.getId());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void otelCalisaniKaydinda_davetKoduYanlisIse400Donmeli() throws Exception {
        String email = "calisan_" + UUID.randomUUID() + "@example.com";
        String body = """
                {"email":"%s","password":"password123","role":"OTEL_CALISANI","otelId":%d,"otelDavetKodu":"YANLISKOD"}
                """.formatted(email, otelA.getId());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Otel id veya davet kodu hatali"));
    }

    @Test
    void otelCalisaniKaydinda_olmayanOtelIdIleAyniOrtakHataMesajiDonmeli() throws Exception {
        // Bilgi sizintisi onlemi: var olmayan otelId ile yanlis davet kodu AYNI mesaji dondurmeli,
        // yoksa saldirgan hangi otelId'lerin var oldugunu ayirt edebilir.
        String email = "calisan_" + UUID.randomUUID() + "@example.com";
        long olmayanOtelId = otelB.getId() + 1_000_000L;
        String body = """
                {"email":"%s","password":"password123","role":"OTEL_CALISANI","otelId":%d,"otelDavetKodu":"HERHANGIBIRKOD"}
                """.formatted(email, olmayanOtelId);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Otel id veya davet kodu hatali"));
    }

    @Test
    void otelCalisaniKaydinda_davetKoduBuyukKucukHarfDuyarsizVeTrimliKabulEdilir() throws Exception {
        String email = "calisan_" + UUID.randomUUID() + "@example.com";
        String kucukHarfliVeBosluklu = "  " + otelA.getDavetKodu().toLowerCase() + "  ";
        String body = """
                {"email":"%s","password":"password123","role":"OTEL_CALISANI","otelId":%d,"otelDavetKodu":"%s"}
                """.formatted(email, otelA.getId(), kucukHarfliVeBosluklu);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void otelCalisaniKaydinda_dogruKoduIleKayitBasariliOlurVeKullaniciVeritabaninaYazilir() throws Exception {
        String email = "calisan_" + UUID.randomUUID() + "@example.com";
        String body = """
                {"email":"%s","password":"password123","role":"OTEL_CALISANI","otelId":%d,"otelDavetKodu":"%s"}
                """.formatted(email, otelA.getId(), otelA.getDavetKodu());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.role").value("OTEL_CALISANI"))
                .andExpect(jsonPath("$.otelId").value(otelA.getId()));

        assertThat(userRepository.existsByEmail(email))
                .as("dogru davet kodu ile kayit sonrasi kullanici veritabaninda olusmus olmali")
                .isTrue();
    }

    @Test
    void otelCalisaniKaydinda_yanlisKoduIleKullaniciVeritabaninaKaydedilmez() throws Exception {
        String email = "calisan_" + UUID.randomUUID() + "@example.com";
        String body = """
                {"email":"%s","password":"password123","role":"OTEL_CALISANI","otelId":%d,"otelDavetKodu":"YANLISKOD"}
                """.formatted(email, otelA.getId());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.existsByEmail(email))
                .as("yanlis davet kodu ile basarisiz kayit denemesi kullanici olusturmamali")
                .isFalse();
    }

    @Test
    void otellerListeleme_davetKoduAlaniCevaptaHicGorunmez() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/oteller"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody)
                .as("GET /api/oteller herkese acik oldugu icin davetKodu alanini HICBIR sekilde icermemeli")
                .doesNotContain("davetKodu")
                .doesNotContain(otelA.getDavetKodu())
                .doesNotContain(otelB.getDavetKodu());
    }
}
