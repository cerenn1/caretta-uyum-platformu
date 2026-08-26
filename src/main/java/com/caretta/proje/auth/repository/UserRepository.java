package com.caretta.proje.auth.repository;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Koltuk bazli uyelik: bir otele bagli, belirtilen rolde kac kullanici var - koltuk
    // sinirinin ZORLANMASI (bu sayiyi satinAlinanKoltukSayisi ile karsilastirip yeni kayit
    // engelleme) icin AuthService#register kullanir.
    long countByOtelIdAndRole(Long otelId, Rol role);

    // Otel yoneticisi paneli: bir otele bagli, belirtilen rolde (OTEL_CALISANI) tum
    // kullanicilarin listesi, en yeni once.
    List<User> findByOtelIdAndRoleOrderByCreatedAtDesc(Long otelId, Rol role);

    // Yatay yetki icin KRITIK: sadece calisanId ile findById yapip SONRA otel/role
    // karsilastirmasi yapmak yerine sorgunun KENDISINE otelId+role dahil edilir - boylece
    // "var olan ama baska otele ait" bir id ile "hic var olmayan" bir id arasindaki fark
    // istemciye SIZMAZ (ikisi de ayni ResourceNotFoundException ile sonuclanir).
    Optional<User> findByIdAndOtelIdAndRole(Long id, Long otelId, Rol role);

    // aktif alani eklenmeden once olusturulmus (NULL kalan) kullanicilar - backfill
    // runner (bkz. common.init.KullaniciAktifBackfillRunner) bunlari bulup true atar.
    List<User> findByAktifIsNull();
}
