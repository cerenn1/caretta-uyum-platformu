package com.caretta.proje.otel.repository;

import com.caretta.proje.otel.entity.KapanisKaniti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface KapanisKanitiRepository extends JpaRepository<KapanisKaniti, Long> {
    boolean existsByOtelIdAndTarih(Long otelId, LocalDate tarih);

    long countByOtelIdAndTarihBetween(Long otelId, LocalDate baslangic, LocalDate bitis);

    // Rapordaki gun gun dokum tablosu icin: donem icinde kanit yuklenen tarihlerin listesi.
    // Turetilmis "find<Alan>By" projeksiyonu yerine acik @Query kullanildi; bazi Spring
    // Data surumlerinde tek alan projeksiyonlu turetilmis sorgular sonuc donusturme
    // asamasinda (ConversionService) hataya dusebiliyor, acik JPQL bu belirsizligi ortadan kaldirir.
    @Query("select k.tarih from KapanisKaniti k where k.otel.id = :otelId and k.tarih between :baslangic and :bitis")
    List<LocalDate> findTarihByOtelIdAndTarihBetween(@Param("otelId") Long otelId,
                                                      @Param("baslangic") LocalDate baslangic,
                                                      @Param("bitis") LocalDate bitis);
}
