package com.caretta.proje.uyelik.entity;

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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tek bir Stripe Checkout Session'a karsilik gelen koltuk satin alma kaydi. Bir otel
 * "5 koltuk satin al" dedigi her seferinde bir kayit olusur (BEKLIYOR durumuyla);
 * odeme Stripe tarafinda tamamlaninca webhook bu kaydi TAMAMLANDI'ya cevirir ve
 * Otel.satinAlinanKoltukSayisi'ni arttirir (bkz. StripeOdemeServisi#webhookIsle).
 */
@Entity
@Table(name = "koltuk_satin_almalar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KoltukSatinAlma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "otel_id", nullable = false)
    private Otel otel;

    @Column(name = "koltuk_sayisi", nullable = false)
    private Integer koltukSayisi;

    @Column(name = "stripe_checkout_session_id", unique = true, nullable = false)
    private String stripeCheckoutSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SatinAlmaDurumu durum;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime olusturulmaZamani = LocalDateTime.now();

    // Sadece durum TAMAMLANDI olunca doldurulur - webhook basariyla islendigi an damgalanir.
    @Column(name = "tamamlanma_zamani")
    private LocalDateTime tamamlanmaZamani;

    // JPA optimistic locking: Stripe ayni webhook'u birden fazla kez gonderebilecegini
    // GARANTI eder (retry mekanizmasi, ag gecikmesi vb.). Bu alan olmadan iki eszamanli
    // webhook istegi ikisi de "durum == BEKLIYOR" okuyup ikisi de TAMAMLANDI'ya gecirebilir
    // ve otelin koltuk sayisini IKI KEZ arttirabilirdi (lost update / bedava koltuk riski).
    // Hibernate her UPDATE'te bu sutunu kontrol eder; ayni anda iki islem ayni versiyonu
    // okuyup guncellemeye calisirsa ikincisi ObjectOptimisticLockingFailureException alir
    // (bkz. StripeOdemeServisi#webhookIsle). Mevcut satirlar icin null/0 ile baslar, elle
    // set edilmez - Hibernate tarafindan otomatik yonetilir.
    @Version
    private Long versiyon;
}
