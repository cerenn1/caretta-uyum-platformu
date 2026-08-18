package com.caretta.proje.otel.repository;

import com.caretta.proje.otel.entity.Otel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtelRepository extends JpaRepository<Otel, Long> {

    boolean existsByDavetKodu(String davetKodu);

    List<Otel> findByDavetKoduIsNull();
}
