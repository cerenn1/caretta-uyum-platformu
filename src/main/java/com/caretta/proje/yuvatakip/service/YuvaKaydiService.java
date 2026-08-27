package com.caretta.proje.yuvatakip.service;

import com.caretta.proje.auth.entity.User;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.service.FotografDepolamaServisi;
import com.caretta.proje.otel.service.OtelService;
import com.caretta.proje.otel.service.OtelYoneticiService;
import com.caretta.proje.puansistemi.service.PuanService;
import com.caretta.proje.yuvatakip.dto.BolgeselYuvaKaydiResponse;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiRequest;
import com.caretta.proje.yuvatakip.dto.YuvaKaydiResponse;
import com.caretta.proje.yuvatakip.entity.Mevsim;
import com.caretta.proje.yuvatakip.entity.YuvaKaydi;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YuvaKaydiService {

    // Otelin bolgesi icinde sayilan yuva kaydi yaricapi (km). Istenen 5-10km
    // araligin ortasi - otelin pratik olarak izleyebilecegi yakin kiyi seridini
    // kapsar, cok uzak/ilgisiz kayitlari dahil etmez.
    private static final double BOLGESEL_YARICAP_KM = 7.0;

    // Kaydi giren kullanicinin kimligi HICBIR SEKILDE sizmasin diye kullanilan
    // sabit/genel etiket - kim oldugu belli olmayacak sekilde HERKES icin ayni.
    private static final String SABIT_KAYDEDEN_ETIKETI = "Sahil Gönüllüsü";

    private final YuvaKaydiRepository yuvaKaydiRepository;
    private final PuanService puanService;
    private final OtelYoneticiService otelYoneticiService;
    private final OtelService otelService;
    private final FotografDepolamaServisi fotografDepolamaServisi;

    // GUVENLIK: yuva kaydi + puan eklemeleri TEK transaction'da atomik olsun diye
    // @Transactional eklendi - aksi halde kayit basariyla kaydedilip puan eklemesi
    // (veya tam tersi) yarim kalirsa veri tutarsizligi olusabilirdi.
    @Transactional
    public YuvaKaydiResponse ekle(YuvaKaydiRequest request, MultipartFile fotograf, User currentUser) {
        YuvaKaydi kayit = YuvaKaydi.builder()
                .user(currentUser)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .tarih(request.tarih())
                .durum(request.durum())
                .notlar(request.notlar())
                .build();

        // Fotograf opsiyonel - gonderilmediyse (null veya bos dosya) sorun degil,
        // kayit fotografsiz devam eder. Gonderildiyse mevcut kapanis kaniti akisiyla
        // AYNI dogrulama kurallariyla (jpg/png, 10MB, UUID dosya adi) kaydedilir.
        if (fotograf != null && !fotograf.isEmpty()) {
            String fotografYolu = fotografDepolamaServisi.yuvaKaydiFotografiKaydet(fotograf, currentUser.getId());
            kayit.setFotografYolu(fotografYolu);
        }

        yuvaKaydiRepository.save(kayit);

        // Puanlama - GUVENLIK: puan degerleri burada sabit olarak belirlenir, istemciden
        // gelen request'te "puan" diye bir alan yoktur/olsa da dikkate alinmaz. Harita
        // bonusu ayri bir satir olarak eklenir (10 + 5, tek satirda 15 degil) ki
        // "nasil kazanildigi" ayri ayri denetlenebilsin.
        puanService.puanEkle(currentUser, 10, "YUVA_KAYDI_EKLENDI");
        if (Boolean.TRUE.equals(request.haritadanSecildiMi())) {
            puanService.puanEkle(currentUser, 5, "HARITADAN_KONUM_SECILDI_BONUS");
        }

        return toResponse(kayit);
    }

    public List<YuvaKaydiResponse> listele(User currentUser) {
        return yuvaKaydiRepository.findByUserIdOrderByTarihDesc(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private YuvaKaydiResponse toResponse(YuvaKaydi kayit) {
        return new YuvaKaydiResponse(
                kayit.getId(),
                kayit.getLatitude(),
                kayit.getLongitude(),
                kayit.getTarih(),
                kayit.getDurum(),
                kayit.getNotlar(),
                kayit.getCreatedAt(),
                Mevsim.hesapla(kayit.getTarih()),
                kayit.getFotografYolu()
        );
    }

    /**
     * Otel yoneticisinin, kendi otelinin bolgesindeki (7km) TUM kullanicilarin
     * girdigi yuva kayitlarini gorebilmesi icin. Yatay yetki kontrolu
     * OtelYoneticiService#yoneticiErisimYetkisiDogrula ile yapilir - sadece
     * OTEL_YONETICISI rolu + SADECE kendi oteli icin erisilebilir.
     *
     * GUVENLIK: kaydi giren kullaniciya (kayit.getUser()) ait HICBIR alan
     * (email, id, isim vb.) donen DTO'ya KONULMAZ - herkes icin sabit
     * SABIT_KAYDEDEN_ETIKETI degeri kullanilir.
     */
    public List<BolgeselYuvaKaydiResponse> bolgeselKayitlariGetir(Long otelId, User currentUser) {
        otelYoneticiService.yoneticiErisimYetkisiDogrula(otelId, currentUser);
        Otel otel = otelService.getEntity(otelId);

        return yuvaKaydiRepository.findAll().stream()
                .filter(k -> haversineKm(otel.getLatitude(), otel.getLongitude(), k.getLatitude(), k.getLongitude()) <= BOLGESEL_YARICAP_KM)
                .map(this::toBolgeselResponse)
                .toList();
    }

    private BolgeselYuvaKaydiResponse toBolgeselResponse(YuvaKaydi kayit) {
        return new BolgeselYuvaKaydiResponse(
                kayit.getId(),
                kayit.getLatitude(),
                kayit.getLongitude(),
                kayit.getTarih(),
                kayit.getDurum(),
                kayit.getNotlar(),
                kayit.getCreatedAt(),
                Mevsim.hesapla(kayit.getTarih()),
                SABIT_KAYDEDEN_ETIKETI
        );
    }

    // Haversine formulu - KapsamAlaniService#haversineKm ile AYNI implementasyon,
    // farkli modulde oldugu icin hiz amacli kopyalandi (bkz. gorev talimati).
    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double dunyaYaricapiKm = 6371.0;

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return dunyaYaricapiKm * c;
    }
}
