package com.caretta.proje.istatistik.service;

import com.caretta.proje.istatistik.dto.IstatistikResponse;
import com.caretta.proje.otel.dto.UyumOraniResponse;
import com.caretta.proje.otel.repository.KapanisKanitiRepository;
import com.caretta.proje.otel.service.KapanisKanitiService;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Birim testleri - Spring context ayaga kaldirilmiyor, saf Mockito.
 * Odak: agregat sayimlarin dogru repository metodlarindan okunmasi ve
 * ortalama uyum orani hesabinin (aktif otel yokken null dahil) dogrulugu.
 */
@ExtendWith(MockitoExtension.class)
class IstatistikServiceTest {

    @Mock
    private YuvaKaydiRepository yuvaKaydiRepository;

    @Mock
    private KapanisKanitiRepository kapanisKanitiRepository;

    @Mock
    private KapanisKanitiService kapanisKanitiService;

    private IstatistikService istatistikService;

    private UyumOraniResponse uyumOraniResponse(Long otelId, double uyumOrani) {
        return new UyumOraniResponse(otelId, "Otel " + otelId, LocalDate.now(), LocalDate.now(), 30L, 0L, uyumOrani);
    }

    @Test
    void hesapla_YuvaKaydiVeKatkidaBulunanKullaniciSayisiRepositoryDenGelir() {
        istatistikService = new IstatistikService(yuvaKaydiRepository, kapanisKanitiRepository, kapanisKanitiService);
        when(yuvaKaydiRepository.count()).thenReturn(3L);
        when(yuvaKaydiRepository.distinctKullaniciSayisi()).thenReturn(2L);
        when(kapanisKanitiRepository.distinctAktifOtelIdListesi()).thenReturn(List.of());

        IstatistikResponse response = istatistikService.hesapla();

        assertThat(response.toplamYuvaKaydiSayisi()).isEqualTo(3L);
        assertThat(response.toplamKatkidaBulunanKullaniciSayisi()).isEqualTo(2L);
    }

    @Test
    void hesapla_AktifOtelYokkenOrtalamaUyumOraniNullDoner() {
        istatistikService = new IstatistikService(yuvaKaydiRepository, kapanisKanitiRepository, kapanisKanitiService);
        when(yuvaKaydiRepository.count()).thenReturn(0L);
        when(yuvaKaydiRepository.distinctKullaniciSayisi()).thenReturn(0L);
        when(kapanisKanitiRepository.distinctAktifOtelIdListesi()).thenReturn(List.of());

        IstatistikResponse response = istatistikService.hesapla();

        assertThat(response.aktifOtelSayisi()).isEqualTo(0L);
        assertThat(response.ortalamaUyumOrani()).isNull();
    }

    @Test
    void hesapla_IkiAktifOtelinUyumOraniOrtalamasiDogruHesaplanir() {
        istatistikService = new IstatistikService(yuvaKaydiRepository, kapanisKanitiRepository, kapanisKanitiService);
        when(yuvaKaydiRepository.count()).thenReturn(0L);
        when(yuvaKaydiRepository.distinctKullaniciSayisi()).thenReturn(0L);
        when(kapanisKanitiRepository.distinctAktifOtelIdListesi()).thenReturn(List.of(1L, 2L));
        when(kapanisKanitiService.donemUyumOraniHesapla(eq(1L), any(), any())).thenReturn(uyumOraniResponse(1L, 10.0));
        when(kapanisKanitiService.donemUyumOraniHesapla(eq(2L), any(), any())).thenReturn(uyumOraniResponse(2L, 50.0));

        IstatistikResponse response = istatistikService.hesapla();

        assertThat(response.aktifOtelSayisi()).isEqualTo(2L);
        // (10.0 + 50.0) / 2 = 30.0
        assertThat(response.ortalamaUyumOrani()).isEqualTo(30.0);
    }

    @Test
    void hesapla_HesaplamaTarihiBugunOlarakDoner() {
        istatistikService = new IstatistikService(yuvaKaydiRepository, kapanisKanitiRepository, kapanisKanitiService);
        when(yuvaKaydiRepository.count()).thenReturn(0L);
        when(yuvaKaydiRepository.distinctKullaniciSayisi()).thenReturn(0L);
        when(kapanisKanitiRepository.distinctAktifOtelIdListesi()).thenReturn(List.of());

        IstatistikResponse response = istatistikService.hesapla();

        assertThat(response.hesaplamaTarihi()).isNotNull();
    }
}
