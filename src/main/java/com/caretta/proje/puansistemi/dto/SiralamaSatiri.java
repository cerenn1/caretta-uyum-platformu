package com.caretta.proje.puansistemi.dto;

/** Liderlik tablosunda tek bir satir: sira, kullanicinin email'i, toplam puani, rozeti. */
public record SiralamaSatiri(int sira, String email, long toplamPuan, String rozet) {
}
