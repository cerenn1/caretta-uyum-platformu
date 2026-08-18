package com.caretta.proje.auth;

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

    private String uniqueEmail(String prefix) {
        return prefix + "_" + UUID.randomUUID() + "@example.com";
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
}
