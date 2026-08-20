package com.caretta.proje.raporlama;

import com.caretta.proje.common.ZamanDilimi;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.otel.service.KapanisKanitiService;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Uyum raporu (PDF) endpoint'inin uctan uca testleri.
 *
 * Paylasilan dev veritabanini kullanir (bkz. application-test.properties); @Transactional
 * sayesinde her test sonunda rollback yapilir, kalici veri birikmez.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UyumRaporuIntegrationTest {

    private static final byte[] PDF_IMZASI = {'%', 'P', 'D', 'F'};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtelRepository otelRepository;

    @Autowired
    private KapanisKanitiService kapanisKanitiService;

    private Otel otelA;
    private Otel otelB;

    @BeforeEach
    void setUp() {
        otelA = otelRepository.save(Otel.builder()
                .ad("Rapor Testi Otel A " + UUID.randomUUID())
                .latitude(36.85)
                .longitude(30.7)
                .davetKodu(rastgeleTestDavetKodu())
                .build());

        otelB = otelRepository.save(Otel.builder()
                .ad("Rapor Testi Otel B " + UUID.randomUUID())
                .latitude(37.0)
                .longitude(31.0)
                .davetKodu(rastgeleTestDavetKodu())
                .build());
    }

    private String rastgeleTestDavetKodu() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String otelCalisaniKaydolVeTokenAl(Otel otel) throws Exception {
        String email = "rapor_calisan_" + UUID.randomUUID() + "@example.com";
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

    private void kapanisKanitiYukle(String token) throws Exception {
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        org.springframework.mock.web.MockMultipartFile dosya = new org.springframework.mock.web.MockMultipartFile(
                "fotograf", "kanit.png", MediaType.IMAGE_PNG_VALUE, pngBytes);

        mockMvc.perform(multipart("/api/kapanis-kaniti")
                        .file(dosya)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    void otelCalisaniBaskaOtelinUyumRaporunuIsteyemez_403Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelB.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isForbidden());
    }

    @Test
    void otelCalisaniKendiOtelininUyumRaporunuAlabilir_GecerliPdfDoner() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);

        MvcResult result = mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelA.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(result.getResponse().getHeader("Content-Disposition"))
                .contains("attachment")
                .contains("uyum-raporu-" + otelA.getId() + "-");

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertThat(pdfBytes.length).isGreaterThan(0);
        assertThat(java.util.Arrays.copyOf(pdfBytes, 4)).isEqualTo(PDF_IMZASI);
    }

    @Test
    void uyumRaporu_HicKanitiOlmayanOtelIcinCokmedenGecerliPdfUretir() throws Exception {
        // otelB icin BeforeEach'ten sonra hicbir kapanis kaniti yuklenmedi.
        String otelBTokeni = otelCalisaniKaydolVeTokenAl(otelB);

        MvcResult result = mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelB.getId())
                        .header("Authorization", "Bearer " + otelBTokeni))
                .andExpect(status().isOk())
                .andReturn();

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertThat(pdfBytes.length).isGreaterThan(0);
        assertThat(java.util.Arrays.copyOf(pdfBytes, 4)).isEqualTo(PDF_IMZASI);
    }

    @Test
    void uyumRaporu_SadeceBaslangicVerilirse400Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);
        LocalDate baslangic = LocalDate.now(ZamanDilimi.TURKIYE).minusDays(10);

        mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelA.getId())
                        .param("baslangic", baslangic.toString())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uyumRaporu_SadeceBitisVerilirse400Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);
        LocalDate bitis = LocalDate.now(ZamanDilimi.TURKIYE);

        mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelA.getId())
                        .param("bitis", bitis.toString())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uyumRaporu_BitisBaslangictanOnceyse400Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelA.getId())
                        .param("baslangic", bugun.toString())
                        .param("bitis", bugun.minusDays(5).toString())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uyumRaporu_BitisGelecekteyse400Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelA.getId())
                        .param("baslangic", bugun.minusDays(10).toString())
                        .param("bitis", bugun.plusDays(3).toString())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uyumRaporu_365GundenUzunAralik400Donmeli() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelA.getId())
                        .param("baslangic", bugun.minusDays(400).toString())
                        .param("bitis", bugun.toString())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uyumRaporuIcinKullanilanOrtakHesaplama_UyumOraniEndpointiIleAyniSonucuVerir() throws Exception {
        String otelATokeni = otelCalisaniKaydolVeTokenAl(otelA);
        kapanisKanitiYukle(otelATokeni);

        MvcResult uyumOraniSonucu = mockMvc.perform(get("/api/otel/{id}/uyum-orani", otelA.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(uyumOraniSonucu.getResponse().getContentAsString());
        double uyumOraniEndpoint = json.get("uyumOrani").asDouble();
        long kanitliGunEndpoint = json.get("kanitYuklenenGunSayisi").asLong();
        long donemGunEndpoint = json.get("donemGunSayisi").asLong();

        // /uyum-raporu parametresiz cagrildiginda ayni "son 30 gun" varsayilanini
        // kullanir; bu yuzden ayni araligi burada da hesaplayip DOGRUDAN ortak
        // hesaplama metodunu (KapanisKanitiService#donemUyumOraniHesapla) cagiriyoruz.
        // Bu metot, hem /uyum-orani hem de PDF raporunun kullandigi TEK KAYNAKTIR.
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);
        LocalDate baslangic = bugun.minusDays(KapanisKanitiService.UYUM_ORANI_DONEM_GUN_SAYISI - 1);
        UyumOraniResponse ortakHesap = kapanisKanitiService.donemUyumOraniHesapla(otelA.getId(), baslangic, bugun);

        assertThat(ortakHesap.uyumOrani()).isEqualTo(uyumOraniEndpoint);
        assertThat(ortakHesap.kanitYuklenenGunSayisi()).isEqualTo(kanitliGunEndpoint);
        assertThat(ortakHesap.donemGunSayisi()).isEqualTo(donemGunEndpoint);

        // Ve /uyum-raporu endpoint'i, ayni veriyle gercekten gecerli bir PDF uretebiliyor:
        MvcResult raporSonucu = mockMvc.perform(get("/api/otel/{id}/uyum-raporu", otelA.getId())
                        .header("Authorization", "Bearer " + otelATokeni))
                .andExpect(status().isOk())
                .andReturn();

        byte[] pdfBytes = raporSonucu.getResponse().getContentAsByteArray();
        assertThat(new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
