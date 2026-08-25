package com.caretta.proje.istatistik.service;

import com.caretta.proje.istatistik.dto.BolgeKaydiSayisi;
import com.caretta.proje.istatistik.dto.KapsamAlaniResponse;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

/**
 * Birim testleri - Spring context ayaga kaldirilmiyor, saf Mockito.
 * Odak: (1) Haversine hesabinin bilinen bir mesafeyle makul olcude uyusmasi,
 * (2) 25km eslesme esiginin icinde/disinda kalan kayitlarin dogru
 * bolgelendirilmesi, (3) k-anonimlik esiginin (3) altinda kalan kucuk bir
 * bolgenin "Diger Bolge"ye tasinmasi, esige ulasan bir bolgenin ise ayri
 * gorunmesi.
 */
@ExtendWith(MockitoExtension.class)
class KapsamAlaniServiceTest {

    @Mock
    private YuvaKaydiRepository yuvaKaydiRepository;

    @Mock
    private KapanisKanitiRepository kapanisKanitiRepository;

    private KapsamAlaniService kapsamAlaniService;

    @Test
    void haversineKm_EkvatorUzerindeBirDerecelikEnlemFarkiYaklasik111KmDoner() {
        // Dunya yaricapi 6371km alindiginda, ekvator uzerinde 1 derecelik enlem
        // farki ~111.19 km'ye denk gelir - bilinen/hesaplanabilir bir referans deger.
        double mesafe = KapsamAlaniService.haversineKm(0.0, 0.0, 1.0, 0.0);

        assertThat(mesafe).isCloseTo(111.19, within(0.5));
    }

    @Test
    void haversineKm_AyniNoktaIcinSifirDoner() {
        double mesafe = KapsamAlaniService.haversineKm(36.27, 29.32, 36.27, 29.32);

        assertThat(mesafe).isCloseTo(0.0, within(0.0001));
    }

    @Test
    void hesapla_EsikIcindekiKayitlarEnYakinKumsalaAtanirEsikDisindakilerDigerBolgeyeGider() {
        kapsamAlaniService = new KapsamAlaniService(yuvaKaydiRepository, kapanisKanitiRepository);

        List<Object[]> konumlar = new ArrayList<>();
        // Patara'nin (36.27, 29.32) TAM UZERINDE 3 kayit -> mesafe 0km, 25km
        // esiginin ICINDE ve k-anonimlik esigine (3) ULASIYOR -> ayri gorunmeli.
        for (int i = 0; i < 3; i++) {
            konumlar.add(new Object[]{36.27, 29.32});
        }
        // Belek'in (36.86, 31.05) TAM UZERINDE 2 kayit -> mesafe 0km, 25km esiginin
        // ICINDE ama k-anonimlik esiginin (3) ALTINDA -> "Diger Bolge"ye tasinmali.
        for (int i = 0; i < 2; i++) {
            konumlar.add(new Object[]{36.86, 31.05});
        }
        // Ankara civari - tum resmi kumsallardan 25km'nin COK disinda 1 kayit ->
        // dogrudan "Diger Bolge"ye gitmeli.
        konumlar.add(new Object[]{39.9208, 32.8541});

        when(yuvaKaydiRepository.count()).thenReturn((long) konumlar.size());
        when(yuvaKaydiRepository.tumKonumlariGetir()).thenReturn(konumlar);
        when(kapanisKanitiRepository.distinctAktifOtelSayisi()).thenReturn(0L);

        KapsamAlaniResponse response = kapsamAlaniService.hesapla();
        List<BolgeKaydiSayisi> bolgeler = response.platformKayitBolgeleri();

        assertThat(bolgeler)
                .as("Patara esik icinde ve k-anonimlik esigini (3) karsiladigi icin ayri gorunmeli")
                .anySatisfy(bolge -> {
                    assertThat(bolge.bolgeAdi()).isEqualTo("Patara");
                    assertThat(bolge.il()).isEqualTo("Antalya");
                    assertThat(bolge.kayitSayisi()).isEqualTo(3L);
                });

        assertThat(bolgeler)
                .as("Belek k-anonimlik esiginin (3) altinda kaldigi (2 kayit) icin ayri GORUNMEMELI")
                .noneMatch(bolge -> bolge.bolgeAdi().equals("Belek"));

        assertThat(bolgeler)
                .as("Belek'in bastirilan 2 kaydi + esik disindaki 1 kayit = Diger Bolge'de toplam 3 kayit olmali")
                .anySatisfy(bolge -> {
                    assertThat(bolge.bolgeAdi()).isEqualTo("Diğer Bölge");
                    assertThat(bolge.il()).isNull();
                    assertThat(bolge.kayitSayisi()).isEqualTo(3L);
                });

        assertThat(bolgeler).hasSize(2);
    }

    @Test
    void hesapla_ResmiKumsallar21TaneVeBesIlAltindaDogruGruplanir() {
        kapsamAlaniService = new KapsamAlaniService(yuvaKaydiRepository, kapanisKanitiRepository);

        when(yuvaKaydiRepository.count()).thenReturn(0L);
        when(yuvaKaydiRepository.tumKonumlariGetir()).thenReturn(List.of());
        when(kapanisKanitiRepository.distinctAktifOtelSayisi()).thenReturn(0L);

        KapsamAlaniResponse response = kapsamAlaniService.hesapla();

        assertThat(response.resmiKorumaAltindakiKumsallar()).hasSize(5);
        int toplamKumsalSayisi = response.resmiKorumaAltindakiKumsallar().stream()
                .mapToInt(grup -> grup.kumsallar().size())
                .sum();
        assertThat(toplamKumsalSayisi).isEqualTo(21);
    }

    @Test
    void hesapla_MaviBayrakVeToplamSayilarDogruDoner() {
        kapsamAlaniService = new KapsamAlaniService(yuvaKaydiRepository, kapanisKanitiRepository);

        when(yuvaKaydiRepository.count()).thenReturn(5L);
        when(yuvaKaydiRepository.tumKonumlariGetir()).thenReturn(List.of());
        when(kapanisKanitiRepository.distinctAktifOtelSayisi()).thenReturn(2L);

        KapsamAlaniResponse response = kapsamAlaniService.hesapla();

        assertThat(response.platformToplamYuvaKaydiSayisi()).isEqualTo(5L);
        assertThat(response.platformAktifOtelSayisi()).isEqualTo(2L);
        assertThat(response.maviBayrakSayilari()).containsEntry("Antalya", 234);
        assertThat(response.maviBayrakYili()).isEqualTo(2026);
    }
}
