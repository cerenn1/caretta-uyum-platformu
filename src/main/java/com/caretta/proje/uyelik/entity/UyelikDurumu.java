package com.caretta.proje.uyelik.entity;

/**
 * Bir otelin koltuk bazli uyelik durumu. DENEME: hic koltuk satin alinmamis veya
 * odeme henuz tamamlanmamis oteller icin varsayilan durum. AKTIF: en az bir koltuk
 * satin alma islemi webhook ile TAMAMLANDI olarak isaretlenmis. PASIF: SONRAKI bir
 * fazda (abonelik iptali/suresi dolmasi) kullanilmak uzere simdiden tanimlanan, ama
 * bu gorevde herhangi bir akis tarafindan atanmayan durum.
 */
public enum UyelikDurumu {
    DENEME,
    AKTIF,
    PASIF
}
