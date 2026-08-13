package com.caretta.proje.yuvatakip.repository;

import com.caretta.proje.yuvatakip.entity.YuvaKaydi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YuvaKaydiRepository extends JpaRepository<YuvaKaydi, Long> {
    List<YuvaKaydi> findByUserIdOrderByTarihDesc(Long userId);
}
