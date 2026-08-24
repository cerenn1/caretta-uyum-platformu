package com.caretta.proje.yuvatakip.entity;

import java.time.LocalDate;

public enum Mevsim {
    YUVALAMA_SEZONU,
    SEZON_DISI;

    public static Mevsim hesapla(LocalDate tarih) {
        int ay = tarih.getMonthValue();
        return (ay >= 5 && ay <= 9) ? YUVALAMA_SEZONU : SEZON_DISI;
    }
}
