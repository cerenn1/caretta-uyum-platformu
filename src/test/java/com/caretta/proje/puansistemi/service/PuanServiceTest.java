package com.caretta.proje.puansistemi.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.puansistemi.dto.PuanDetayResponse;
import com.caretta.proje.puansistemi.dto.SiralamaResponse;
import com.caretta.proje.puansistemi.entity.KullaniciPuani;
import com.caretta.proje.puansistemi.entity.Rozet;
import com.caretta.proje.puansistemi.repository.KullaniciPuaniRepository;
import com.caretta.proje.yuvatakip.repository.YuvaKaydiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private YuvaKaydiRepository yuvaKaydiRepository;

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
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
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
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(25L);

        Long toplam = puanService.toplamPuanHesapla(1L);

        assertThat(toplam).isEqualTo(25L);
    }

    @Test
    void toplamPuanHesapla_RepositoryNullDonerseSifirDoner() {
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(null);

        Long toplam = puanService.toplamPuanHesapla(1L);

        assertThat(toplam).isEqualTo(0L);
    }

    @Test
    void detayHesapla_RozetYokkenSonrakiRozetBronzVeKalanDogruHesaplanir() {
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
        User kullanici = kullanici();
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(20L);

        // 3 yuva kaydi -> esik olan 5'in altinda, henuz rozet yok.
        PuanDetayResponse detay = puanService.detayHesapla(kullanici, 3L);

        assertThat(detay.toplamPuan()).isEqualTo(20L);
        assertThat(detay.rozet()).isNull();
        assertThat(detay.yuvaKayitToplam()).isEqualTo(3L);
        assertThat(detay.sonrakiRozet()).isEqualTo(Rozet.BRONZ.name());
        assertThat(detay.sonrakiRozeteKalanKayit()).isEqualTo(2L);
    }

    @Test
    void detayHesapla_BronzdaykenSonrakiRozetGumusVeKalanDogruHesaplanir() {
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
        User kullanici = kullanici();
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(70L);

        // 8 yuva kaydi -> BRONZ (esik 5), bir sonraki GUMUS (esik 20), kalan 12.
        PuanDetayResponse detay = puanService.detayHesapla(kullanici, 8L);

        assertThat(detay.rozet()).isEqualTo(Rozet.BRONZ.name());
        assertThat(detay.sonrakiRozet()).isEqualTo(Rozet.GUMUS.name());
        assertThat(detay.sonrakiRozeteKalanKayit()).isEqualTo(12L);
    }

    @Test
    void detayHesapla_AltindaykenSonrakiRozetVeKalanNullOdulTeslimBilgisiDolu() {
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
        User kullanici = kullanici();
        when(kullaniciPuaniRepository.toplamPuanHesapla(1L)).thenReturn(550L);

        // 55 yuva kaydi -> ALTIN (esik 50), zaten en yuksek seviye.
        PuanDetayResponse detay = puanService.detayHesapla(kullanici, 55L);

        assertThat(detay.rozet()).isEqualTo(Rozet.ALTIN.name());
        assertThat(detay.sonrakiRozet()).isNull();
        assertThat(detay.sonrakiRozeteKalanKayit()).isNull();
    }

    @Test
    void siralamaGetir_IlkOnDogruSirayla_KendiSirasiIlkOndeyseNull() {
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
        User kullanici = kullanici();

        when(kullaniciPuaniRepository.kullaniciBazindaToplamPuanSiralamasi())
                .thenReturn(List.of(new Object[]{2L, 100L}, new Object[]{1L, 50L}));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(
                User.builder().id(2L).email("birinci@example.com").password("hash").role(Rol.KULLANICI).build()));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(kullanici));
        when(yuvaKaydiRepository.countByUserId(2L)).thenReturn(30L);
        when(yuvaKaydiRepository.countByUserId(1L)).thenReturn(3L);

        SiralamaResponse response = puanService.siralamaGetir(kullanici);

        assertThat(response.ilkOnlar()).hasSize(2);
        assertThat(response.ilkOnlar().get(0).sira()).isEqualTo(1);
        assertThat(response.ilkOnlar().get(0).email()).isEqualTo("birinci@example.com");
        assertThat(response.ilkOnlar().get(0).toplamPuan()).isEqualTo(100L);
        assertThat(response.ilkOnlar().get(0).rozet()).isEqualTo(Rozet.GUMUS.name());
        assertThat(response.ilkOnlar().get(1).sira()).isEqualTo(2);
        assertThat(response.ilkOnlar().get(1).email()).isEqualTo(kullanici.getEmail());
        // kullanici ilk 10'da olduğu icin kendinin ayrica gosterilmesine gerek yok.
        assertThat(response.kullanicininKendiSirasi()).isNull();
    }

    @Test
    void siralamaGetir_KullaniciIlkOndeDegilseKendiSirasiAyricaDoner() {
        puanService = new PuanService(kullaniciPuaniRepository, userRepository, yuvaKaydiRepository);
        User kullanici = kullanici();

        List<Object[]> siralama = new java.util.ArrayList<>();
        for (long i = 101; i <= 110; i++) {
            siralama.add(new Object[]{i, 1000L - i});
            when(userRepository.findById(i)).thenReturn(java.util.Optional.of(
                    User.builder().id(i).email("kullanici" + i + "@example.com").password("hash")
                            .role(Rol.KULLANICI).build()));
            when(yuvaKaydiRepository.countByUserId(i)).thenReturn(0L);
        }
        siralama.add(new Object[]{1L, 5L}); // 11. sirada, ilk 10'un disinda kalan kullanicimiz.
        when(kullaniciPuaniRepository.kullaniciBazindaToplamPuanSiralamasi()).thenReturn(siralama);
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(kullanici));
        when(yuvaKaydiRepository.countByUserId(1L)).thenReturn(3L);

        SiralamaResponse response = puanService.siralamaGetir(kullanici);

        assertThat(response.ilkOnlar()).hasSize(10);
        assertThat(response.kullanicininKendiSirasi()).isNotNull();
        assertThat(response.kullanicininKendiSirasi().sira()).isEqualTo(11);
        assertThat(response.kullanicininKendiSirasi().email()).isEqualTo(kullanici.getEmail());
        assertThat(response.kullanicininKendiSirasi().toplamPuan()).isEqualTo(5L);
    }
}
