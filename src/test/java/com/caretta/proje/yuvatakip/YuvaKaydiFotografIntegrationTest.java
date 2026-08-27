package com.caretta.proje.yuvatakip;

import com.caretta.proje.yuvatakip.entity.YuvaDurumu;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Yuva kaydina OPSIYONEL fotograf yukleme ozelligi icin uctan uca testler.
 * Odak: fotografsiz kayit hala calisiyor mu (regresyon), gecerli/gecersiz
 * dosya tipiyle davranis dogru mu.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class YuvaKaydiFotografIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String kaydolVeTokenAl() throws Exception {
        String email = "yuva_fotograf_" + UUID.randomUUID() + "@example.com";
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

    @Test
    void fotografsizKayit_HalaBasariliCalisirVeFotografYoluNullDoner() throws Exception {
        String token = kaydolVeTokenAl();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/yuva-kayitlari")
                        .param("latitude", "36.85")
                        .param("longitude", "30.7")
                        .param("tarih", "2026-06-15")
                        .param("durum", YuvaDurumu.values()[0].name())
                        .param("notlar", "fotografsiz test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("fotografYolu").isNull()).isTrue();
    }

    @Test
    void gecerliJpgFotografla_BasariliCalisirVeFotografYoluDoner() throws Exception {
        String token = kaydolVeTokenAl();
        MockMultipartFile fotograf = new MockMultipartFile(
                "fotograf", "kayit.jpg", MediaType.IMAGE_JPEG_VALUE,
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/yuva-kayitlari")
                        .file(fotograf)
                        .param("latitude", "36.85")
                        .param("longitude", "30.7")
                        .param("tarih", "2026-06-15")
                        .param("durum", YuvaDurumu.values()[0].name())
                        .param("notlar", "fotografli test")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("fotografYolu").asText()).isNotBlank();
    }

    @Test
    void gecersizDosyaTipiyle_400Doner() throws Exception {
        String token = kaydolVeTokenAl();
        MockMultipartFile dosya = new MockMultipartFile(
                "fotograf", "not.txt", MediaType.TEXT_PLAIN_VALUE, "sadece metin".getBytes());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/yuva-kayitlari")
                        .file(dosya)
                        .param("latitude", "36.85")
                        .param("longitude", "30.7")
                        .param("tarih", "2026-06-15")
                        .param("durum", YuvaDurumu.values()[0].name())
                        .param("notlar", "gecersiz dosya testi")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }
}
