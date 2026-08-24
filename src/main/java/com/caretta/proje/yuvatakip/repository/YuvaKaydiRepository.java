package com.caretta.proje.yuvatakip.repository;

import com.caretta.proje.yuvatakip.entity.YuvaKaydi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface YuvaKaydiRepository extends JpaRepository<YuvaKaydi, Long> {
    List<YuvaKaydi> findByUserIdOrderByTarihDesc(Long userId);

    // Panel ozeti icin: sadece sayi lazim, findByUserIdOrderByTarihDesc gibi tum
    // kayitlari belleğe cekip .size() almak yerine dogrudan COUNT sorgusu calistirir.
    long countByUserId(Long userId);

    // Panel ozetindeki "son yuva kaydi" karti icin. Ayni tarihte birden fazla kayit
    // olabildiginden (bir gunde birden fazla gozlem girilebilir), tarih siralamasi tek
    // basina deterministik degil; ikincil siralama id ile yapilarak "en son eklenen"
    // her zaman ayni kaydi verir.
    Optional<YuvaKaydi> findFirstByUserIdOrderByTarihDescIdDesc(Long userId);

    // Agregat istatistik endpoint'i icin: en az bir yuva kaydi eklemis DISTINCT
    // kullanici sayisi. Toplam kayitli kullanici sayisindan farkli - sadece
    // gercekten katki saglayanlari sayar.
    @Query("select count(distinct y.user.id) from YuvaKaydi y")
    long distinctKullaniciSayisi();
}
