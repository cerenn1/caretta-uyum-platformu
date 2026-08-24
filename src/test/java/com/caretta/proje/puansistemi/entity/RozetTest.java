package com.caretta.proje.puansistemi.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Birim testleri - Rozet.hesapla() sinir degerlerinde dogru rozeti donmeli.
 * Esikler: 5 -> BRONZ, 20 -> GUMUS, 50 -> ALTIN, esik altinda null.
 */
class RozetTest {

    @Test
    void hesapla_4KayitRozetYokDoner() {
        assertThat(Rozet.hesapla(4)).isNull();
    }

    @Test
    void hesapla_5KayitBronzDoner() {
        assertThat(Rozet.hesapla(5)).isEqualTo(Rozet.BRONZ);
    }

    @Test
    void hesapla_19KayitBronzDoner() {
        assertThat(Rozet.hesapla(19)).isEqualTo(Rozet.BRONZ);
    }

    @Test
    void hesapla_20KayitGumusDoner() {
        assertThat(Rozet.hesapla(20)).isEqualTo(Rozet.GUMUS);
    }

    @Test
    void hesapla_49KayitGumusDoner() {
        assertThat(Rozet.hesapla(49)).isEqualTo(Rozet.GUMUS);
    }

    @Test
    void hesapla_50KayitAltinDoner() {
        assertThat(Rozet.hesapla(50)).isEqualTo(Rozet.ALTIN);
    }

    @Test
    void sonraki_BronzdanGumuseGecer() {
        assertThat(Rozet.BRONZ.sonraki()).isEqualTo(Rozet.GUMUS);
    }

    @Test
    void sonraki_GumustenAltinaGecer() {
        assertThat(Rozet.GUMUS.sonraki()).isEqualTo(Rozet.ALTIN);
    }

    @Test
    void sonraki_AltinIcinNullDoner() {
        assertThat(Rozet.ALTIN.sonraki()).isNull();
    }

    @Test
    void esikVeOduAlanlariDogruDoner() {
        assertThat(Rozet.BRONZ.getEsikYuvaKayitSayisi()).isEqualTo(5);
        assertThat(Rozet.GUMUS.getEsikYuvaKayitSayisi()).isEqualTo(20);
        assertThat(Rozet.ALTIN.getEsikYuvaKayitSayisi()).isEqualTo(50);

        assertThat(Rozet.BRONZ.getOduAciklamasi()).isEqualTo("Partner otelde %5 indirim kodu");
        assertThat(Rozet.GUMUS.getOduAciklamasi()).isEqualTo("Partner otelde %10 indirim kodu");
        assertThat(Rozet.ALTIN.getOduAciklamasi()).isEqualTo("Partner otelde ücretsiz bir gecelik konaklama");
    }
}
