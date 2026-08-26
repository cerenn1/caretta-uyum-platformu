package com.caretta.proje.uyelik.service;

import com.caretta.proje.auth.entity.Rol;
import com.caretta.proje.auth.entity.User;
import com.caretta.proje.auth.repository.UserRepository;
import com.caretta.proje.common.exception.YetkisizErisimException;
import com.caretta.proje.otel.entity.Otel;
import com.caretta.proje.otel.repository.OtelRepository;
import com.caretta.proje.otel.service.KapanisKanitiService;
import com.caretta.proje.otel.service.OtelService;
import com.caretta.proje.uyelik.dto.KoltukSatinAlmaResponse;
import com.caretta.proje.uyelik.dto.UyelikDurumuResponse;
import com.caretta.proje.uyelik.entity.UyelikDurumu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UyelikServiceTest {

    private static final String DOGRU_ADMIN_ANAHTARI = "test-icin-uydurulmus-admin-anahtari";

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtelRepository otelRepository;

    @Mock
    private OtelService otelService;

    @Mock
    private StripeOdemeServisi stripeOdemeServisi;

    @Mock
    private KapanisKanitiService kapanisKanitiService;

    @InjectMocks
    private UyelikService uyelikService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uyelikService, "yapilandirilmisAdminAnahtari", DOGRU_ADMIN_ANAHTARI);
    }

    private Otel otel(Boolean manuelPremiumMu, UyelikDurumu uyelikDurumu) {
        return Otel.builder()
                .id(1L)
                .ad("Test Otel")
                .latitude(36.85)
                .longitude(30.7)
                .satinAlinanKoltukSayisi(5)
                .manuelPremiumMu(manuelPremiumMu)
                .uyelikDurumu(uyelikDurumu)
                .build();
    }

    @Test
    void premiumMu_manuelPremiumMuTrueIseUyelikDurumuNeOlursaOlsunTrueDoner() {
        assertThat(uyelikService.premiumMu(otel(true, UyelikDurumu.DENEME))).isTrue();
        assertThat(uyelikService.premiumMu(otel(true, null))).isTrue();
    }

    @Test
    void premiumMu_uyelikDurumuAktifIseTrueDoner() {
        assertThat(uyelikService.premiumMu(otel(false, UyelikDurumu.AKTIF))).isTrue();
    }

    @Test
    void premiumMu_ikisiDeFalseVeyaDenemeIseFalseDoner() {
        assertThat(uyelikService.premiumMu(otel(false, UyelikDurumu.DENEME))).isFalse();
        assertThat(uyelikService.premiumMu(otel(null, null))).isFalse();
        assertThat(uyelikService.premiumMu(otel(false, UyelikDurumu.PASIF))).isFalse();
    }

    @Test
    void kullanilanKoltukSayisi_userRepositoryUzerindenOtelCalisaniSayisiniDoner() {
        when(userRepository.countByOtelIdAndRole(1L, Rol.OTEL_CALISANI)).thenReturn(3L);

        long sonuc = uyelikService.kullanilanKoltukSayisi(1L);

        assertThat(sonuc).isEqualTo(3L);
        verify(userRepository).countByOtelIdAndRole(1L, Rol.OTEL_CALISANI);
    }

    @Test
    void durumGetir_tumAlanlariDogruDoldurur() {
        Otel otel = otel(false, UyelikDurumu.AKTIF);
        when(userRepository.countByOtelIdAndRole(1L, Rol.OTEL_CALISANI)).thenReturn(2L);

        UyelikDurumuResponse response = uyelikService.durumGetir(otel);

        assertThat(response.otelId()).isEqualTo(1L);
        assertThat(response.satinAlinanKoltukSayisi()).isEqualTo(5);
        assertThat(response.kullanilanKoltukSayisi()).isEqualTo(2L);
        assertThat(response.uyelikDurumu()).isEqualTo(UyelikDurumu.AKTIF);
        assertThat(response.premiumMu()).isTrue();
    }

    @Test
    void koltukSatinAlBaslat_yetkiKontroluGecerseStripeServisineDelegeEder() {
        User calisan = User.builder().id(10L).email("c@example.com").password("h").role(Rol.OTEL_CALISANI).build();
        Otel otel = otel(false, UyelikDurumu.DENEME);
        KoltukSatinAlmaResponse beklenenYanit = new KoltukSatinAlmaResponse("https://checkout.stripe.com/xyz", 42L);

        lenient().when(otelService.getEntity(1L)).thenReturn(otel);
        lenient().when(stripeOdemeServisi.checkoutOturumuOlustur(otel, 5)).thenReturn(beklenenYanit);

        KoltukSatinAlmaResponse sonuc = uyelikService.koltukSatinAlBaslat(1L, 5, calisan);

        verify(kapanisKanitiService).otelErisimYetkisiDogrula(1L, calisan);
        assertThat(sonuc).isEqualTo(beklenenYanit);
    }

    @Test
    void koltukSatinAlBaslat_yetkiYoksaStripeServisiHicCagrilmaz() {
        User calisan = User.builder().id(10L).email("c@example.com").password("h").role(Rol.OTEL_CALISANI).build();
        org.mockito.Mockito.doThrow(new YetkisizErisimException("yetkisiz"))
                .when(kapanisKanitiService).otelErisimYetkisiDogrula(eq(2L), any());

        assertThatThrownBy(() -> uyelikService.koltukSatinAlBaslat(2L, 5, calisan))
                .isInstanceOf(YetkisizErisimException.class);

        verify(stripeOdemeServisi, never()).checkoutOturumuOlustur(any(), anyInt());
    }

    @Test
    void premiumDurumAyarla_dogruAnahtarIleManuelPremiumMuGuncellenir() {
        Otel otel = otel(false, UyelikDurumu.DENEME);
        when(otelService.getEntity(1L)).thenReturn(otel);

        uyelikService.premiumDurumAyarla(1L, true, DOGRU_ADMIN_ANAHTARI);

        assertThat(otel.getManuelPremiumMu()).isTrue();
        verify(otelRepository).save(otel);
    }

    @Test
    void premiumDurumAyarla_yanlisAnahtarYetkisizErisimFirlatir() {
        assertThatThrownBy(() -> uyelikService.premiumDurumAyarla(1L, true, "yanlis-anahtar"))
                .isInstanceOf(YetkisizErisimException.class);

        verify(otelRepository, never()).save(any());
    }

    @Test
    void premiumDurumAyarla_eksikAnahtarYetkisizErisimFirlatir() {
        assertThatThrownBy(() -> uyelikService.premiumDurumAyarla(1L, true, null))
                .isInstanceOf(YetkisizErisimException.class);

        verify(otelRepository, never()).save(any());
    }

    @Test
    void premiumDurumAyarla_yapilandirilmamisAdminAnahtariHerZamanReddeder() {
        ReflectionTestUtils.setField(uyelikService, "yapilandirilmisAdminAnahtari", "");

        assertThatThrownBy(() -> uyelikService.premiumDurumAyarla(1L, true, "herhangi-bir-anahtar"))
                .isInstanceOf(YetkisizErisimException.class);

        verify(otelRepository, never()).save(any());
    }
}
