package com.caretta.proje.auth.repository;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Koltuk bazli uyelik: bir otele bagli, belirtilen rolde kac kullanici var - koltuk
    // sinirinin ZORLANMASI (bu sayiyi satinAlinanKoltukSayisi ile karsilastirip yeni kayit
    // engelleme) SONRAKI bir gorevde yapilacak, burada sadece sayim saglanir.
    long countByOtelIdAndRole(Long otelId, Rol role);
}
