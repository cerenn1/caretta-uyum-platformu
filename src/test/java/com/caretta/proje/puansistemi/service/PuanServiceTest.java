package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.puansistemi.entity.KullaniciPuani;
import com.caretta.proje.puansistemi.entity.Rozet;
import com.caretta.proje.puansistemi.repository.KullaniciPuaniRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Birim testleri - Spring context ayaga kaldirilmiyor, saf Mockito.
 * Odak: puan satirinin dogru alanlarla kaydedilmesi ve toplam puan hesabinda
 * null -> 0 donusumu.
 */
@ExtendWith(MockitoExtension.class)
class PuanServiceTest {

    @Mock
    private KullaniciPuaniRepository kullaniciPuaniRepository;

    private PuanService puanService;

    private User kullanici() {
        return User.builder()
                .id(1L)
                .email("kullanici@example.com")
                .password("hash")
                .role(Rol.KULLANICI)
                .build();
    }

    @Test
    void puanEkle_DogruAlanlarlaSatirKaydeder() {
        puanService = new PuanService(kullaniciPuaniRepository);
        User kullanici = kullanici();

        puanService.puanEkle(kullanici, 10, "YUVA_KAYDI_EKLENDI");

        ArgumentCaptor<KullaniciPuani> captor = ArgumentCaptor.forClass(KullaniciPuani.class);
        verify(kullaniciPuaniRepository).save(captor.capture());

        KullaniciPuani kaydedilen = captor.getValue();
        assertThat(kaydedilen.getKullanici()).isEqualTo(kullanici);
        assertThat(kaydedilen.getPuan()).isEqualTo(10);
        assertThat(kaydedilen.getSebep()).isEqualTo("YUVA_KAYDI_EKLENDI");
    }

    @Test
    void toplamPuanHesapla_RepositoryDonenDegeriAynenDoner() {
        puanService = new PuanService(kullaniciPuaniRepository);
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(25L);

        Long toplam = puanService.toplamPuanHesapla(1L);

        assertThat(toplam).isEqualTo(25L);
    }

    @Test
    void toplamPuanHesapla_RepositoryNullDonerseSifirDoner() {
        puanService = new PuanService(kullaniciPuaniRepository);
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(null);

        Long toplam = puanService.toplamPuanHesapla(1L);

        assertThat(toplam).isEqualTo(0L);
    }

    @Test
    void detayHesapla_RozetYokkenSonrakiRozetBronzVeKalanDogruHesaplanir() {
        puanService = new PuanService(kullaniciPuaniRepository);
        User kullanici = kullanici();
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(20L);

        // 3 yuva kaydi -> esik olan 5'in altinda, henuz rozet yok.
        PuanDetayResponse detay = puanService.detayHesapla(kullanici, 3L);

        assertThat(detay.toplamPuan()).isEqualTo(20L);
        assertThat(detay.rozet()).isNull();
        assertThat(detay.yuvaKayitToplam()).isEqualTo(3L);
        assertThat(detay.sonrakiRozet()).isEqualTo(Rozet.BRONZ.name());
        assertThat(detay.sonrakiRozeteKalanKayit()).isEqualTo(2L);
        assertThat(detay.odulMesaji()).isNull();
    }

    @Test
    void detayHesapla_BronzdaykenSonrakiRozetGumusVeKalanDogruHesaplanir() {
        puanService = new PuanService(kullaniciPuaniRepository);
        User kullanici = kullanici();
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(70L);

        // 8 yuva kaydi -> BRONZ (esik 5), bir sonraki GUMUS (esik 20), kalan 12.
        PuanDetayResponse detay = puanService.detayHesapla(kullanici, 8L);

        assertThat(detay.rozet()).isEqualTo(Rozet.BRONZ.name());
        assertThat(detay.sonrakiRozet()).isEqualTo(Rozet.GUMUS.name());
        assertThat(detay.sonrakiRozeteKalanKayit()).isEqualTo(12L);
        assertThat(detay.odulMesaji()).isEqualTo(PuanService.ODUL_MESAJI);
    }

    @Test
    void detayHesapla_AltindaykenSonrakiRozetVeKalanNullOdulTeslimBilgisiDolu() {
        puanService = new PuanService(kullaniciPuaniRepository);
        User kullanici = kullanici();
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(550L);

        // 55 yuva kaydi -> ALTIN (esik 50), zaten en yuksek seviye.
        PuanDetayResponse detay = puanService.detayHesapla(kullanici, 55L);

        assertThat(detay.rozet()).isEqualTo(Rozet.ALTIN.name());
        assertThat(detay.sonrakiRozet()).isNull();
        assertThat(detay.sonrakiRozeteKalanKayit()).isNull();
        assertThat(detay.odulMesaji()).isEqualTo(PuanService.ODUL_MESAJI);
    }

    @Test
    void detayHesapla_RolFarkiOlmaksizinAyniOdulMesajiGosterilir() {
        puanService = new PuanService(kullaniciPuaniRepository);
        User otelCalisani = User.builder()
                .id(2L)
                .email("otel_calisani@example.com")
                .password("hash")
                .role(Rol.OTEL_CALISANI)
                .build();
        when(kullaniciPuaniRepository.toplamPuanHesapla(2L)).thenReturn(70L);

        // Ayni kayit sayisi (8) - biri KULLANICI biri OTEL_CALISANI - AYNI genel
        // odul mesajini gormeli, aralarinda hicbir fark OLMAMALI.
        PuanDetayResponse detay = puanService.detayHesapla(otelCalisani, 8L);

        assertThat(detay.odulMesaji()).isEqualTo(PuanService.ODUL_MESAJI);
    }
}
