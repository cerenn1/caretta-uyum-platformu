package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.puansistemi.entity.KullaniciPuani;
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
}
