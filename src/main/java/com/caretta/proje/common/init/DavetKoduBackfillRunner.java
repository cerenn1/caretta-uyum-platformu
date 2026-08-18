package com.caretta.proje.common.init;

import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.otel.service.DavetKoduUretici;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Davet kodu ozelligi eklenmeden once olusturulmus oteller icin davetKodu alani
 * veritabaninda null kalir. Uygulama her acilista bu oteli bulup birer kod atar,
 * boylece eski oteller de "sadece davet koduyla otel calisani kaydi" kuraliyla
 * uyumlu hale gelir. Uretilen kodlar guvenlik geregi log'a yazilmaz, sadece sayisi yazilir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DavetKoduBackfillRunner implements ApplicationRunner {

    private final OtelRepository otelRepository;
    private final DavetKoduUretici davetKoduUretici;

    @Override
    public void run(ApplicationArguments args) {
        List<Otel> kodsuzOteller = otelRepository.findByDavetKoduIsNull();
        if (kodsuzOteller.isEmpty()) {
            return;
        }

        kodsuzOteller.forEach(otel -> otel.setDavetKodu(davetKoduUretici.uret()));
        otelRepository.saveAll(kodsuzOteller);

        log.info("Davet kodu backfill tamamlandi: {} otele yeni davet kodu atandi", kodsuzOteller.size());
    }
}
