package com.caretta.proje.puansistemi.dto;

/**
 * Puan/rozet karti tiklaninca acilan detay ekrani icin cevap. Butun alanlar
 * token'daki kullanicidan turetilir (bkz. PuanDetayController), URL'de id yoktur.
 */
public record PuanDetayResponse(
        long toplamPuan,
        String rozet,                    // null ise henuz rozet yok
        long yuvaKayitToplam,
        String sonrakiRozet,              // null ise zaten ALTIN (en yuksek seviye)
        Long sonrakiRozeteKalanKayit,      // null ise zaten ALTIN
        String odulMesaji                 // rozet null ise null; doluysa GENEL (rol/rozet
                                           // seviyesi farketmeksizin ayni) - spesifik bir
                                           // vaatte (yuzde, tutar, indirim turu) BULUNMAZ
) {
}
