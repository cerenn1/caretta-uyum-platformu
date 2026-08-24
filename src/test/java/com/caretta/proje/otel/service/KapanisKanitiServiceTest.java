package com.caretta.proje.otel.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.common.exception.YetkisizErisimException;
import com.caretta.proje.otel.dto.KapanisKanitiResponse;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import com.caretta.proje.puansistemi.service.PuanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Birim testleri - Spring context ayaga kaldirilmiyor, saf Mockito.
 * Odak: uyum orani hesaplama mantigi ve otel bazli yetkilendirme kontrolu.
 */
@ExtendWith(MockitoExtension.class)
class KapanisKanitiServiceTest {

    @Mock
    private KapanisKanitiRepository kapanisKanitiRepository;

    @Mock
    private FotografDepolamaServisi fotografDepolamaServisi;

    @Mock
    private OtelService otelService;

    @Mock
    private PuanService puanService;

    @InjectMocks
    private KapanisKanitiService kapanisKanitiService;

    private Otel otelA;
    private Otel otelB;

    @BeforeEach
    void setUp() {
        otelA = Otel.builder().id(1L).ad("Otel A").latitude(36.85).longitude(30.7).build();
        otelB = Otel.builder().id(2L).ad("Otel B").latitude(37.0).longitude(31.0).build();
    }

    private User otelCalisani(Otel otel) {
        return User.builder()
                .id(10L)
                .email("calisan@example.com")
                .password("hash")
                .role(Rol.OTEL_CALISANI)
                .otel(otel)
                .build();
    }

    @Test
    void uyumOrani_30GunlukDonemde3GunKanitVarsaYuzde10Doner() {
        // Senaryo: gorev tanimindaki ornek - son 30 gunde 3 gun kanit -> %10
        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiRepository.countByOtelIdAndTarihBetween(any(), any(), any())).thenReturn(3L);

        UyumOraniResponse response = kapanisKanitiService.uyumOraniHesapla(1L, otelCalisani(otelA));

        assertThat(response.kanitYuklenenGunSayisi()).isEqualTo(3L);
        assertThat(response.donemGunSayisi()).isEqualTo(30L);
        assertThat(response.uyumOrani()).isEqualTo(10.0);
    }

    @Test
    void uyumOrani_30GundeBirGunKanitVarsaYuzde3Nokta3Doner() {
        // Canli testte dogrulanan deger: 1/30 = %3.3 (yuzde birinci ondalige yuvarlanir)
        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiRepository.countByOtelIdAndTarihBetween(any(), any(), any())).thenReturn(1L);

        UyumOraniResponse response = kapanisKanitiService.uyumOraniHesapla(1L, otelCalisani(otelA));

        assertThat(response.uyumOrani()).isEqualTo(3.3);
    }

    @Test
    void uyumOrani_HicKanitYoksaYuzde0Doner() {
        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiRepository.countByOtelIdAndTarihBetween(any(), any(), any())).thenReturn(0L);

        UyumOraniResponse response = kapanisKanitiService.uyumOraniHesapla(1L, otelCalisani(otelA));

        assertThat(response.uyumOrani()).isEqualTo(0.0);
    }

    @Test
    void uyumOrani_TumGunlerKanitliyseYuzde100Doner() {
        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiRepository.countByOtelIdAndTarihBetween(any(), any(), any())).thenReturn(30L);

        UyumOraniResponse response = kapanisKanitiService.uyumOraniHesapla(1L, otelCalisani(otelA));

        assertThat(response.uyumOrani()).isEqualTo(100.0);
    }

    @Test
    void uyumOrani_DonemBaslangicVeBitisTarihleriDogruHesaplanir() {
        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        lenient().when(kapanisKanitiRepository.countByOtelIdAndTarihBetween(any(), any(), any())).thenReturn(0L);

        UyumOraniResponse response = kapanisKanitiService.uyumOraniHesapla(1L, otelCalisani(otelA));

        LocalDate bugun = LocalDate.now();
        assertThat(response.donemBitis()).isEqualTo(bugun);
        assertThat(response.donemBaslangic()).isEqualTo(bugun.minusDays(29));
    }

    @Test
    void uyumOrani_BaskaOtelinVerisineErisimYetkisizErisimFirlatir() {
        // Kritik yetkilendirme testi: Otel A calisani, Otel B (id=2) verisini isteyemez.
        User otelACalisani = otelCalisani(otelA);

        assertThatThrownBy(() -> kapanisKanitiService.uyumOraniHesapla(otelB.getId(), otelACalisani))
                .isInstanceOf(YetkisizErisimException.class);
    }

    @Test
    void uyumOrani_HicbirOteleBagliOlmayanKullaniciYetkisizErisimFirlatir() {
        User otelSizKullanici = User.builder()
                .id(20L)
                .email("kullanici@example.com")
                .password("hash")
                .role(Rol.KULLANICI)
                .otel(null)
                .build();

        assertThatThrownBy(() -> kapanisKanitiService.uyumOraniHesapla(1L, otelSizKullanici))
                .isInstanceOf(YetkisizErisimException.class);
    }

    @Test
    void yukle_BasariliYuklemeSonrasiBesPuanEklenir() {
        User calisan = otelCalisani(otelA);
        MockMultipartFile fotograf = new MockMultipartFile(
                "fotograf", "kanit.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        lenient().when(kapanisKanitiRepository.existsByOtelIdAndTarih(any(), any())).thenReturn(false);
        lenient().when(fotografDepolamaServisi.kaydet(any(), any(), any())).thenReturn("dosya.png");

        KapanisKanitiResponse response = kapanisKanitiService.yukle(fotograf, calisan);

        assertThat(response.otelId()).isEqualTo(otelA.getId());
        verify(puanService).puanEkle(calisan, 5, "KAPANIS_KANITI_YUKLENDI");
    }
}
