package com.caretta.proje.istatistik.dto;

/**
 * Platformdaki yuva kayitlarinin, en yakin resmi kumsala gore OTOMATIK
 * gruplanmis dagilimindaki tek bir satir. "Diger Bolge" kovasi icin
 * {@code il} alani null doner - hem 25km eslesme esiginin disinda kalan
 * kayitlar hem de k-anonimlik esiginin altinda kalip bastirilan kucuk
 * bolgeler bu kovaya toplanir (bkz. KapsamAlaniService).
 */
public record BolgeKaydiSayisi(String bolgeAdi, String il, long kayitSayisi) {
}
