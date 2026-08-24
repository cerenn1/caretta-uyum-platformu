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
}
