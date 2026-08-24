package com.caretta.proje.yuvatakip;

import com.caretta.proje.yuvatakip.entity.Mevsim;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Birim testleri - Mevsim.hesapla() sinir tarihlerinde dogru sonucu donmeli.
 * Yuvalama sezonu: Mayis (5) - Eylul (9) arasi, her iki ay dahil.
 */
class MevsimTest {

    @Test
    void hesapla_30NisanSezonDisiDoner() {
        assertThat(Mevsim.hesapla(LocalDate.of(2026, 4, 30))).isEqualTo(Mevsim.SEZON_DISI);
    }

    @Test
    void hesapla_1MayisYuvalamaSezonuDoner() {
        assertThat(Mevsim.hesapla(LocalDate.of(2026, 5, 1))).isEqualTo(Mevsim.YUVALAMA_SEZONU);
    }

    @Test
    void hesapla_30EylulYuvalamaSezonuDoner() {
        assertThat(Mevsim.hesapla(LocalDate.of(2026, 9, 30))).isEqualTo(Mevsim.YUVALAMA_SEZONU);
    }

    @Test
    void hesapla_1EkimSezonDisiDoner() {
        assertThat(Mevsim.hesapla(LocalDate.of(2026, 10, 1))).isEqualTo(Mevsim.SEZON_DISI);
    }
}
