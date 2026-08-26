package com.caretta.proje.auth.entity;

import com.caretta.proje.otel.entity.Otel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol role = Rol.KULLANICI;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "otel_id")
    private Otel otel;

    // DIKKAT: nullable = false KOYMA. Davet kodu/koltuk alanlarindaki AYNI sebeple -
    // veritabaninda zaten kayitli kullanicilar var, ddl-auto=update ile NOT NULL kolon
    // eklemek semayi patlatir. Mevcut kullanicilar icin backfill runner (bkz.
    // common.init.KullaniciAktifBackfillRunner) acilista true atar; null iken de
    // isEnabled() geriye donuk uyumluluk icin aktif sayar (asagida). YENI kayitlarin
    // hepsi (bkz. AuthService#register) Boolean.TRUE ile olusturulur, null sadece
    // gecmis (backfill ONCESI) kayitlarda gorulur.
    @Builder.Default
    @Column(nullable = true)
    private Boolean aktif = Boolean.TRUE;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // null = ozellik eklenmeden once olusturulmus (backfill ONCESI) kullanici ->
        // geriye donuk uyumluluk icin aktif sayilir, aksi halde TUM mevcut kullanicilar
        // aniden giris yapamaz hale gelirdi.
        return aktif == null || aktif;
    }
}
