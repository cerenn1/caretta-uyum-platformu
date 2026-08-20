package com.caretta.proje.common;

import java.time.ZoneId;

/**
 * Uygulama genelinde "bugun" ve tarih hesaplamalari icin kullanilacak TEK zaman dilimi.
 *
 * Neden: sunucunun calistigi makinenin (veya Docker container'inin) sistem varsayilan
 * saat dilimi ortama gore degisebilir (ozellikle bulut/CI ortaminda genelde UTC olur).
 * Uyum orani ve denetim raporu gibi hukuki/ticari sonucu olan hesaplamalarda
 * `LocalDate.now()` (sistem varsayilani) yerine burada tanimli sabit kullanilmali;
 * aksi halde ayni istek, sunucunun bulundugu bolgeye gore farkli "bugun" degeri uretebilir.
 */
public final class ZamanDilimi {

    public static final ZoneId TURKIYE = ZoneId.of("Europe/Istanbul");

    private ZamanDilimi() {
    }
}
