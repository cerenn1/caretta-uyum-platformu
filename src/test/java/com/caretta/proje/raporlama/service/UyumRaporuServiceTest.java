package com.caretta.proje.raporlama.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.common.ZamanDilimi;
import com.caretta.proje.common.exception.GecersizIstekException;
import com.caretta.proje.common.exception.YetkisizErisimException;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.service.KapanisKanitiService;
import com.caretta.proje.otel.service.OtelService;
import com.caretta.proje.raporlama.dto.UyumRaporuSonucu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Birim testleri - Spring context ayaga kaldirilmiyor, saf Mockito.
 *
 * Odak: uyum raporu servisinin (1) yetkilendirmeyi /uyum-orani ile ayni sekilde
 * kontrol ettigi, (2) uyum orani hesabi icin KENDI hesaplamasini yapmayip ORTAK
 * metodu (KapanisKanitiService#donemUyumOraniHesapla) kullandigi ve (3) tarih
 * araligi dogrulamalarinin dogru calistigi.
 */
@ExtendWith(MockitoExtension.class)
class UyumRaporuServiceTest {

    @Mock
    private KapanisKanitiService kapanisKanitiService;

    @Mock
    private OtelService otelService;

    @Mock
    private UyumRaporuPdfOlusturucu pdfOlusturucu;

    @InjectMocks
    private UyumRaporuService uyumRaporuService;

    private Otel otelA;
    private User otelCalisaniA;

    @BeforeEach
    void setUp() {
        otelA = Otel.builder().id(1L).ad("Otel A").latitude(36.85).longitude(30.7).build();
        otelCalisaniA = User.builder()
                .id(10L)
                .email("calisan@example.com")
                .password("hash")
                .role(Rol.OTEL_CALISANI)
                .otel(otelA)
                .build();
    }

    private UyumOraniResponse ornekUyumOrani(LocalDate baslangic, LocalDate bitis, long donemGun, long kanitliGun, double oran) {
        return new UyumOraniResponse(otelA.getId(), otelA.getAd(), baslangic, bitis, donemGun, kanitliGun, oran);
    }

    @Test
    void raporUret_TarihParametresiVerilmezse_UyumOraniIleAyniOrtakMetoduAyniAralikIleCagirir() {
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);
        LocalDate beklenenBaslangic = bugun.minusDays(KapanisKanitiService.UYUM_ORANI_DONEM_GUN_SAYISI - 1);

        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiService.donemUyumOraniHesapla(eq(1L), eq(beklenenBaslangic), eq(bugun)))
                .thenReturn(ornekUyumOrani(beklenenBaslangic, bugun, 30, 3, 10.0));
        when(kapanisKanitiService.kanitliTarihleriGetir(eq(1L), eq(beklenenBaslangic), eq(bugun)))
                .thenReturn(List.of(bugun));
        when(pdfOlusturucu.olustur(any())).thenReturn(new byte[]{1, 2, 3});

        UyumRaporuSonucu sonuc = uyumRaporuService.raporUret(1L, null, null, otelCalisaniA);

        // KRITIK: rapor servisi uyum oranini KENDI hesaplamiyor, /uyum-orani ile ayni
        // ortak metodu (donemUyumOraniHesapla) tam olarak bu parametrelerle cagiriyor.
        verify(kapanisKanitiService).donemUyumOraniHesapla(1L, beklenenBaslangic, bugun);
        assertThat(sonuc.donemBaslangic()).isEqualTo(beklenenBaslangic);
        assertThat(sonuc.donemBitis()).isEqualTo(bugun);
        assertThat(sonuc.pdfBaytlari()).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void raporUret_GecerliBirTarihAraligiVerilirse_OAralikOrtakMetodaAynenIletilir() {
        LocalDate baslangic = LocalDate.now(ZamanDilimi.TURKIYE).minusDays(10);
        LocalDate bitis = LocalDate.now(ZamanDilimi.TURKIYE);

        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiService.donemUyumOraniHesapla(1L, baslangic, bitis))
                .thenReturn(ornekUyumOrani(baslangic, bitis, 11, 11, 100.0));
        lenient().when(kapanisKanitiService.kanitliTarihleriGetir(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(pdfOlusturucu.olustur(any())).thenReturn(new byte[]{9});

        UyumRaporuSonucu sonuc = uyumRaporuService.raporUret(1L, baslangic, bitis, otelCalisaniA);

        verify(kapanisKanitiService).donemUyumOraniHesapla(1L, baslangic, bitis);
        assertThat(sonuc.donemBaslangic()).isEqualTo(baslangic);
        assertThat(sonuc.donemBitis()).isEqualTo(bitis);
    }

    @Test
    void raporUret_BaskaOtelinIdsiIcinYetkisizErisimFirlatirVePdfHicUretilmez() {
        doThrow(new YetkisizErisimException("yetkisiz"))
                .when(kapanisKanitiService).otelErisimYetkisiDogrula(2L, otelCalisaniA);

        assertThatThrownBy(() -> uyumRaporuService.raporUret(2L, null, null, otelCalisaniA))
                .isInstanceOf(YetkisizErisimException.class);

        verify(pdfOlusturucu, never()).olustur(any());
    }

    @Test
    void raporUret_SadeceBaslangicVerilirseGecersizIstekFirlatir() {
        LocalDate baslangic = LocalDate.now(ZamanDilimi.TURKIYE).minusDays(5);

        assertThatThrownBy(() -> uyumRaporuService.raporUret(1L, baslangic, null, otelCalisaniA))
                .isInstanceOf(GecersizIstekException.class);
    }

    @Test
    void raporUret_SadeceBitisVerilirseGecersizIstekFirlatir() {
        LocalDate bitis = LocalDate.now(ZamanDilimi.TURKIYE);

        assertThatThrownBy(() -> uyumRaporuService.raporUret(1L, null, bitis, otelCalisaniA))
                .isInstanceOf(GecersizIstekException.class);
    }

    @Test
    void raporUret_BitisBaslangictanOnceyseGecersizIstekFirlatir() {
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        assertThatThrownBy(() -> uyumRaporuService.raporUret(1L, bugun, bugun.minusDays(1), otelCalisaniA))
                .isInstanceOf(GecersizIstekException.class);
    }

    @Test
    void raporUret_BitisGelecekteyseGecersizIstekFirlatir() {
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        assertThatThrownBy(() -> uyumRaporuService.raporUret(1L, bugun.minusDays(5), bugun.plusDays(1), otelCalisaniA))
                .isInstanceOf(GecersizIstekException.class);
    }

    @Test
    void raporUret_365GundenUzunAralikGecersizIstekFirlatir() {
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);

        assertThatThrownBy(() -> uyumRaporuService.raporUret(1L, bugun.minusDays(400), bugun, otelCalisaniA))
                .isInstanceOf(GecersizIstekException.class);
    }

    @Test
    void raporUret_TamAraligi365GunOlanIstekKabulEdilir() {
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);
        LocalDate baslangic = bugun.minusDays(364);

        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiService.donemUyumOraniHesapla(1L, baslangic, bugun))
                .thenReturn(ornekUyumOrani(baslangic, bugun, 365, 0, 0.0));
        lenient().when(kapanisKanitiService.kanitliTarihleriGetir(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(pdfOlusturucu.olustur(any())).thenReturn(new byte[]{1});

        UyumRaporuSonucu sonuc = uyumRaporuService.raporUret(1L, baslangic, bugun, otelCalisaniA);

        assertThat(sonuc.pdfBaytlari()).isNotEmpty();
    }

    @Test
    void raporUret_HicKanitiOlmayanOtelIcinCokmedenPdfUretir() {
        LocalDate bugun = LocalDate.now(ZamanDilimi.TURKIYE);
        LocalDate baslangic = bugun.minusDays(29);

        lenient().when(otelService.getEntity(1L)).thenReturn(otelA);
        when(kapanisKanitiService.donemUyumOraniHesapla(1L, baslangic, bugun))
                .thenReturn(ornekUyumOrani(baslangic, bugun, 30, 0, 0.0));
        when(kapanisKanitiService.kanitliTarihleriGetir(1L, baslangic, bugun))
                .thenReturn(List.of());
        when(pdfOlusturucu.olustur(any())).thenReturn(new byte[]{7, 7});

        UyumRaporuSonucu sonuc = uyumRaporuService.raporUret(1L, null, null, otelCalisaniA);

        assertThat(sonuc.pdfBaytlari()).isEqualTo(new byte[]{7, 7});

        ArgumentCaptor<com.caretta.proje.raporlama.dto.UyumRaporuVerisi> captor =
                ArgumentCaptor.forClass(com.caretta.proje.raporlama.dto.UyumRaporuVerisi.class);
        verify(pdfOlusturucu).olustur(captor.capture());
        assertThat(captor.getValue().kanitliGunSayisi()).isZero();
        assertThat(captor.getValue().uyumOrani()).isZero();
        assertThat(captor.getValue().kanitliTarihler()).isEmpty();
    }
}
