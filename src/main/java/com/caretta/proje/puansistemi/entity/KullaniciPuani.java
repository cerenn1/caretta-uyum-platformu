package com.caretta.proje.puansistemi.entity;

import com.caretta.proje.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// DIKKAT: bu tabloya satir eklenmesi SADECE PuanService.puanEkle(...) uzerinden,
// sunucu tarafinda hesaplanan sabit puan degerleriyle olur. Istemciden gelen bir
// HTTP istegiyle dogrudan satir eklenmesine izin veren hicbir controller yoktur.
@Entity
@Table(name = "kullanici_puanlari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KullaniciPuani {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User kullanici;

    @Column(nullable = false)
    private Integer puan;

    @Column(nullable = false)
    private String sebep;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturulmaZamani = LocalDateTime.now();
}
