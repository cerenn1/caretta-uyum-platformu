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
    private final Path yuvaKaydiYuklemeDizini;

    public FotografDepolamaServisi(@Value("${uploads.kapanis-kaniti-dir}") String yuklemeDizini,
                                    @Value("${uploads.yuva-kaydi-dir}") String yuvaKaydiYuklemeDizini) {
        this.yuklemeDizini = Paths.get(yuklemeDizini).toAbsolutePath().normalize();
        this.yuvaKaydiYuklemeDizini = Paths.get(yuvaKaydiYuklemeDizini).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.yuklemeDizini);
            Files.createDirectories(this.yuvaKaydiYuklemeDizini);
        } catch (IOException e) {
            log.error("Yukleme dizini olusturulamadi: {}", this.yuklemeDizini, e);
            throw new UncheckedIOException("Yukleme dizini hazirlanamadi", e);
        }
    }

    public String kaydet(MultipartFile dosya, Long otelId, LocalDate tarih) {
        String dosyaAdiOneki = otelId + "_" + tarih;
        return kaydetOrtak(dosya, yuklemeDizini, dosyaAdiOneki);
    }

    /**
     * Yuva kaydina eklenen OPSIYONEL fotograf icin genel amacli kayit metodu.
     * Otel kapanis kaniti akisiyla AYNI guvenlik kurallarini (jpg/png, 10MB,
     * UUID dosya adi, path traversal kontrolu) uygular - sadece hedef klasor
     * ve dosya adi oneki farklidir.
     */
    public String yuvaKaydiFotografiKaydet(MultipartFile dosya, Long kullaniciId) {
        String dosyaAdiOneki = "yuva_" + kullaniciId;
        return kaydetOrtak(dosya, yuvaKaydiYuklemeDizini, dosyaAdiOneki);
    }

    private String kaydetOrtak(MultipartFile dosya, Path hedefKlasor, String dosyaAdiOneki) {
        String uzanti = IZIN_VERILEN_UZANTILAR.get(dosya.getContentType());
        if (uzanti == null) {
            throw new GecersizIstekException("Sadece JPG/PNG resim dosyalari kabul edilir");
        }

        String dosyaAdi = dosyaAdiOneki + "_" + UUID.randomUUID() + uzanti;
        Path hedef = hedefKlasor.resolve(dosyaAdi).normalize();

        if (!hedef.startsWith(hedefKlasor)) {
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
