package com.caretta.proje.istatistik.dto;

import java.time.LocalDate;

/**
 * Fon basvurusu / ortaklik (Greenpeace, WWF vb.) icin disariya gosterilebilecek
 * TOPLU, KISISEL VERI ICERMEYEN istatistik ozeti. Kisi adi, e-posta, tam konum
 * gibi hicbir tekil veri buraya EKLENMEMELI - sadece sayilar ve genel ortalamalar.
 */
public record IstatistikResponse(
        long toplamYuvaKaydiSayisi,
        long toplamKatkidaBulunanKullaniciSayisi,
        long aktifOtelSayisi,
        Double ortalamaUyumOrani,
        LocalDate hesaplamaTarihi
) {
}
