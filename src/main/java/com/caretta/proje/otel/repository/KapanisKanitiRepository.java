package com.caretta.proje.otel.repository;

import com.caretta.proje.otel.entity.KapanisKaniti;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface KapanisKanitiRepository extends JpaRepository<KapanisKaniti, Long> {
    boolean existsByOtelIdAndTarih(Long otelId, LocalDate tarih);

    long countByOtelIdAndTarihBetween(Long otelId, LocalDate baslangic, LocalDate bitis);
}
