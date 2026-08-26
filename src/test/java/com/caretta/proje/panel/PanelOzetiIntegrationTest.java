package com.caretta.proje.panel;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Mobil ana panel ozeti (/api/panel-ozeti) uctan uca testleri.
 *
 * En kritik kontrol: OTEL_CALISANI olmayan bir kullanici icin otel/uyum ile ilgili
 * TUM alanlarin null donmesi (bkz. panelOzeti_NormalKullaniciIcinOtelAlanlariNullDoner).
 *
 * Paylasilan dev veritabanini kullanir (bkz. application-test.properties); @Transactional
 * sayesinde her test sonunda rollback yapilir, kalici veri birikmez.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PanelOzetiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtelRepository otelRepository;

    private Otel otelA;

    @BeforeEach
    void setUp() {
        // satinAlinanKoltukSayisi ELLE verilir (bkz. UyelikIntegrationTest#setUp ile AYNI
        // desen) - AuthService'teki koltuk siniri kontrolu icin gerekli, aksi halde null (=0)
        // kalir ve otel calisani kaydi HER ZAMAN reddedilir.
        otelA = otelRepository.save(Otel.builder()
                .ad("Panel Testi Otel A " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(10)
                .build());
    }

    private String rastgeleTestDavetKodu() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String otelCalisaniKaydolVeTokenAl(Otel otel) throws Exception {
        String email = "panel_calisan_" + UUID.randomUUID() + "@example.com";
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
        String email = "panel_kullanici_" + UUID.randomUUID() + "@example.com";
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

    private void kapanisKanitiYukle(String token) throws Exception {
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile dosya = new MockMultipartFile(
                "fotograf", "kanit.png", MediaType.IMAGE_PNG_VALUE, pngBytes);

        mockMvc.perform(multipart("/api/kapanis-kaniti")
                        .file(dosya)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    private void yuvaKaydiEkle(String token, String tarih, String durum) throws Exception {
        String body = """
                {"latitude":36.85,"longitude":30.7,"tarih":"%s","durum":"%s","notlar":"panel testi"}
                """.formatted(tarih, durum);

        mockMvc.perform(post("/api/yuva-kayitlari")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void panelOzeti_TokensizIstek401Veya403Donmeli() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/panel-ozeti")).andReturn();

        assertThat(result.getResponse().getStatus())
                .as("token olmadan panel ozeti 401 ya da 403 donmeli")
                .isIn(401, 403);
    }

    @Test
    void panelOzeti_HicYuvaKaydiOlmayanKullaniciIcinCokmedenSifirDoner() throws Exception {
        String token = normalKullaniciKaydolVeTokenAl();

        mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yuvaKayitToplam").value(0))
                .andExpect(jsonPath("$.sonYuvaKaydiTarih").doesNotExist())
                .andExpect(jsonPath("$.sonYuvaKaydiDurum").doesNotExist())
                .andExpect(jsonPath("$.toplamPuan").value(0))
                .andExpect(jsonPath("$.rozet").doesNotExist());
    }

    @Test
    void panelOzeti_NormalKullaniciIcinOtelAlanlariNullDoner() throws Exception {
        String token = normalKullaniciKaydolVeTokenAl();
        yuvaKaydiEkle(token, "2026-08-10", "AKTIF");
        yuvaKaydiEkle(token, "2026-08-15", "RISK_ALTINDA");

        mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("KULLANICI"))
                .andExpect(jsonPath("$.yuvaKayitToplam").value(2))
                .andExpect(jsonPath("$.sonYuvaKaydiTarih").value("2026-08-15"))
                .andExpect(jsonPath("$.sonYuvaKaydiDurum").value("RISK_ALTINDA"))
                // KRITIK: normal kullanicida otel/uyum ile ilgili HICBIR alan sizmamali.
                .andExpect(jsonPath("$.otelId").doesNotExist())
                .andExpect(jsonPath("$.otelAdi").doesNotExist())
                .andExpect(jsonPath("$.uyumOrani").doesNotExist())
                .andExpect(jsonPath("$.donemBaslangic").doesNotExist())
                .andExpect(jsonPath("$.donemBitis").doesNotExist())
                .andExpect(jsonPath("$.donemGunSayisi").doesNotExist())
                .andExpect(jsonPath("$.kanitYuklenenGunSayisi").doesNotExist())
                .andExpect(jsonPath("$.bugunKanitYuklendiMi").doesNotExist())
                // 2 yuva kaydi x 10 puan = 20; esik 5'in altinda oldugu icin rozet yok.
                .andExpect(jsonPath("$.toplamPuan").value(20))
                .andExpect(jsonPath("$.rozet").doesNotExist());
    }

    @Test
    void panelOzeti_BesYuvaKaydiSonrasiBronzRozetVeElliPuanDoner() throws Exception {
        String token = normalKullaniciKaydolVeTokenAl();
        for (int i = 1; i <= 5; i++) {
            yuvaKaydiEkle(token, "2026-08-0" + i, "AKTIF");
        }

        mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yuvaKayitToplam").value(5))
                .andExpect(jsonPath("$.toplamPuan").value(50))
                .andExpect(jsonPath("$.rozet").value("BRONZ"));
    }

    @Test
    void panelOzeti_HaritadanSecildiMiTrueIsePuanaBesEkBonusYansir() throws Exception {
        String token = normalKullaniciKaydolVeTokenAl();
        String body = """
                {"latitude":36.85,"longitude":30.7,"tarih":"2026-08-10","durum":"AKTIF","notlar":"harita testi","haritadanSecildiMi":true}
                """;

        mockMvc.perform(post("/api/yuva-kayitlari")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // 1 kayit: temel 10 + harita bonusu 5 = 15 (tek satirda degil, iki ayri islemin toplami).
                .andExpect(jsonPath("$.toplamPuan").value(15));
    }

    @Test
    void panelOzeti_YuvaKaydiIstegindeFazladanPuanAlaniGonderilirseYokSayilir() throws Exception {
        // GUVENLIK: istemci "puan" diye var olmayan bir alan gonderse bile bu alan
        // sessizce yok sayilir (Jackson bilinmeyen alanlari yoksayar), sunucu puani
        // kendi sabit degeriyle (10) hesaplar - istemciden puan enjekte edilemez.
        String token = normalKullaniciKaydolVeTokenAl();
        String body = """
                {"latitude":36.85,"longitude":30.7,"tarih":"2026-08-10","durum":"AKTIF","notlar":"guvenlik testi","puan":999999}
                """;

        mockMvc.perform(post("/api/yuva-kayitlari")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toplamPuan").value(10));
    }

    @Test
    void panelOzeti_OtelCalisaniIcinOtelAlanlariDoluGelirVeUyumOraniEndpointiIleAyni() throws Exception {
        String token = otelCalisaniKaydolVeTokenAl(otelA);
        kapanisKanitiYukle(token);

        MvcResult uyumOraniSonucu = mockMvc.perform(get("/api/otel/{id}/uyum-orani", otelA.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode uyumOraniJson = objectMapper.readTree(uyumOraniSonucu.getResponse().getContentAsString());

        MvcResult panelSonucu = mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OTEL_CALISANI"))
                .andExpect(jsonPath("$.otelId").value(otelA.getId()))
                .andExpect(jsonPath("$.otelAdi").value(otelA.getAd()))
                .andExpect(jsonPath("$.bugunKanitYuklendiMi").value(true))
                .andReturn();
        JsonNode panelJson = objectMapper.readTree(panelSonucu.getResponse().getContentAsString());

        assertThat(panelJson.get("uyumOrani").asDouble())
                .as("panel ozetindeki uyum orani /uyum-orani endpoint'i ile birebir ayni olmali")
                .isEqualTo(uyumOraniJson.get("uyumOrani").asDouble());
        assertThat(panelJson.get("donemGunSayisi").asLong())
                .isEqualTo(uyumOraniJson.get("donemGunSayisi").asLong());
        assertThat(panelJson.get("kanitYuklenenGunSayisi").asLong())
                .isEqualTo(uyumOraniJson.get("kanitYuklenenGunSayisi").asLong());
        assertThat(panelJson.get("donemBaslangic").asText())
                .isEqualTo(uyumOraniJson.get("donemBaslangic").asText());
        assertThat(panelJson.get("donemBitis").asText())
                .isEqualTo(uyumOraniJson.get("donemBitis").asText());
    }

    @Test
    void panelOzeti_OtelCalisaniAmaKanitYuklemedenBugunKanitYuklendiMiFalseDoner() throws Exception {
        String token = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otelId").value(otelA.getId()))
                .andExpect(jsonPath("$.bugunKanitYuklendiMi").value(false));
    }
}
