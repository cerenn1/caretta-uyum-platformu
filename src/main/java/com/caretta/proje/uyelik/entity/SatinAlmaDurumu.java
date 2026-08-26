package com.caretta.proje.uyelik.entity;

/**
 * Tek bir koltuk satin alma (Stripe Checkout Session) isleminin yasam dongusu durumu.
 * BEKLIYOR: checkout oturumu olusturuldu, kullanici henuz odemeyi tamamlamadi (veya
 * webhook henuz gelmedi). TAMAMLANDI: webhook imzasi dogrulanmis "checkout.session.completed"
 * eventi ile kesinlesti - koltuk sayisi SADECE bu durumda arttirilir. BASARISIZ:
 * SONRAKI bir fazda (ör. "checkout.session.expired" webhook eventi islenmeye baslandiginda)
 * kullanilmak uzere simdiden tanimlanan, bu gorevde herhangi bir akis tarafindan atanmayan durum.
 */
public enum SatinAlmaDurumu {
    BEKLIYOR,
    TAMAMLANDI,
    BASARISIZ
}
