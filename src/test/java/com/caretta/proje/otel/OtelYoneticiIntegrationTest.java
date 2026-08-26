package com.caretta.proje.otel;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Otel yoneticisi paneli (calisan listeleme + aktif/pasif durum degistirme) ve
 * pasif yapilan bir kullanicinin ERISIMININ GERCEKTEN kesildiginin uctan uca testleri.
 *
 * Paylasilan dev veritabanini kullanir (bkz. application-test.properties); @Transactional
 * sayesinde her test sonunda rollback yapilir, kalici veri birikmez.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OtelYoneticiIntegrationTest {

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
                .ad("Yonetici Paneli Testi Otel A " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(10)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
                .build());

        otelB = otelRepository.save(Otel.builder()
                .ad("Yonetici Paneli Testi Otel B " + UUID.randomUUID())
                .latitude(37.0)
                .longitude(31.0)
                .davetKodu(rastgeleTestDavetKodu())
                .satinAlinanKoltukSayisi(10)
                .uyelikDurumu(UyelikDurumu.DENEME)
                .manuelPremiumMu(false)
                .build());
    }

    private String rastgeleTestDavetKodu() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record KayitSonucu(String email, String password, String token, Long userId) {
    }

    private KayitSonucu kaydol(String rol, Otel otel) throws Exception {
        String email = "otelYonetici_" + UUID.randomUUID() + "@example.com";
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
        String token = json.get("token").asText();
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        return new KayitSonucu(email, password, token, userId);
    }

    // ---------- GET /api/otel/{id}/calisanlar ----------

    @Test
    void calisanlariListeleme_tokensizIstek401Veya403Doner() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/otel/{id}/calisanlar", otelA.getId())).andReturn();

        assertThat(result.getResponse().getStatus())
                .as("token olmadan cagrilinca kimlik dogrulama gerekli hatasi donmeli")
                .isIn(401, 403);
    }

    @Test
    void calisanlariListeleme_otelCalisaniCagiramaz_403Doner() throws Exception {
        KayitSonucu calisan = kaydol("OTEL_CALISANI", otelA);

        mockMvc.perform(get("/api/otel/{id}/calisanlar", otelA.getId())
                        .header("Authorization", "Bearer " + calisan.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void calisanlariListeleme_baskaOtelinYoneticisi403Doner() throws Exception {
        KayitSonucu otelAYoneticisi = kaydol("OTEL_YONETICISI", otelA);

        mockMvc.perform(get("/api/otel/{id}/calisanlar", otelB.getId())
                        .header("Authorization", "Bearer " + otelAYoneticisi.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void calisanlariListeleme_kendiYoneticisiIcin200DonerVeSifreAlaniHicIcermez() throws Exception {
        KayitSonucu yonetici = kaydol("OTEL_YONETICISI", otelA);
        KayitSonucu calisan = kaydol("OTEL_CALISANI", otelA);

        MvcResult result = mockMvc.perform(get("/api/otel/{id}/calisanlar", otelA.getId())
                        .header("Authorization", "Bearer " + yonetici.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].aktif").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .as("calisan listesi sifre alanini KESINLIKLE icermemeli")
                .doesNotContain("password")
                .contains(calisan.email());
    }

    // ---------- PATCH /api/otel/{id}/calisanlar/{calisanId}/durum ----------

    @Test
    void calisanDurumuDegistir_otelCalisaniCagiramaz_403Doner() throws Exception {
        KayitSonucu calisan = kaydol("OTEL_CALISANI", otelA);

        mockMvc.perform(patch("/api/otel/{id}/calisanlar/{calisanId}/durum", otelA.getId(), calisan.userId())
                        .header("Authorization", "Bearer " + calisan.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aktif\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void calisanDurumuDegistir_baskaOtelinYoneticisiYanlisOtelPathiIle403Doner() throws Exception {
        KayitSonucu otelACalisani = kaydol("OTEL_CALISANI", otelA);
        KayitSonucu otelBYoneticisi = kaydol("OTEL_YONETICISI", otelB);

        // otelB yoneticisi, otelA'nin path'i (id=otelA) uzerinden istek atmaya calisiyor ->
        // yoneticiErisimYetkisiDogrula kendi oteli olmadigi icin 403 doner.
        mockMvc.perform(patch("/api/otel/{id}/calisanlar/{calisanId}/durum", otelA.getId(), otelACalisani.userId())
                        .header("Authorization", "Bearer " + otelBYoneticisi.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aktif\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void calisanDurumuDegistir_baskaOtelinCalisanIdsiKendiOtelPathiyleDenense404Doner() throws Exception {
        // Yatay yetki / IDOR kontrolunun KRITIK noktasi: otelB yoneticisi KENDI otelinin
        // path'ini (id=otelB) kullaniyor -> yoneticiErisimYetkisiDogrula GECER. Ama
        // calisanId aslinda otelA'ya ait - findByIdAndOtelIdAndRole (otelId=otelB ile)
        // bu id'yi BULAMAZ, 404 doner (403 DEGIL) - "var olan ama baska otele ait id"
        // ile "hic var olmayan id" arasindaki fark disariya sizmaz.
        KayitSonucu otelACalisani = kaydol("OTEL_CALISANI", otelA);
        KayitSonucu otelBYoneticisi = kaydol("OTEL_YONETICISI", otelB);

        mockMvc.perform(patch("/api/otel/{id}/calisanlar/{calisanId}/durum", otelB.getId(), otelACalisani.userId())
                        .header("Authorization", "Bearer " + otelBYoneticisi.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aktif\":false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void calisanDurumuDegistir_pasifYapilanCalisanSonrasindaGirisYapamaz_401Veya403Doner() throws Exception {
        KayitSonucu yonetici = kaydol("OTEL_YONETICISI", otelA);
        KayitSonucu calisan = kaydol("OTEL_CALISANI", otelA);

        mockMvc.perform(patch("/api/otel/{id}/calisanlar/{calisanId}/durum", otelA.getId(), calisan.userId())
                        .header("Authorization", "Bearer " + yonetici.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aktif\":false}"))
                .andExpect(status().isNoContent());

        String loginBody = """
                {"email":"%s","password":"%s"}
                """.formatted(calisan.email(), calisan.password());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn();

        assertThat(loginResult.getResponse().getStatus())
                .as("pasif yapilmis bir hesap ile giris denemesi reddedilmeli")
                .isIn(401, 403);
    }

    @Test
    void calisanDurumuDegistir_tekrarAktifYapilanCalisanTekrarGirisYapabilir() throws Exception {
        KayitSonucu yonetici = kaydol("OTEL_YONETICISI", otelA);
        KayitSonucu calisan = kaydol("OTEL_CALISANI", otelA);

        mockMvc.perform(patch("/api/otel/{id}/calisanlar/{calisanId}/durum", otelA.getId(), calisan.userId())
                        .header("Authorization", "Bearer " + yonetici.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aktif\":false}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/otel/{id}/calisanlar/{calisanId}/durum", otelA.getId(), calisan.userId())
                        .header("Authorization", "Bearer " + yonetici.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aktif\":true}"))
                .andExpect(status().isNoContent());

        String loginBody = """
                {"email":"%s","password":"%s"}
                """.formatted(calisan.email(), calisan.password());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());
    }

    // ---------- JwtAuthFilter: pasif yapilan kullanicinin ELINDEKI (suresi dolmamis) ----------
    // ---------- token'i da artik ise yaramamali (bkz. JwtAuthFilter#doFilterInternal)   ----------

    @Test
    void gecerliTokenAliniHalindekiKullaniciSonradanPasifYapilirsa_ayniTokenArtikCalismaz() throws Exception {
        // 1) Normal kayit + giris ile GECERLI bir JWT al (bu asamada kullanici aktif).
        String email = "pasif_jwt_" + UUID.randomUUID() + "@example.com";
        String password = "password123";
        String registerBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String token = registerJson.get("token").asText();

        // Token GECERLI oldugunu (suresi dolmamis, kullanici aktif) once dogrula.
        mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 2) Kullaniciyi DOGRUDAN repository ile pasif yap - PATCH endpoint'ini DEGIL,
        // JwtAuthFilter'in isEnabled() kontrolunu izole test etmek icin.
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setAktif(false);
        userRepository.save(user);

        // 3) AYNI (hala suresi dolmamis) token ile korumali endpoint'e tekrar istek at ->
        // JwtAuthFilter userDetails.isEnabled() false gordugu icin SecurityContext'e
        // authentication SET ETMEMELI, istek kimliksiz devam etmeli -> 401/403.
        MvcResult sonuc = mockMvc.perform(get("/api/panel-ozeti")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .as("pasif yapilan kullanicinin ELINDEKI suresi dolmamis token'i da artik gecersiz sayilmali")
                .isIn(401, 403);
    }
}
