package com.caretta.proje.otel.entity;

import com.caretta.proje.uyelik.entity.UyelikDurumu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "oteller")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otel {

    // Yeni bir otel olusturuldugunda (bkz. OtelService#ekle) atanan, ve gecmis
    // kayitlara backfill runner (bkz. uyelik.init.OtelUyelikBackfillRunner) tarafindan
    // uygulanan varsayilan "deneme" koltuk sayisi - TEK KAYNAK, iki yerde de bu sabit kullanilir.
    public static final int VARSAYILAN_DENEME_KOLTUK_SAYISI = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ad;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // DIKKAT: nullable = false KOYMA. Veritabaninda zaten kayitli oteller var,
    // ddl-auto=update ile NOT NULL kolon eklemek semayi patlatir. Mevcut kayitlar
    // icin CommonPackage'daki backfill runner acilista kod atar.
    @Column(name = "davet_kodu", unique = true, length = 12)
    private String davetKodu;

    // DIKKAT: asagidaki 3 alan da (koltuk bazli uyelik altyapisi) AYNI sebeple
    // nullable = false DEGIL - mevcut oteller icin varsayilan deger backfill runner
    // ile atanir (bkz. uyelik.init.OtelUyelikBackfillRunner), ddl-auto=update semayi
    // patlatmasin diye once nullable kolon eklenir.
    @Column(name = "satin_alinan_koltuk_sayisi")
    private Integer satinAlinanKoltukSayisi;

    @Enumerated(EnumType.STRING)
    @Column(name = "uyelik_durumu")
    private UyelikDurumu uyelikDurumu;

    // Odeme akisindan TAMAMEN bagimsiz, demo amacli manuel isaretleme (bkz.
    // uyelik.controller.AdminUyelikController) - gercek bir abonelik/odeme kaniti DEGILDIR.
    @Column(name = "manuel_premium_mu")
    private Boolean manuelPremiumMu;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
