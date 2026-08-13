package com.caretta.proje.otel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FotografDepolamaServisi {

    private final Path yuklemeDizini;

    public FotografDepolamaServisi(@Value("${uploads.kapanis-kaniti-dir}") String yuklemeDizini) {
        this.yuklemeDizini = Paths.get(yuklemeDizini).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.yuklemeDizini);
        } catch (IOException e) {
            throw new UncheckedIOException("Yukleme dizini olusturulamadi: " + this.yuklemeDizini, e);
        }
    }

    public String kaydet(MultipartFile dosya, Long otelId, LocalDate tarih) {
        String uzanti = uzantiCikar(dosya.getOriginalFilename());
        String dosyaAdi = otelId + "_" + tarih + "_" + UUID.randomUUID() + uzanti;

        try {
            Path hedef = yuklemeDizini.resolve(dosyaAdi);
            dosya.transferTo(hedef);
        } catch (IOException e) {
            throw new UncheckedIOException("Dosya kaydedilemedi: " + dosyaAdi, e);
        }

        return dosyaAdi;
    }

    private String uzantiCikar(String orijinalAd) {
        if (orijinalAd == null) {
            return "";
        }
        int noktaIndex = orijinalAd.lastIndexOf('.');
        return noktaIndex >= 0 ? orijinalAd.substring(noktaIndex) : "";
    }
}
