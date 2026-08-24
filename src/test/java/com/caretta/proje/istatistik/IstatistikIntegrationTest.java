package com.caretta.proje.istatistik;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /api/istatistikler uctan uca testleri.
 *
 * En kritik kontroller: (1) hicbir token gonderilmeden 200 donmesi (permitAll
 * dogrulamasi - fon basvurusu/ortaklik gorusmelerinde disariya acik olmasi
 * gerekiyor), (2) cevap govdesinde e-posta/isim/tam konum gibi HICBIR kisisel
 * veya tekil alanin bulunmamasi (sadece agregat sayilar donmeli).
 *
 * Paylasilan dev veritabanini kullanir (bkz. application-test.properties); @Transactional
 * sayesinde her test sonunda rollback yapilir, kalici veri birikmez.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IstatistikIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtelRepository otelRepository;

    private Otel otel;

    @BeforeEach
    void setUp() {
        otel = otelRepository.save(Otel.builder()
                .ad("Istatistik Testi Otel " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .build());
    }

    private String rastgeleTestDavetKodu() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String normalKullaniciKaydolVeTokenAl() throws Exception {
        String email = "istatistik_kullanici_" + UUID.randomUUID() + "@example.com";
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

    private String otelCalisaniKaydolVeTokenAl(Otel otel) throws Exception {
        String email = "istatistik_calisan_" + UUID.randomUUID() + "@example.com";
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

    private void yuvaKaydiEkle(String token, String tarih) throws Exception {
        String body = """
                {"latitude":36.85,"longitude":30.7,"tarih":"%s","durum":"AKTIF","notlar":"istatistik testi"}
                """.formatted(tarih);

        mockMvc.perform(post("/api/yuva-kayitlari")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private void kapanisKanitiYukle(String token) throws Exception {
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile dosya = new MockMultipartFile(
                "fotograf", "kanit.png", MediaType.IMAGE_PNG_VALUE, pngBytes);

        mockMvc.perform(multipart("/api/kapanis-kaniti")
                        .file(dosya)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    void istatistikler_TokensizIstek200Doner() throws Exception {
        mockMvc.perform(get("/api/istatistikler"))
                .andExpect(status().isOk());
    }

    @Test
    void istatistikler_CevapGovdesindeKisiselVeriBulunmaz() throws Exception {
        String kullaniciToken = normalKullaniciKaydolVeTokenAl();
        yuvaKaydiEkle(kullaniciToken, "2026-08-10");

        String otelCalisaniToken = otelCalisaniKaydolVeTokenAl(otel);
        kapanisKanitiYukle(otelCalisaniToken);

        MvcResult result = mockMvc.perform(get("/api/istatistikler"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody)
                .as("agregat istatistik cevabinda e-posta, isim veya konum gibi kisisel/tekil veri bulunmamali")
                .doesNotContain("@example.com")
                .doesNotContain("email")
                .doesNotContain("latitude")
                .doesNotContain("longitude")
                .doesNotContain(otel.getAd())
                .doesNotContain("davetKodu");

        JsonNode json = objectMapper.readTree(responseBody);
        assertThat(json.get("toplamYuvaKaydiSayisi").asLong()).isGreaterThanOrEqualTo(1L);
        assertThat(json.get("toplamKatkidaBulunanKullaniciSayisi").asLong()).isGreaterThanOrEqualTo(1L);
        assertThat(json.get("aktifOtelSayisi").asLong()).isGreaterThanOrEqualTo(1L);
        assertThat(json.has("hesaplamaTarihi")).isTrue();
    }
}
