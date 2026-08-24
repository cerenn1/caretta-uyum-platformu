package com.caretta.proje.otel.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.common.ZamanDilimi;
import com.caretta.proje.common.exception.DuplicateResourceException;
import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.common.exception.YetkisizErisimException;
import com.caretta.proje.otel.dto.KapanisKanitiResponse;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.entity.KapanisKaniti;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import com.caretta.proje.puansistemi.service.PuanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KapanisKanitiService {

    private static final Set<String> IZIN_VERILEN_TIPLER = Set.of("image/jpeg", "image/png");

    // Uyum orani endpoint'inin varsayilan donemi (son 30 gun). public: uyum raporu
    // servisi de tarih araligi verilmediginde ayni varsayilani kullanmak icin buna basvurur,
    // boylece "son 30 gun" tanimi tek yerde durur.
    public static final long UYUM_ORANI_DONEM_GUN_SAYISI = 30;
    private static final long MAKS_DOSYA_BOYUTU_BYTE = 10L * 1024 * 1024;

    private final KapanisKanitiRepository kapanisKanitiRepository;
    private final FotografDepolamaServisi fotografDepolamaServisi;
    private final OtelService otelService;
    private final PuanService puanService;

    // GUVENLIK: kanit kaydi + puan eklemesi TEK transaction'da atomik olsun diye
    // @Transactional eklendi - aksi halde kanit basariyla kaydedilip puan eklemesi
    // yarim kalirsa (veya tam tersi) veri tutarsizligi olusabilirdi (bkz. YuvaKaydiService.ekle).
    @Transactional
    public KapanisKanitiResponse yukle(MultipartFile fotograf, User currentUser) {
        if (currentUser.getRole() != Rol.OTEL_CALISANI || currentUser.getOtel() == null) {
            throw new YetkisizErisimException("Sadece bir otele bagli otel calisanlari kapanis kaniti yukleyebilir");
        }

        if (fotograf == null || fotograf.isEmpty()) {
            throw new GecersizIstekException("Yuklenecek bir fotograf secmelisiniz");
        }

        String contentType = fotograf.getContentType();
        if (contentType == null || !IZIN_VERILEN_TIPLER.contains(contentType)) {
            throw new GecersizIstekException("Sadece JPG/PNG resim dosyalari kabul edilir");
        }

        if (fotograf.getSize() > MAKS_DOSYA_BOYUTU_BYTE) {
            throw new GecersizIstekException("Dosya boyutu en fazla 10MB olabilir");
        }

        Long otelId = currentUser.getOtel().getId();
        // Zaman dilimi ACIKCA Turkiye - LocalDate.now() sistem varsayilanini kullanir ve
        // container UTC'de calisiyor. Kapanis kaniti dogasi geregi gece yuklenir
        // (20:00-08:00 kisitlamasi), yani Turkiye saatiyle 00:00-03:00 arasi yuklenen bir
        // kanit UTC'ye gore BIR ONCEKI gune damgalanirdi; bu hem gunun kanit slotunu
        // yanlis gune harcar hem de uyum raporunu hatali gosterirdi.
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        if (kapanisKanitiRepository.existsByOtelIdAndTarih(otelId, bugun)) {
            throw new DuplicateResourceException("Bugun icin kapanis kaniti zaten yuklenmis");
        }

        String fotografYolu = fotografDepolamaServisi.kaydet(fotograf, otelId, bugun);

        KapanisKaniti kayit = KapanisKaniti.builder()
                .otel(currentUser.getOtel())
                .kullanici(currentUser)
                .tarih(bugun)
                .fotografYolu(fotografYolu)
                .build();

        kapanisKanitiRepository.save(kayit);

        // Puanlama - GUVENLIK: puan degeri burada sabit olarak belirlenir, istemciden
        // gelen istekte "puan" diye bir alan yoktur/olsa da dikkate alinmaz. Gunde otel
        // basina zaten sadece bir kanit yuklenebildigi icin (yukaridaki existsByOtelIdAndTarih
        // kontrolu) bu +5 puan ekstra bir kisitlamaya gerek kalmadan gunde bir kereyle sinirlidir.
        puanService.puanEkle(currentUser, 5, "KAPANIS_KANITI_YUKLENDI");

        return toResponse(kayit);
    }

    public UyumOraniResponse uyumOraniHesapla(Long otelId, User currentUser) {
        otelErisimYetkisiDogrula(otelId, currentUser);

        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);
        LocalDate baslangic = bugun.minusDays(UYUM_ORANI_DONEM_GUN_SAYISI - 1);

        return donemUyumOraniHesapla(otelId, baslangic, bugun);
    }

    /**
     * Bir kullanicinin belirtilen otelin verilerine (uyum orani, uyum raporu) erisip
     * erisemeyecegini kontrol eder. Otel var mi diye BAKMADAN once cagrilmali; aksi
     * halde "var olmayan otelId -> 404, baska otelin id'si -> 403" farki, saldirgana
     * hangi otelId'lerin gercekte var oldugunu sizdirir.
     */
    public void otelErisimYetkisiDogrula(Long otelId, User currentUser) {
        if (currentUser.getOtel() == null || !currentUser.getOtel().getId().equals(otelId)) {
            throw new YetkisizErisimException("Sadece kendi otelinizin verilerini goruntuleyebilirsiniz");
        }
    }

    /**
     * Uyum orani hesabinin TEK KAYNAGI. Hem /uyum-orani (son 30 gun) hem de PDF
     * denetim raporu (istege bagli tarih araligi) ayni metodu cagirir; boylece iki
     * ayri hesaplama birbirinden sapip raporu guvenilmez hale getirmez.
     */
    public UyumOraniResponse donemUyumOraniHesapla(Long otelId, LocalDate baslangic, LocalDate bitis) {
        Otel otel = otelService.getEntity(otelId);

        long donemGunSayisi = ChronoUnit.DAYS.between(baslangic, bitis) + 1;
        long kanitliGun = kapanisKanitiRepository.countByOtelIdAndTarihBetween(otelId, baslangic, bitis);
        double uyumOrani = Math.round((kanitliGun / (double) donemGunSayisi) * 1000.0) / 10.0;

        return new UyumOraniResponse(
                otel.getId(),
                otel.getAd(),
                baslangic,
                bitis,
                donemGunSayisi,
                kanitliGun,
                uyumOrani
        );
    }

    /**
     * Belirtilen donem icinde kanit yuklenmis tarihlerin listesi. Uyum raporundaki
     * gun gun dokum tablosu ve eksik gunler listesi bu veriden turetilir.
     */
    public List<LocalDate> kanitliTarihleriGetir(Long otelId, LocalDate baslangic, LocalDate bitis) {
        return kapanisKanitiRepository.findTarihByOtelIdAndTarihBetween(otelId, baslangic, bitis);
    }

    private KapanisKanitiResponse toResponse(KapanisKaniti kayit) {
        return new KapanisKanitiResponse(
                kayit.getId(),
                kayit.getOtel().getId(),
                kayit.getTarih(),
                kayit.getFotografYolu(),
                kayit.getOlusturulmaZamani()
        );
    }
}
