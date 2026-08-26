package com.caretta.proje.common.init;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Aktif/pasif calisan ozelligi eklenmeden once olusturulmus kullanicilar icin aktif
 * alani veritabaninda null kalir. Uygulama her acilista bu kullanicilari bulup true
 * atar, boylece User#isEnabled() null durumunu gecici bir geriye-donuk-uyumluluk
 * ONLEMI olarak kullanmaya devam etmek zorunda kalmaz - DavetKoduBackfillRunner ile
 * AYNI desen (bkz. otel.entity.Otel#davetKodu).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KullaniciAktifBackfillRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<User> aktifDegeriNullOlanlar = userRepository.findByAktifIsNull();
        if (aktifDegeriNullOlanlar.isEmpty()) {
            return;
        }

        aktifDegeriNullOlanlar.forEach(user -> user.setAktif(true));
        userRepository.saveAll(aktifDegeriNullOlanlar);

        log.info("Kullanici aktif backfill tamamlandi: {} kullaniciya aktif=true atandi", aktifDegeriNullOlanlar.size());
    }
}
