package com.caretta.proje.otel.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kapanis_kanitlari", uniqueConstraints = @UniqueConstraint(columnNames = {"otel_id", "tarih"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KapanisKaniti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "otel_id", nullable = false)
    private Otel otel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User kullanici;

    @Column(nullable = false)
    private LocalDate tarih;

    @Column(nullable = false)
    private String fotografYolu;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturulmaZamani = LocalDateTime.now();
}
