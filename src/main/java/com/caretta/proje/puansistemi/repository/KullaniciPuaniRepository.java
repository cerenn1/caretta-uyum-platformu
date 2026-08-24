package com.caretta.proje.puansistemi.repository;

import com.caretta.proje.puansistemi.entity.KullaniciPuani;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KullaniciPuaniRepository extends JpaRepository<KullaniciPuani, Long> {

    // coalesce ile: hic puan satiri yoksa (SUM null doner) sonuc 0 olarak gelir,
    // servis katmaninda ayrica null kontrolu yapmaya gerek kalmaz.
    @Query("select coalesce(sum(k.puan), 0) from KullaniciPuani k where k.kullanici.id = :kullaniciId")
    Long toplamPuanHesapla(@Param("kullaniciId") Long kullaniciId);
}
