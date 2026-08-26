package com.caretta.proje.uyelik.init;

import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Koltuk bazli uyelik ozelligi eklenmeden once olusturulmus oteller icin
 * satinAlinanKoltukSayisi/uyelikDurumu/manuelPremiumMu alanlari veritabaninda null
 * kalir. Uygulama her acilista bu otelleri bulup makul varsayilanlar atar:
 * - satinAlinanKoltukSayisi -> 1 (varsayilan "deneme" koltugu, herkes en az bir
 *   calisan hesabi olusturabilsin diye)
 * - uyelikDurumu -> DENEME
 * - manuelPremiumMu -> false
 *
 * DavetKoduBackfillRunner ile BIREBIR AYNI desen (bkz. common.init.DavetKoduBackfillRunner).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtelUyelikBackfillRunner implements ApplicationRunner {

    private final OtelRepository otelRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Otel> guncellenecekOteller = otelRepository.findAll().stream()
                .filter(otel -> otel.getSatinAlinanKoltukSayisi() == null
                        || otel.getUyelikDurumu() == null
                        || otel.getManuelPremiumMu() == null)
                .toList();

        if (guncellenecekOteller.isEmpty()) {
            return;
        }

        guncellenecekOteller.forEach(otel -> {
            if (otel.getSatinAlinanKoltukSayisi() == null) {
                otel.setSatinAlinanKoltukSayisi(Otel.VARSAYILAN_DENEME_KOLTUK_SAYISI);
            }
            if (otel.getUyelikDurumu() == null) {
                otel.setUyelikDurumu(UyelikDurumu.DENEME);
            }
            if (otel.getManuelPremiumMu() == null) {
                otel.setManuelPremiumMu(false);
            }
        });
        otelRepository.saveAll(guncellenecekOteller);

        log.info("Uyelik backfill tamamlandi: {} otele varsayilan uyelik alanlari atandi",
                guncellenecekOteller.size());
    }
}
