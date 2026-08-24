package com.caretta.proje.puansistemi.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /api/puan-detay uctan uca testleri. Puan/rozet karti tiklaninca acilan detay
 * ekraninin cevabini dogrular (bkz. PanelOzetiIntegrationTest - ayni test stili).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PuanDetayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String normalKullaniciKaydolVeTokenAl() throws Exception {
        String email = "puan_detay_kullanici_" + UUID.randomUUID() + "@example.com";
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

    private void yuvaKaydiEkle(String token, String tarih) throws Exception {
        String body = """
                {"latitude":36.85,"longitude":30.7,"tarih":"%s","durum":"AKTIF","notlar":"puan detay testi"}
                """.formatted(tarih);

        mockMvc.perform(post("/api/yuva-kayitlari")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void puanDetay_TokensizIstek401Veya403Donmeli() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/puan-detay")).andReturn();

        assertThat(result.getResponse().getStatus())
                .as("token olmadan puan detay 401 ya da 403 donmeli")
                .isIn(401, 403);
    }

    @Test
    void puanDetay_HicYuvaKaydiOlmayanKullaniciIcinRozetYokVeBronzaKalanBesDoner() throws Exception {
        String token = normalKullaniciKaydolVeTokenAl();

        mockMvc.perform(get("/api/puan-detay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toplamPuan").value(0))
                .andExpect(jsonPath("$.rozet").doesNotExist())
                .andExpect(jsonPath("$.yuvaKayitToplam").value(0))
                .andExpect(jsonPath("$.sonrakiRozet").value("BRONZ"))
                .andExpect(jsonPath("$.sonrakiRozeteKalanKayit").value(5))
                .andExpect(jsonPath("$.mevcutRozetOdulu").doesNotExist())
                .andExpect(jsonPath("$.sonrakiRozetOdulu").value("Partner otelde %5 indirim kodu"))
                .andExpect(jsonPath("$.odulTeslimBilgisi").doesNotExist());
    }

    @Test
    void puanDetay_BesYuvaKaydiSonrasiBronzRozetVeSonrakiGumusDoner() throws Exception {
        String token = normalKullaniciKaydolVeTokenAl();
        for (int i = 1; i <= 5; i++) {
            yuvaKaydiEkle(token, "2026-08-0" + i);
        }

        mockMvc.perform(get("/api/puan-detay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toplamPuan").value(50))
                .andExpect(jsonPath("$.rozet").value("BRONZ"))
                .andExpect(jsonPath("$.yuvaKayitToplam").value(5))
                .andExpect(jsonPath("$.sonrakiRozet").value("GUMUS"))
                .andExpect(jsonPath("$.sonrakiRozeteKalanKayit").value(15))
                .andExpect(jsonPath("$.mevcutRozetOdulu").value("Partner otelde %5 indirim kodu"))
                .andExpect(jsonPath("$.sonrakiRozetOdulu").value("Partner otelde %10 indirim kodu"))
                .andExpect(jsonPath("$.odulTeslimBilgisi").isNotEmpty());
    }
}
