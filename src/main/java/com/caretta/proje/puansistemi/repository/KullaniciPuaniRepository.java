package com.caretta.proje.puansistemi.repository;

import com.caretta.proje.puansistemi.entity.KullaniciPuani;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KullaniciPuaniRepository extends JpaRepository<KullaniciPuani, Long> {

    // coalesce ile: hic puan satiri yoksa (SUM null doner) sonuc 0 olarak gelir,
    // servis katmaninda ayrica null kontrolu yapmaya gerek kalmaz.
    @Query("select coalesce(sum(k.puan), 0) from KullaniciPuani k where k.kullanici.id = :kullaniciId")
    Long toplamPuanHesapla(@Param("kullaniciId") Long kullaniciId);

    // Liderlik tablosu (/api/puan-siralamasi) icin: her kullanicinin toplam puani,
    // buyukten kucuge. Rol farki YOK - otel calisani da normal kullanici da AYNI
    // birlesik siralamada yer alir. Her satir Object[]{kullaniciId, toplamPuan}.
    @Query("select k.kullanici.id, sum(k.puan) as toplam from KullaniciPuani k group by k.kullanici.id order by toplam desc")
    List<Object[]> kullaniciBazindaToplamPuanSiralamasi();
}
