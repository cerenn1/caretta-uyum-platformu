package com.caretta.proje.uyelik.service;

import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.uyelik.dto.KoltukSatinAlmaResponse;
import com.caretta.proje.uyelik.entity.KoltukSatinAlma;
import com.caretta.proje.uyelik.entity.SatinAlmaDurumu;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import com.caretta.proje.uyelik.repository.KoltukSatinAlmaRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Stripe Checkout (TEST/SANDBOX modu) entegrasyonu.
 *
 * Mimari karar: kart bilgisi HICBIR ZAMAN bizim sunucumuza gelmez - backend sadece bir
 * Checkout Session olusturur, kullanici Stripe'in kendi barindirdigi odeme sayfasina
 * yonlendirilir (PCI DSS kapsamimizin disinda kalir). Odeme basarili oldugunda koltuk
 * sayisi SADECE imzasi dogrulanmis webhook ile (bkz. webhookIsle) artar - checkout
 * URL'ine gidip odeme yapmadan geri donmek (ya da sahte bir "basarili" istegi atmak)
 * asla bedava koltuk kazandirmaz.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeOdemeServisi {

    private final KoltukSatinAlmaRepository koltukSatinAlmaRepository;
    private final OtelRepository otelRepository;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.koltuk-birim-fiyati-usd-cent}")
    private long koltukBirimFiyatiCent;

    @Value("${stripe.basari-url}")
    private String basariUrl;

    @Value("${stripe.iptal-url}")
    private String iptalUrl;

    // stripe-java kutuphanesinin API anahtarini okudugu tek yer bu global statik
    // alandir. Kullanicinin henuz gercek bir Stripe hesabi/anahtari yok - secretKey
    // gelistirme ortaminda bos kalabilir, bu durumda uygulama acilista COKMEZ,
    // sadece Session.create() cagrildiginda anlamli bir hata alinir (asagida yakalanir).
    @PostConstruct
    void stripeApiAnahtariniAyarla() {
        Stripe.apiKey = secretKey;
    }

    @Transactional
    public KoltukSatinAlmaResponse checkoutOturumuOlustur(Otel otel, int koltukSayisi) {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(basariUrl)
                .setCancelUrl(iptalUrl)
                .putMetadata("otelId", String.valueOf(otel.getId()))
                .putMetadata("koltukSayisi", String.valueOf(koltukSayisi))
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity((long) koltukSayisi)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(koltukBirimFiyatiCent)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("CarettaGuard - Koltuk")
                                        .setDescription("Otel calisani hesabi icin koltuk (test/sandbox odemesi)")
                                        .build())
                                .build())
                        .build())
                .build();

        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            // GUVENLIK: Stripe'in gercek hata mesaji (ör. "Invalid API Key provided")
            // istemciye dogrudan sizdirilmiyor - loglanip GlobalExceptionHandler'in
            // genel 500 yanitina dusecek genel bir RuntimeException'a cevriliyor
            // (AuthService.login'deki IllegalStateException ile ayni desen).
            log.error("Stripe checkout oturumu olusturulamadi (otelId={}, koltukSayisi={})",
                    otel.getId(), koltukSayisi, e);
            throw new IllegalStateException(
                    "Odeme sayfasi su anda olusturulamiyor, lutfen daha sonra tekrar deneyin", e);
        }

        KoltukSatinAlma satinAlma = KoltukSatinAlma.builder()
                .otel(otel)
                .koltukSayisi(koltukSayisi)
                .stripeCheckoutSessionId(session.getId())
                .durum(SatinAlmaDurumu.BEKLIYOR)
                .build();
        koltukSatinAlmaRepository.save(satinAlma);

        return new KoltukSatinAlmaResponse(session.getUrl(), satinAlma.getId());
    }

    /**
     * Stripe webhook govdesini isler. Imza dogrulamasi HAM govde (payload) uzerinden
     * yapilir - controller katmani govdeyi JSON'a parse ETMEDEN String olarak buraya
     * iletmelidir, aksi halde HMAC-SHA256 imzasi uyusmaz (bkz. StripeWebhookController).
     *
     * Idempotent: Stripe ayni webhook'u birden fazla kez gonderebilecegini garanti eder,
     * bu yuzden ayni session zaten TAMAMLANDI ise koltuk sayisi TEKRAR ARTTIRILMAZ. Bu kontrol
     * ARDISIK (sequential) tekrarlar icin yeterlidir; NEREDEYSE ESZAMANLI iki tekrar icin ise
     * asagida KoltukSatinAlma#versiyon uzerinden JPA optimistic locking + saveAndFlush ile
     * ayrica korunuyor (detay icin asagidaki yorumlara bak).
     */
    @Transactional
    public void webhookIsle(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new GecersizIstekException("Gecersiz webhook imzasi");
        }

        if (!"checkout.session.completed".equals(event.getType())) {
            // Ilgilenmedigimiz event turleri (ör. payment_intent.created) sessizce yok sayilir.
            return;
        }

        Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();
        if (stripeObject.isEmpty() || !(stripeObject.get() instanceof Session session)) {
            log.warn("checkout.session.completed eventinde beklenen Session verisi cozumlenemedi (eventId={})",
                    event.getId());
            return;
        }

        Optional<KoltukSatinAlma> satinAlmaOpt =
                koltukSatinAlmaRepository.findByStripeCheckoutSessionId(session.getId());
        if (satinAlmaOpt.isEmpty()) {
            // Beklenmeyen durum (ör. baska bir ortamda olusturulmus bir session) ama
            // Stripe'a yine de 200 donulmeli - aksi halde ayni webhook'u tekrar tekrar
            // dener. Sadece logla, hata FIRLATMA.
            log.warn("Webhook: bilinmeyen stripe checkout session id: {}", session.getId());
            return;
        }

        KoltukSatinAlma satinAlma = satinAlmaOpt.get();
        if (satinAlma.getDurum() == SatinAlmaDurumu.TAMAMLANDI) {
            log.info("Webhook: {} session'i zaten TAMAMLANDI, tekrar islenmiyor (idempotent)", session.getId());
            return;
        }

        satinAlma.setDurum(SatinAlmaDurumu.TAMAMLANDI);
        satinAlma.setTamamlanmaZamani(LocalDateTime.now());

        // GUVENLIK/EZAMANLILIK: Stripe ayni webhook'u birden fazla kez gonderebilecegini
        // GARANTI eder (retry mekanizmasi, ag gecikmesi vb.). Yukaridaki "durum == TAMAMLANDI mi"
        // kontrolu TEK BASINA bir race condition'a karsi korumasizdir - iki webhook istegi
        // (A ve B) neredeyse ayni anda gelirse ikisi de durumu BEKLIYOR okuyabilir (henuz
        // kimse commit etmeden), ikisi de bu kontrolu gecip Otel'in koltuk sayisini ARTTIRABILIR
        // (lost update - otel odediginden fazla/bedava koltuk kazanir).
        //
        // Bunu onlemek icin KoltukSatinAlma#versiyon alaninda JPA optimistic locking kullaniliyor.
        // saveAndFlush() BILINCLI olarak duz save() yerine cagriliyor: save() flush'i transaction
        // commit'ine kadar erteleyebilir, bu da A ve B'nin ikisinin de versiyon celismesi hic
        // FIRLAMADAN Otel guncellemesine ulasmasina izin verirdi. saveAndFlush(), versiyon
        // celismesini HEMEN (Otel guncellemesine gecmeden ONCE) tetikler: A once flush edip
        // versiyonu arttirinca, B'nin (eski versiyonla yuklenmis) UPDATE'i Hibernate tarafindan
        // ObjectOptimisticLockingFailureException ile reddedilir.
        try {
            koltukSatinAlmaRepository.saveAndFlush(satinAlma);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Baska bir eszamanli webhook istegi bu kaydi BIZDEN ONCE tamamladi - Otel
            // guncellemesi ATLANIR, hata FIRLATILMAZ. Stripe'a yine de 200 donulmeli,
            // aksi halde Stripe bu webhook'u sonsuza kadar tekrar dener.
            log.info("Webhook: {} session'i icin optimistic lock celismesi - baska bir eszamanli " +
                    "istek zaten tamamlamis, koltuk sayisi tekrar arttirilmiyor", session.getId());
            return;
        }

        Otel otel = satinAlma.getOtel();
        int mevcutKoltuk = otel.getSatinAlinanKoltukSayisi() != null ? otel.getSatinAlinanKoltukSayisi() : 0;
        otel.setSatinAlinanKoltukSayisi(mevcutKoltuk + satinAlma.getKoltukSayisi());
        otel.setUyelikDurumu(UyelikDurumu.AKTIF);
        otelRepository.save(otel);

        log.info("Koltuk satin alma tamamlandi: otelId={}, eklenenKoltuk={}, yeniToplamKoltuk={}",
                otel.getId(), satinAlma.getKoltukSayisi(), otel.getSatinAlinanKoltukSayisi());
    }
}
