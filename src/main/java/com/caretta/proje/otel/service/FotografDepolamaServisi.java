package com.caretta.proje.otel.service;

import com.caretta.proje.common.exception.GecersizIstekException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FotografDepolamaServisi {

    private static final Map<String, String> IZIN_VERILEN_UZANTILAR = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png"
    );

    private final Path yuklemeDizini;

    public FotografDepolamaServisi(@Value("${uploads.kapanis-kaniti-dir}") String yuklemeDizini) {
        this.yuklemeDizini = Paths.get(yuklemeDizini).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.yuklemeDizini);
        } catch (IOException e) {
            log.error("Yukleme dizini olusturulamadi: {}", this.yuklemeDizini, e);
            throw new UncheckedIOException("Yukleme dizini hazirlanamadi", e);
        }
    }

    public String kaydet(MultipartFile dosya, Long otelId, LocalDate tarih) {
        String uzanti = IZIN_VERILEN_UZANTILAR.get(dosya.getContentType());
        if (uzanti == null) {
            throw new GecersizIstekException("Sadece JPG/PNG resim dosyalari kabul edilir");
        }

        String dosyaAdi = otelId + "_" + tarih + "_" + UUID.randomUUID() + uzanti;
        Path hedef = yuklemeDizini.resolve(dosyaAdi).normalize();

        if (!hedef.startsWith(yuklemeDizini)) {
            throw new GecersizIstekException("Gecersiz dosya adi");
        }

        try {
            dosya.transferTo(hedef);
        } catch (IOException e) {
            log.error("Dosya kaydedilemedi: {}", hedef, e);
            throw new UncheckedIOException("Dosya kaydedilemedi", e);
        }

        return dosyaAdi;
    }
}
