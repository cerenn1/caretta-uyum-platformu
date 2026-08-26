package com.caretta.proje.common.init;

import com.caretta.proje.auth.entity.Rol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Rol enum'una (bkz. auth.entity.Rol) yeni bir deger eklendiginde, Hibernate'in
 * ddl-auto=update modu eksik TABLO/KOLONU ekler ama enum kolonuna (users.role) ILK
 * OLUSTURMA aninda otomatik eklenen CHECK constraint'ini ("users_role_check") ASLA
 * guncellemez - bu, ddl-auto=update'in bilinen bir sinirlamasidir (sadece EKSIK seyleri
 * ekler, VAR OLAN bir constraint'in TANIMINI degistirmez). Sonuc: OTEL_YONETICISI rolu
 * eklendiginde, bu ozellikten ONCE olusturulmus (ve halihazirda calisan) veritabanlarinda
 * constraint hala SADECE eski iki degeri (KULLANICI, OTEL_CALISANI) kabul eder; yeni rolle
 * kayit denemesi DataIntegrityViolationException (500 Internal Server Error) ile patlar -
 * yani bu SADECE bir test sorunu degil, gercek (dev/prod) ortamda da ayni sekilde patlar.
 *
 * Bu runner, Rol.values() ile GUNCEL enum degerlerini okuyup constraint'i her acilista
 * yeniden olusturur (DROP + ADD, idempotent) - boylece bu sorun sadece bu seferlik degil,
 * ILERIDE Rol'e baska bir deger eklendiginde de KENDI KENDINE cozulur.
 *
 * GUVENLIK/MIMARI NOTU: burada native SQL kullanilmasi CLAUDE.md'nin "ham SQL yazma, JPA
 * kullan" kuralinin BILINCLI istisnasidir - bu bir IS SORGUSU (business query) DEGIL, JPA/
 * Hibernate'in araciligi OLMAYAN bir SEMA BAKIM islemidir (CHECK constraint TANIMINI ALTER
 * etmek); Spring Data JPA'da bunun bir karsiligi yoktur.
 */
@Slf4j
@Component
public class RolKisitiGuncelleyiciRunner implements ApplicationRunner {

    private static final String KISIT_ADI = "users_role_check";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String izinVerilenDegerler = Arrays.stream(Rol.values())
                .map(rol -> "'" + rol.name() + "'")
                .collect(Collectors.joining(", "));

        entityManager.createNativeQuery(
                "ALTER TABLE users DROP CONSTRAINT IF EXISTS " + KISIT_ADI
        ).executeUpdate();

        entityManager.createNativeQuery(
                "ALTER TABLE users ADD CONSTRAINT " + KISIT_ADI +
                        " CHECK (role IN (" + izinVerilenDegerler + "))"
        ).executeUpdate();

        log.info("users.role CHECK constraint guncel Rol degerleriyle senkronize edildi: {}", izinVerilenDegerler);
    }
}
