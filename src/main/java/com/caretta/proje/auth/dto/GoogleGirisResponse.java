package com.caretta.proje.auth.dto;

/**
 * yeniKullaniciMi=true VE authResponse=null ise: kullanici henuz yok, role bilgisi
 * olmadan tekrar cagirilmali (client rol secim formu gostersin). yeniKullaniciMi=false
 * ya da (true + role verildi) ise authResponse dolu gelir (JWT).
 */
public record GoogleGirisResponse(boolean yeniKullaniciMi, String email, AuthResponse authResponse) {
}
