const API_BASE = "https://caretta-uyum-platformu-backend.onrender.com";

const state = {
  token: localStorage.getItem("caretta_token") || null,
  email: localStorage.getItem("caretta_email") || null,
  role: localStorage.getItem("caretta_role") || null,
  otelId: localStorage.getItem("caretta_otel_id") || null,
  dil: localStorage.getItem("caretta_dil") || "tr",
};

const els = {
  authSection: document.getElementById("auth-section"),
  dashboardSection: document.getElementById("dashboard-section"),
  navLoginBtn: document.getElementById("nav-login-btn"),
  navLogoutBtn: document.getElementById("nav-logout-btn"),
  heroCta: document.getElementById("hero-cta"),
  tabBtns: document.querySelectorAll(".tab-btn"),
  registerForm: document.getElementById("register-form"),
  loginForm: document.getElementById("login-form"),
  yuvaForm: document.getElementById("yuva-form"),
  yuvaList: document.getElementById("yuva-list"),
  konumSeciliDeger: document.getElementById("konum-secili-deger"),
  userEmailLabel: document.getElementById("user-email-label"),
  registerRole: document.getElementById("register-role"),
  otelSecimAlani: document.getElementById("otel-secim-alani"),
  otelSelect: document.getElementById("otel-select"),
  otelDavetKoduInput: document.getElementById("otel-davet-kodu"),
  yeniOtelAdInput: document.getElementById("yeni-otel-ad"),
  yeniOtelLatInput: document.getElementById("yeni-otel-lat"),
  yeniOtelLngInput: document.getElementById("yeni-otel-lng"),
  yeniOtelEkleBtn: document.getElementById("yeni-otel-ekle-btn"),
  yeniOtelGirisNotu: document.getElementById("yeni-otel-giris-notu"),
  otelPanelSection: document.getElementById("otel-panel-section"),
  yoneticiPanelSection: document.getElementById("yonetici-panel-section"),
  uyelikDurumEtiketi: document.getElementById("uyelik-durum-etiketi"),
  koltukKullanilan: document.getElementById("koltuk-kullanilan"),
  koltukSatinAlinan: document.getElementById("koltuk-satin-alinan"),
  yoneticiDavetKodu: document.getElementById("yonetici-davet-kodu"),
  koltukSatinAlmaForm: document.getElementById("koltuk-satin-alma-form"),
  calisanListesi: document.getElementById("calisan-listesi"),
  bolgeselKayitListesi: document.getElementById("bolgesel-kayit-listesi"),
  uyumOraniGauge: document.getElementById("uyum-orani-gauge"),
  uyumOraniDetay: document.getElementById("uyum-orani-detay"),
  kapanisKanitiForm: document.getElementById("kapanis-kaniti-form"),
  panelSection: document.getElementById("panel-section"),
  panelYukleniyor: document.getElementById("panel-yukleniyor"),
  panelHata: document.getElementById("panel-hata"),
  panelHataMesaj: document.getElementById("panel-hata-mesaj"),
  panelTekrarBtn: document.getElementById("panel-tekrar-btn"),
  panelIcerik: document.getElementById("panel-icerik"),
  haritaToggleBtn: document.getElementById("harita-toggle-btn"),
  haritaAlani: document.getElementById("harita-alani"),
  raporIndirBtn: document.getElementById("rapor-indir-btn"),
  altSekme: document.getElementById("alt-sekme"),
  puanDetayModal: document.getElementById("puan-detay-modal"),
  puanDetayIcerik: document.getElementById("puan-detay-icerik"),
  puanDetayKapatBtn: document.getElementById("puan-detay-kapat-btn"),
  etkiAlaniYukleniyor: document.getElementById("etki-alani-yukleniyor"),
  etkiAlaniIcerik: document.getElementById("etki-alani-icerik"),
  dilDegistirBtn: document.getElementById("dil-degistir-btn"),
};

// ---------------------------------------------------------------------------
// DIL DEGISTIRME (TR/EN) - DEKAMER'in kendi anasayfasindaki gibi tek tikla
// tum statik metni degistiren bir sozluk tabanli i18n. data-i18n="anahtar"
// tasiyan HER eleman icin CEVIRI[dil][anahtar] uygulanir. Eleman input/select
// gibi COCUK ogeler barindirabildigi icin textContent'i TOPTAN degistirmek
// yerine SADECE ilk metin dugumu (text node) guncellenir - boylece ic ice
// <input> gibi ogeler SILINMEZ. data-i18n-placeholder ise input/textarea
// placeholder'ini degistirir.
// ---------------------------------------------------------------------------
const CEVIRI = {
  tr: {
    "nav.giris": "Giriş Yap", "nav.cikis": "Çıkış Yap",
    "hero.slogan": "🐢 Fotoğrafını çek, yükle, caretta'ları hayata bağla.",
    "hero.baslik": "Kaplumbağa Yuvalama Sahillerinde Kıyı Turizmi Uyum Platformu",
    "hero.paragraf": "Antalya ve çevresindeki caretta caretta yuvalama sahillerinde faaliyet gösteren otellerin yasal koruma yükümlülüklerine (gece 20:00–08:00 sahil giriş yasağı, ışıklandırma kısıtlaması) uyumunu izleyen, sahadaki yuva/gözlem kayıtlarını dijitalleştiren ve denetim/sertifikasyon süreçlerinde kullanılabilecek kanıt üreten bir B2B/B2G platform.",
    "hero.cta": "Ücretsiz Kayıt Ol",
    "ozellik.yuva.baslik": "Yuva Kaydı", "ozellik.yuva.aciklama": "Konum, tarih ve fotoğrafla saha gözlemini anında kaydet",
    "ozellik.harita.baslik": "Harita", "ozellik.harita.aciklama": "Tüm kayıtları haritada gör, bölgesel dağılımı takip et",
    "ozellik.uyum.baslik": "Uyum Takibi", "ozellik.uyum.aciklama": "Otel kapanış kanıtı ve uyum oranı tek panelde",
    "ozellik.puan.baslik": "Puan ve Rozet", "ozellik.puan.aciklama": "Katkı sağladıkça puan kazan, sıralamada yüksel",
    "etki.baslik": "Etkimiz / Kapsam Alanımız", "genel.yukleniyor": "Yükleniyor…", "genel.tekrarDene": "Tekrar Dene",
    "tab.kayit": "Kayıt Ol", "tab.giris": "Giriş Yap", "kayit.baslik": "Kayıt Ol",
    "form.email": "Email", "form.sifre": "Şifre", "form.kayitTuru": "Kayıt Türü",
    "rol.kullanici": "Vatandaş / Gözlemci", "rol.calisan": "Otel Çalışanı", "rol.yonetici": "Otel Yöneticisi",
    "form.otel": "Otel", "form.otelSec": "Otel seç...", "form.davetKodu": "Otel davet kodu",
    "form.davetKoduYerTutucu": "Otel size verdiği 8 haneli kodu girin",
    "form.yeniOtelEkle": "+ Listede yok, yeni otel ekle", "form.otelAdi": "Otel adı",
    "form.enlem": "Enlem", "form.boylam": "Boylam", "form.oteliKaydet": "Oteli Kaydet",
    "form.yeniOtelGirisNotu": "Yeni otel eklemek icin once giris yapmalisiniz.",
    "form.veya": "veya", "form.otelId": "Otel ID", "form.davetKoduKisa": "Davet Kodu", "form.kaydiTamamla": "Kaydı Tamamla",
    "panel.baslik": "Ana Panel", "panel.yukleniyor": "Panel yükleniyor…",
    "yuva.baslik": "Yuva / Gözlem Kayıtları", "yuva.konum": "Konum",
    "yuva.haritaNotu1": "Haritada bir noktaya dokunarak/tıklayarak işaretçiyi yuvanın gerçek konumuna taşı.",
    "yuva.konumBelirleniyor": "Konum belirleniyor…",
    "form.tarih": "Tarih", "form.durum": "Durum",
    "durum.aktif": "Aktif", "durum.cikisYapti": "Çıkış Yaptı", "durum.riskAltinda": "Risk Altında",
    "form.not": "Not", "form.notYerTutucu": "Gözlem notu (opsiyonel)",
    "form.fotografOpsiyonel": "Fotoğraf (opsiyonel, JPG/PNG, maks. 10MB)", "form.kaydiEkle": "Kaydı Ekle",
    "yuva.kayitlarim": "Kayıtlarım", "yuva.haritadaGoster": "Haritada Göster",
    "yuva.haritaNotu2": "İşaretçiye tıklayınca kaydın tarihi, durumu ve notu görünür.",
    "otel.panelBaslik": "Otel Kapanış Kanıtı Paneli", "otel.raporIndir": "Uyum Raporu İndir (PDF)",
    "otel.kapanisKanitiFotograf": "Bugünün kapanış kanıtı fotoğrafı (JPG/PNG, maks. 10MB)", "otel.fotografiYukle": "Fotoğrafı Yükle",
    "yonetici.panelBaslik": "Otel Yöneticisi Paneli", "yonetici.uyelikDurumu": "Üyelik durumu:",
    "yonetici.koltuk": "Koltuk:", "yonetici.kullaniliyor": "kullanılıyor", "yonetici.davetKodu": "Davet kodu:",
    "yonetici.koltukSayisi": "Satın alınacak koltuk sayısı", "yonetici.koltukSatinAl": "Koltuk Satın Al (Stripe test kartı)",
    "yonetici.calisanlar": "Çalışanlar", "yonetici.bolgeselKayitlar": "Bölgedeki Yuva Kayıtları (7km, herkese ait)",
    "footer.metin": "CarettaGuard — Kaplumbağa Yuvalama Bölgeleri için Kıyı Turizmi Sürdürülebilirlik Uyum Platformu",
    "puan.detayBaslik": "Puan ve Rozet Detayı",
    "etki.kumsallar.baslik": "🏖️ Resmi Korumalı Yuvalama Kumsalları",
    "etki.kaynak": "Kaynak",
    "etki.plaj": "plaj",
    "etki.maviBayrak.ayrimNotu": "Bu, yukarıdaki yuvalama kumsalı listesinden <strong>ayrı ve farklı bir sertifikadır</strong> — karıştırılmamalıdır.",
    "etki.platform.baslik": "📊 Platformun Kendi Verisi",
    "etki.platform.ayrimNotu": "Bu bölüm resmi ulusal veri DEĞİL — platforma girilen gerçek kayıtlardan anlık hesaplanır.",
    "etki.platform.toplamKayit": "toplam yuva/gözlem kaydı",
    "etki.platform.aktifOtel": "aktif otel",
    "etki.platform.bolgeler": "Kayıtlarımızın Bulunduğu Bölgeler",
    "etki.platform.bolgelerNotu": "Bölgeler, kayıtlı konumlara göre otomatik gruplanır (elle girilmez).",
    "sekme.anasayfa": "Ana Sayfa", "sekme.kayitlarim": "Kayıtlarım", "sekme.harita": "Harita", "sekme.otelPaneli": "Otel Paneli",
    "yuva.haritayiGizle": "Haritayı Gizle",
  },
  en: {
    "nav.giris": "Log In", "nav.cikis": "Log Out",
    "hero.slogan": "🐢 Snap it, upload it, keep caretta's alive.",
    "hero.baslik": "Coastal Tourism Compliance Platform for Sea Turtle Nesting Beaches",
    "hero.paragraf": "A B2B/B2G platform that monitors hotels' compliance with legal protection rules (no beach access 20:00–08:00, lighting restrictions) on caretta caretta nesting beaches around Antalya, digitizes field nest/observation records, and produces evidence usable in audits and sustainability certification.",
    "hero.cta": "Sign Up Free",
    "ozellik.yuva.baslik": "Nest Records", "ozellik.yuva.aciklama": "Log field observations instantly with location, date and photo",
    "ozellik.harita.baslik": "Map", "ozellik.harita.aciklama": "See all records on the map, track regional distribution",
    "ozellik.uyum.baslik": "Compliance Tracking", "ozellik.uyum.aciklama": "Hotel closing evidence and compliance rate in one panel",
    "ozellik.puan.baslik": "Points & Badges", "ozellik.puan.aciklama": "Earn points as you contribute, climb the leaderboard",
    "etki.baslik": "Our Impact / Coverage Area", "genel.yukleniyor": "Loading…", "genel.tekrarDene": "Try Again",
    "tab.kayit": "Sign Up", "tab.giris": "Log In", "kayit.baslik": "Sign Up",
    "form.email": "Email", "form.sifre": "Password", "form.kayitTuru": "Account Type",
    "rol.kullanici": "Citizen / Observer", "rol.calisan": "Hotel Staff", "rol.yonetici": "Hotel Manager",
    "form.otel": "Hotel", "form.otelSec": "Select hotel...", "form.davetKodu": "Hotel invite code",
    "form.davetKoduYerTutucu": "Enter the 8-character code your hotel gave you",
    "form.yeniOtelEkle": "+ Not listed, add a new hotel", "form.otelAdi": "Hotel name",
    "form.enlem": "Latitude", "form.boylam": "Longitude", "form.oteliKaydet": "Save Hotel",
    "form.yeniOtelGirisNotu": "You must log in first to add a new hotel.",
    "form.veya": "or", "form.otelId": "Hotel ID", "form.davetKoduKisa": "Invite Code", "form.kaydiTamamla": "Complete Sign-up",
    "panel.baslik": "Dashboard", "panel.yukleniyor": "Loading dashboard…",
    "yuva.baslik": "Nest / Observation Records", "yuva.konum": "Location",
    "yuva.haritaNotu1": "Tap/click a point on the map to move the marker to the nest's real location.",
    "yuva.konumBelirleniyor": "Determining location…",
    "form.tarih": "Date", "form.durum": "Status",
    "durum.aktif": "Active", "durum.cikisYapti": "Completed", "durum.riskAltinda": "At Risk",
    "form.not": "Note", "form.notYerTutucu": "Observation note (optional)",
    "form.fotografOpsiyonel": "Photo (optional, JPG/PNG, max 10MB)", "form.kaydiEkle": "Add Record",
    "yuva.kayitlarim": "My Records", "yuva.haritadaGoster": "Show on Map",
    "yuva.haritaNotu2": "Click the marker to see the record's date, status and note.",
    "otel.panelBaslik": "Hotel Closing Evidence Panel", "otel.raporIndir": "Download Compliance Report (PDF)",
    "otel.kapanisKanitiFotograf": "Today's closing evidence photo (JPG/PNG, max 10MB)", "otel.fotografiYukle": "Upload Photo",
    "yonetici.panelBaslik": "Hotel Manager Panel", "yonetici.uyelikDurumu": "Membership status:",
    "yonetici.koltuk": "Seats:", "yonetici.kullaniliyor": "in use", "yonetici.davetKodu": "Invite code:",
    "yonetici.koltukSayisi": "Number of seats to purchase", "yonetici.koltukSatinAl": "Buy Seats (Stripe test card)",
    "yonetici.calisanlar": "Staff", "yonetici.bolgeselKayitlar": "Nest Records in Region (7km, everyone's)",
    "footer.metin": "CarettaGuard — Coastal Tourism Sustainability Compliance Platform for Sea Turtle Nesting Areas",
    "puan.detayBaslik": "Points & Badge Details",
    "etki.kumsallar.baslik": "🏖️ Officially Protected Nesting Beaches",
    "etki.kaynak": "Source",
    "etki.plaj": "beaches",
    "etki.maviBayrak.ayrimNotu": "This is a <strong>separate and different certification</strong> from the nesting beach list above — do not confuse the two.",
    "etki.platform.baslik": "📊 The Platform's Own Data",
    "etki.platform.ayrimNotu": "This section is NOT official national data — it is calculated live from real records entered into the platform.",
    "etki.platform.toplamKayit": "total nest/observation records",
    "etki.platform.aktifOtel": "active hotels",
    "etki.platform.bolgeler": "Regions of Our Records",
    "etki.platform.bolgelerNotu": "Regions are grouped automatically based on recorded locations (not entered manually).",
    "sekme.anasayfa": "Home", "sekme.kayitlarim": "My Records", "sekme.harita": "Map", "sekme.otelPaneli": "Hotel Panel",
    "yuva.haritayiGizle": "Hide Map",
  },
};

function diliUygula(dil) {
  state.dil = dil;
  localStorage.setItem("caretta_dil", dil);
  document.documentElement.lang = dil;
  els.dilDegistirBtn.textContent = dil === "tr" ? "ENGLISH" : "TÜRKÇE";

  document.querySelectorAll("[data-i18n]").forEach((el) => {
    const metin = CEVIRI[dil][el.dataset.i18n];
    if (metin === undefined) return;
    const ilkMetinDugumu = Array.from(el.childNodes).find((n) => n.nodeType === Node.TEXT_NODE);
    if (ilkMetinDugumu) {
      ilkMetinDugumu.textContent = metin;
    } else {
      el.textContent = metin;
    }
  });

  document.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
    const metin = CEVIRI[dil][el.dataset.i18nPlaceholder];
    if (metin !== undefined) el.placeholder = metin;
  });

  // Zaten yuklenmis dinamik icerik varsa (giris yapilmissa) yeniden cek ki
  // DURUM/ROZET/MEVSIM etiketleri de yeni dilde gorunsun.
  if (state.token) {
    loadPanelOzeti();
    loadYuvaKayitlari();
    altSekmeleriKur(); // alt sekme etiketleri (Ana Sayfa/Kayitlarim/Harita/Otel Paneli) de yenilensin
  }
  loadKapsamAlani();
}

els.dilDegistirBtn.addEventListener("click", () => {
  diliUygula(state.dil === "tr" ? "en" : "tr");
});

// Kullanicidan gelen metni (ornegin yuva notu) HTML'e gomerken kacis yapar.
// Olmazsa not alanina yazilan <script> gibi bir icerik sayfada CALISIR (XSS).
function kacisliMetin(deger) {
  if (deger === null || deger === undefined) return "";
  return String(deger)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

// Backend siniriyla AYNI deger (application.properties: max-file-size=10MB)
const MAKS_FOTO_BAYT = 10 * 1024 * 1024;

// Dinamik olarak uretilen (backend kod degerlerinden turetilen) Turkce etiketleri,
// dil Ingilizce ise karsiligina cevirir - veri yapilarina DOKUNMADAN sadece son
// gorunen metni degistiren kucuk bir yardimci. Bilinmeyen bir metin gelirse
// (harita disi) oldugu gibi doner, hata vermez.
const ETIKET_CEVIRI = {
  "Aktif": "Active",
  "Çıkış Yaptı": "Completed",
  "Risk Altında": "At Risk",
  "🥉 Bronz Rozet": "🥉 Bronze Badge",
  "🥈 Gümüş Rozet": "🥈 Silver Badge",
  "🥇 Altın Rozet": "🥇 Gold Badge",
  "🐢 Yuvalama Sezonu": "🐢 Nesting Season",
  "Sezon Dışı": "Off Season",
  "Deneme": "Trial",
  "Pasif": "Inactive",
};
function ce(metin) {
  if (state.dil !== "en") return metin;
  return ETIKET_CEVIRI[metin] || metin;
}

// CEVIRI sozlugunden (asagida tanimli) anahtar bazli metin ceker - dinamik
// olarak (innerHTML ile) uretilen bolumlerde (Etkimiz karti, alt sekme
// cubugu vb.) data-i18n ATTRIBUTE'U kullanamadigimiz icin bu yardimciyla
// dogrudan cagrilir. CEVIRI asagida tanimlanir ama bu fonksiyon SADECE
// calisma zamaninda (event/async callback icinde) cagrildigi icin sorun
// olmaz (CEVIRI o ana kadar zaten tanimlanmis olur).
function t(anahtar) {
  return (CEVIRI[state.dil] && CEVIRI[state.dil][anahtar]) || CEVIRI.tr[anahtar] || anahtar;
}

const DURUM_ETIKET = { AKTIF: "Aktif", CIKIS_YAPTI: "Çıkış Yaptı", RISK_ALTINDA: "Risk Altında" };
const DURUM_RENK = { AKTIF: "#2e7d32", CIKIS_YAPTI: "#1565c0", RISK_ALTINDA: "#c62828" };

// Backend'den gelen rozet degerleri (BRONZ/GUMUS/ALTIN) icin gosterim metni ve CSS sinifi.
// esik alani SADECE referans listesi (puanDetayHtmlUret'teki "Rozet Basamaklari") icin -
// Backend'deki Rozet.java'da esik degisirse burasi da GUNCELLENMELI. Odul TURU (partner
// otel indirimi mi, dogrudan maddi odul mu) HENUZ KARARA BAGLANMADI - bu yuzden burada
// (ya da baska hicbir yerde) spesifik bir odul metni YOK, sadece backend'den gelen genel
// odulMesaji gosterilir (bkz. puanDetayHtmlUret).
const ROZET_ETIKET = {
  BRONZ: { etiket: "🥉 Bronz Rozet", sinif: "bronz", esik: 5 },
  GUMUS: { etiket: "🥈 Gümüş Rozet", sinif: "gumus", esik: 20 },
  ALTIN: { etiket: "🥇 Altın Rozet", sinif: "altin", esik: 50 },
};

// Backend'de tarihten hesaplanan mevsim alani (Mevsim.java, Mayis-Eylul = yuvalama sezonu).
const MEVSIM_ETIKET = {
  YUVALAMA_SEZONU: { etiket: "🐢 Yuvalama Sezonu", sinif: "sezonda" },
  SEZON_DISI: { etiket: "Sezon Dışı", sinif: "sezon-disi" },
};

// Uyum orani esikleri mobil uygulama ve PDF raporla AYNI: >=90 yuksek, 70-90 orta, <70 dusuk.
function esikSinifi(oran) {
  if (oran >= 90) return "yuksek";
  if (oran >= 70) return "orta";
  return "dusuk";
}

function setMessage(formName, text, isError) {
  const el = document.querySelector(`.form-message[data-for="${formName}"]`);
  el.textContent = text;
  el.classList.toggle("error", Boolean(isError));
}

function showTab(tab) {
  els.tabBtns.forEach((btn) => btn.classList.toggle("active", btn.dataset.tab === tab));
  els.registerForm.classList.toggle("hidden", tab !== "register");
  els.loginForm.classList.toggle("hidden", tab !== "login");
}

function updateAuthUI() {
  const loggedIn = Boolean(state.token);
  const otelCalisani = loggedIn && state.role === "OTEL_CALISANI" && state.otelId;
  const otelYoneticisi = loggedIn && state.role === "OTEL_YONETICISI" && state.otelId;

  els.authSection.classList.toggle("hidden", loggedIn);
  els.altSekme.classList.toggle("hidden", !loggedIn);
  document.body.classList.toggle("alt-sekme-acik", loggedIn);
  if (loggedIn) altSekmeleriKur();
  els.panelSection.classList.toggle("hidden", !loggedIn);
  els.dashboardSection.classList.toggle("hidden", !loggedIn);
  // Konum secici haritasi, kayit formu ilk gorunur oldugunda (giriste) baslatilir -
  // yuva kaydi eklemeye "baslamak" icin ayri bir adim/buton yok, harita dogrudan hazir.
  if (loggedIn) konumSeciciHaritayiBaslat();
  els.navLoginBtn.classList.toggle("hidden", loggedIn);
  els.navLogoutBtn.classList.toggle("hidden", !loggedIn);
  els.otelPanelSection.classList.toggle("hidden", !otelCalisani);
  els.yoneticiPanelSection.classList.toggle("hidden", !otelYoneticisi);
  if (otelYoneticisi) loadYoneticiPaneli();
  els.userEmailLabel.textContent = state.email ? `Giriş yapan: ${state.email}` : "";

  els.yeniOtelEkleBtn.disabled = !loggedIn;
  els.yeniOtelAdInput.disabled = !loggedIn;
  els.yeniOtelLatInput.disabled = !loggedIn;
  els.yeniOtelLngInput.disabled = !loggedIn;
  els.yeniOtelGirisNotu.classList.toggle("hidden", loggedIn);
}

function saveSession(token, email, role, otelId) {
  state.token = token;
  state.email = email;
  state.role = role || "KULLANICI";
  state.otelId = otelId || null;
  localStorage.setItem("caretta_token", token);
  localStorage.setItem("caretta_email", email);
  localStorage.setItem("caretta_role", state.role);
  if (state.otelId) {
    localStorage.setItem("caretta_otel_id", state.otelId);
  } else {
    localStorage.removeItem("caretta_otel_id");
  }
}

// Cikista sadece localStorage degil, DOM'daki hassas veri de temizlenmeli.
// Aksi halde ortak bir bilgisayarda cikis yapildiktan sonra DevTools'tan
// "hidden" sinifi kaldirilarak onceki kullanicinin e-postasi, yuva kayitlari
// (koordinat + not) ve uyum orani hala goruntulenebilir.
function ekrandakiVeriyiTemizle() {
  els.panelIcerik.innerHTML = "";
  els.yuvaList.innerHTML = "";
  els.otelSelect.innerHTML = "";
  els.uyumOraniDetay.textContent = "";
  els.uyumOraniGauge.textContent = "--%";
  if (haritaKatmani) haritaKatmani.clearLayers();
  els.puanDetayModal.classList.add("hidden");
}

function clearSession() {
  ekrandakiVeriyiTemizle();
  state.token = null;
  state.email = null;
  state.role = null;
  state.otelId = null;
  localStorage.removeItem("caretta_token");
  localStorage.removeItem("caretta_email");
  localStorage.removeItem("caretta_role");
  localStorage.removeItem("caretta_otel_id");
}

async function apiRequest(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const message = data && data.message ? data.message : "Bir hata oluştu";
    throw new Error(message);
  }
  return data;
}

async function apiUpload(path, formData) {
  const headers = {};
  if (state.token) headers.Authorization = `Bearer ${state.token}`;

  const response = await fetch(`${API_BASE}${path}`, { method: "POST", headers, body: formData });
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const message = data && data.message ? data.message : "Bir hata oluştu";
    throw new Error(message);
  }
  return data;
}

async function loadYuvaKayitlari() {
  try {
    const kayitlar = await apiRequest("/api/yuva-kayitlari");
    els.yuvaList.innerHTML = kayitlar.length
      ? kayitlar.map(renderYuvaCard).join("")
      : '<p class="empty">Henüz kayıt yok. Yukarıdaki formla ilk kaydını ekle.</p>';
  } catch (err) {
    // textContent kullaniliyor: hata mesaji ileride kullanici girdisi
    // yansitirsa innerHTML burada XSS acardi.
    els.yuvaList.innerHTML = "";
    const p = document.createElement("p");
    p.className = "empty error";
    p.textContent = err.message;
    els.yuvaList.appendChild(p);
  }
}

function renderYuvaCard(kayit) {
  const durumEtiket = ce(DURUM_ETIKET[kayit.durum] || kacisliMetin(kayit.durum));
  const mevsimEtiket = MEVSIM_ETIKET[kayit.mevsim];
  return `
    <div class="yuva-card durum-${kayit.durum.toLowerCase()}">
      <div class="yuva-card-header">
        <span class="durum-badge">${durumEtiket}</span>
        <span class="tarih">${kacisliMetin(kayit.tarih)}</span>
      </div>
      ${mevsimEtiket ? `<span class="mevsim-badge mevsim-${mevsimEtiket.sinif}">${ce(mevsimEtiket.etiket)}</span>` : ""}
      <p class="konum">📍 ${kayit.latitude}, ${kayit.longitude}</p>
      ${kayit.notlar ? `<p class="not">${kacisliMetin(kayit.notlar)}</p>` : ""}
    </div>
  `;
}

// ---------------------------------------------------------------------------
// ETKI ALANI / KAPSAM ALANIMIZ - herkese acik (giris GEREKMEZ), kisisel veri
// icermez. GET /api/kapsam-alani UC AYRI veri kumesi donuyor - BILINCLI olarak
// ayri kutularda/etiketlerle gosteriliyor, birbirine KARISTIRILMIYOR:
//   1) Resmi korumali kumsal listesi (Tarim ve Orman/Cevre Bakanligi kaynakli)
//   2) Mavi Bayrak sayilari (FEE 2026, AYRI bir sertifika - kumsal listesiyle
//      karistirilmamasi gerektigi acikca belirtilir)
//   3) Platformun KENDI canli verisi (toplam kayit/otel + otomatik bolge
//      gruplama - resmi listeden BAGIMSIZ, veritabanindan hesaplanir)
// ---------------------------------------------------------------------------
async function loadKapsamAlani() {
  try {
    const veri = await apiRequest("/api/kapsam-alani");
    els.etkiAlaniIcerik.innerHTML = kapsamAlaniHtmlUret(veri);
    els.etkiAlaniYukleniyor.classList.add("hidden");
    els.etkiAlaniIcerik.classList.remove("hidden");
  } catch (err) {
    els.etkiAlaniYukleniyor.textContent = "Kapsam alanı bilgisi şu an yüklenemedi.";
  }
}

function kapsamAlaniHtmlUret(veri) {
  const kumsalHtml = veri.resmiKorumaAltindakiKumsallar
    .map(
      (grup) => `
        <div class="etki-il-grubu">
          <span class="etki-il-adi">${kacisliMetin(grup.il)}</span>
          <span class="etki-kumsal-listesi">${grup.kumsallar.map(kacisliMetin).join(", ")}</span>
        </div>`
    )
    .join("");

  const maviBayrakHtml = Object.entries(veri.maviBayrakSayilari)
    .map(([il, sayi]) => `<span class="etki-mavi-bayrak-satir">${kacisliMetin(il)}: <strong>${sayi}</strong> ${t("etki.plaj")}</span>`)
    .join("");

  const bolgeHtml = veri.platformKayitBolgeleri
    .map(
      (b) => `
        <div class="etki-bolge-satir">
          <span>${kacisliMetin(b.bolgeAdi)}${b.il ? ` (${kacisliMetin(b.il)})` : ""}</span>
          <span class="etki-bolge-sayi">${b.kayitSayisi}</span>
        </div>`
    )
    .join("");

  return `
    <div class="etki-kutu">
      <h3 class="etki-kutu-baslik">${t("etki.kumsallar.baslik")}</h3>
      <div class="etki-il-listesi">${kumsalHtml}</div>
      <p class="etki-kaynak-notu">${t("etki.kaynak")}: ${kacisliMetin(veri.resmiVeriKaynagi)}</p>
    </div>

    <div class="etki-kutu etki-kutu-mavi">
      <h3 class="etki-kutu-baslik">🚩 ${veri.maviBayrakYili} ${state.dil === "en" ? "Blue Flag Counts" : "Mavi Bayrak Sayıları"}</h3>
      <p class="etki-ayrim-notu">${t("etki.maviBayrak.ayrimNotu")}</p>
      <div class="etki-mavi-bayrak-listesi">${maviBayrakHtml}</div>
      <p class="etki-kaynak-notu">${t("etki.kaynak")}: ${kacisliMetin(veri.maviBayrakKaynagi)}</p>
    </div>

    <div class="etki-kutu etki-kutu-platform">
      <h3 class="etki-kutu-baslik">${t("etki.platform.baslik")}</h3>
      <p class="etki-ayrim-notu">${t("etki.platform.ayrimNotu")}</p>
      <div class="etki-platform-sayilar">
        <div><span class="etki-platform-buyuk">${veri.platformToplamYuvaKaydiSayisi}</span><span>${t("etki.platform.toplamKayit")}</span></div>
        <div><span class="etki-platform-buyuk">${veri.platformAktifOtelSayisi}</span><span>${t("etki.platform.aktifOtel")}</span></div>
      </div>
      <h4 class="etki-alt-baslik">${t("etki.platform.bolgeler")}</h4>
      <p class="etki-ayrim-notu-kucuk">${t("etki.platform.bolgelerNotu")}</p>
      <div class="etki-bolge-listesi">${bolgeHtml}</div>
    </div>
  `;
}

// ---------------------------------------------------------------------------
// ANA PANEL (dashboard)
// Tek istek: GET /api/panel-ozeti. Backend role gore veri donuyor - normal
// kullaniciya otel/uyum alanlari NULL geliyor, yani veri tarayiciya hic inmiyor.
// Biz de o kartlarin HTML'ini hic URETMIYORUZ (sadece CSS ile gizlemek yeterli degil).
// ---------------------------------------------------------------------------
async function loadPanelOzeti() {
  els.panelYukleniyor.classList.remove("hidden");
  els.panelHata.classList.add("hidden");
  els.panelIcerik.classList.add("hidden");

  try {
    const veri = await apiRequest("/api/panel-ozeti");
    els.panelIcerik.innerHTML = panelHtmlUret(veri);
    els.panelYukleniyor.classList.add("hidden");
    els.panelIcerik.classList.remove("hidden");

    const ekleBtn = document.getElementById("panel-ilk-kayit-btn");
    if (ekleBtn) {
      ekleBtn.addEventListener("click", () => {
        document.getElementById("yuva-form").scrollIntoView({ behavior: "smooth", block: "center" });
      });
    }

    const puanKart = document.getElementById("puan-kart");
    if (puanKart) {
      puanKart.addEventListener("click", puanDetayiniAc);
      puanKart.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          puanDetayiniAc();
        }
      });
    }
  } catch (err) {
    els.panelYukleniyor.classList.add("hidden");
    els.panelHataMesaj.textContent = err.message;
    els.panelHata.classList.remove("hidden");
  }
}

function panelHtmlUret(veri) {
  const rolEtiket = veri.role === "OTEL_CALISANI" ? "Otel Çalışanı" : "Kullanıcı";
  const otelCalisani = veri.role === "OTEL_CALISANI" && veri.otelId !== null && veri.otelId !== undefined;

  let html = `
    <article class="panel-kart">
      <h3 class="panel-kart-baslik">Hesabım</h3>
      <p class="panel-satir"><span class="panel-etiket">E-posta</span><span class="panel-deger">${kacisliMetin(veri.email)}</span></p>
      <p class="panel-satir"><span class="panel-etiket">Rol</span><span class="panel-deger">${rolEtiket}</span></p>
      ${otelCalisani ? `<p class="panel-satir"><span class="panel-etiket">Otel</span><span class="panel-deger">${kacisliMetin(veri.otelAdi)}</span></p>` : ""}
    </article>
  `;

  // Puan ve rozet karti - TUM roller icin uretilir (otel calisani/kullanici farki yok,
  // herkes yuva kaydi ekleyip puan kazanabilir). Rozet backend'de toplam kayit sayisindan
  // anlik hesaplaniyor (bkz. PanelOzetiService), burada sadece gosterimi yapiliyor.
  const toplamPuan = Number(veri.toplamPuan) || 0;
  const rozetBilgi = ROZET_ETIKET[veri.rozet];
  html += `
    <article class="panel-kart puan-kart" id="puan-kart" role="button" tabindex="0" aria-label="Puan ve rozet detayını gör">
      <h3 class="panel-kart-baslik">Puan ve Rozet</h3>
      <p class="panel-sayi">${toplamPuan} <span class="puan-birim">puan</span></p>
      ${rozetBilgi
        ? `<span class="rozet rozet-${rozetBilgi.sinif}">${ce(rozetBilgi.etiket)}</span>`
        : `<p class="panel-detay">Henüz rozet yok · Bronz rozete ${Math.max(0, 5 - Number(veri.yuvaKayitToplam || 0))} kayıt kaldı</p>`}
      <p class="puan-detay-tikla-notu">Detay için tıkla →</p>
    </article>
  `;

  // Uyum karti SADECE otel calisani icin uretilir.
  if (otelCalisani) {
    const oran = Number(veri.uyumOrani) || 0;
    const sinif = esikSinifi(oran);
    const bugunYuklendi = veri.bugunKanitYuklendiMi === true;
    html += `
      <article class="panel-kart uyum-kart">
        <h3 class="panel-kart-baslik">Uyum Durumu</h3>
        <div class="uyum-oran uyum-${sinif}">%${oran.toFixed(1)}</div>
        <div class="uyum-cubuk"><span class="uyum-cubuk-dolu uyum-${sinif}" style="width:${Math.min(Math.max(oran, 0), 100)}%"></span></div>
        <p class="uyum-detay">${kacisliMetin(veri.donemBaslangic)} – ${kacisliMetin(veri.donemBitis)} · ${veri.donemGunSayisi} günde ${veri.kanitYuklenenGunSayisi} gün kanıt</p>
        <span class="rozet ${bugunYuklendi ? "rozet-basarili" : "rozet-uyari"}">
          ${bugunYuklendi ? "✓ Bugün kanıt yüklendi" : "⚠ Bugün kanıt bekleniyor"}
        </span>
      </article>
    `;
  }

  const kayitVar = Number(veri.yuvaKayitToplam) > 0;
  html += `
    <article class="panel-kart">
      <h3 class="panel-kart-baslik">Yuva Kayıtları</h3>
      ${kayitVar
        ? `<p class="panel-sayi">${veri.yuvaKayitToplam}</p>
           <p class="panel-detay">Son kayıt: ${kacisliMetin(veri.sonYuvaKaydiTarih)} · ${ce(DURUM_ETIKET[veri.sonYuvaKaydiDurum] || kacisliMetin(veri.sonYuvaKaydiDurum))}</p>`
        : `<p class="bos-durum-metin">Sahada gözlemlediğin yuvaları kaydederek başla.</p>
           <button type="button" id="panel-ilk-kayit-btn" class="btn btn-primary">İlk Kaydını Ekle</button>`}
    </article>
  `;

  return html;
}

// ---------------------------------------------------------------------------
// PUAN/ROZET DETAY MODALI - "Puan ve Rozet" kartina tiklaninca GET /api/puan-detay
// cekilip modal icinde gosterilir. Puan kazanma aciklamasi burada SABIT/statik
// metin - backend'den kisisel olmayan, genel bir aciklama gelmiyor cunku bu
// herkes icin ayni (mobil tarafta da ayni statik metin kullanilir, tutarlilik icin).
// ---------------------------------------------------------------------------
async function puanDetayiniAc() {
  els.puanDetayIcerik.innerHTML = "<p>Yükleniyor…</p>";
  els.puanDetayModal.classList.remove("hidden");

  try {
    const veri = await apiRequest("/api/puan-detay");
    els.puanDetayIcerik.innerHTML = puanDetayHtmlUret(veri);

    const sertifikaBtn = document.getElementById("katki-sertifikasi-btn");
    if (sertifikaBtn) sertifikaBtn.addEventListener("click", indirKatkiSertifikasi);
    const siralamaBtn = document.getElementById("siralama-goster-btn");
    if (siralamaBtn) siralamaBtn.addEventListener("click", siralamayiGoster);
  } catch (err) {
    els.puanDetayIcerik.innerHTML = `<p class="form-message error">${kacisliMetin(err.message)}</p>`;
  }
}

// fetch ile indirilir cunku Authorization basligi gerekiyor (bkz. indirUyumRaporu,
// ayni desen) - duz bir <a href> linki token gonderemez.
async function indirKatkiSertifikasi() {
  const btn = document.getElementById("katki-sertifikasi-btn");
  if (btn) btn.disabled = true;
  setMessage("sertifika", "Sertifika hazırlanıyor…", false);

  try {
    const response = await fetch(`${API_BASE}/api/katki-sertifikasi`, {
      headers: { Authorization: `Bearer ${state.token}` },
    });

    if (!response.ok) {
      const hata = await response.json().catch(() => null);
      throw new Error(hata && hata.message ? hata.message : "Sertifika indirilemedi");
    }

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "katki-sertifikasi.pdf";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    setMessage("sertifika", "Sertifika indirildi.", false);
  } catch (err) {
    setMessage("sertifika", err.message, true);
  } finally {
    if (btn) btn.disabled = false;
  }
}

function puanDetayHtmlUret(veri) {
  const mevcutRozetBilgi = ROZET_ETIKET[veri.rozet];
  const sonrakiRozetBilgi = ROZET_ETIKET[veri.sonrakiRozet];

  let html = `
    <div class="puan-detay-satir">
      <span class="puan-detay-etiket">Toplam Puan</span>
      <span class="puan-detay-buyuk">${Number(veri.toplamPuan) || 0}</span>
    </div>
    <div class="puan-detay-satir">
      <span class="puan-detay-etiket">Mevcut Rozet</span>
      ${mevcutRozetBilgi
        ? `<span class="rozet rozet-${mevcutRozetBilgi.sinif}">${ce(mevcutRozetBilgi.etiket)}</span>`
        : `<span>Henüz yok</span>`}
    </div>
  `;

  if (sonrakiRozetBilgi && veri.sonrakiRozeteKalanKayit !== null && veri.sonrakiRozeteKalanKayit !== undefined) {
    html += `
      <div class="puan-detay-satir">
        <span class="puan-detay-etiket">Sıradaki Rozet</span>
        <span>${ce(sonrakiRozetBilgi.etiket)} için ${veri.sonrakiRozeteKalanKayit} kayıt daha kaldı</span>
      </div>
    `;
  } else {
    html += `
      <div class="puan-detay-satir">
        <span class="puan-detay-etiket">Sıradaki Rozet</span>
        <span>En üst seviyeye ulaştın 🎉</span>
      </div>
    `;
  }

  // Rozet basamakları - kullanıcının ilerlemesinden BAĞIMSIZ, tüm seviyeleri ve
  // eşiklerini baştan gösteren sabit bir referans listesi. Odul metni YOK (yukarida
  // aciklandigi gibi henuz belirlenmedi), sadece hangi seviyenin kac kayit gerektirdigi
  // ve kazanilip kazanilmadigi gosterilir.
  html += `<div class="puan-detay-basamaklar">`;
  ["BRONZ", "GUMUS", "ALTIN"].forEach((kod) => {
    const bilgi = ROZET_ETIKET[kod];
    const kazanildiMi = veri.rozet && ["BRONZ", "GUMUS", "ALTIN"].indexOf(veri.rozet) >= ["BRONZ", "GUMUS", "ALTIN"].indexOf(kod);
    html += `
      <div class="puan-detay-basamak ${kazanildiMi ? "kazanildi" : ""}">
        <span>${kazanildiMi ? "✓" : "•"} ${ce(bilgi.etiket)} (${bilgi.esik} kayıt)</span>
      </div>
    `;
  });
  html += `</div>`;

  html += `
    <p class="puan-detay-aciklama">
      Puan nasıl kazanılır: yuva/gözlem kaydı eklemek <strong>+10 puan</strong>,
      konumu haritadan seçmek <strong>+5 bonus</strong> kazandırır. Otel çalışanları
      için günlük kapanış kanıtı fotoğrafı yüklemek <strong>+5 puan</strong> kazandırır.
      Rozetler toplam yuva kaydı sayına göre otomatik verilir: Bronz 5, Gümüş 20, Altın 50 kayıt.
    </p>
    <button type="button" id="katki-sertifikasi-btn" class="btn btn-outline-dark puan-detay-sertifika-btn">
      📄 Katkı Sertifikamı İndir (PDF)
    </button>
    <p class="form-message" data-for="sertifika"></p>
    <button type="button" id="siralama-goster-btn" class="btn btn-outline-dark puan-detay-sertifika-btn">
      🏆 Sıralamayı Gör
    </button>
    <div id="siralama-icerik"></div>
  `;

  return html;
}

async function siralamayiGoster() {
  const el = document.getElementById("siralama-icerik");
  el.innerHTML = "<p>Yükleniyor…</p>";
  try {
    const veri = await apiRequest("/api/puan-siralamasi");
    const satirlar = veri.ilkOnlar
      .map((s) => `<div class="calisan-satir"><span>#${s.sira} ${kacisliMetin(s.email)}</span><span>${s.toplamPuan} puan</span></div>`)
      .join("");
    const kendiSira = veri.kullanicininKendiSirasi
      ? `<p><strong>Senin sıran:</strong> #${veri.kullanicininKendiSirasi.sira} — ${veri.kullanicininKendiSirasi.toplamPuan} puan</p>`
      : "";
    el.innerHTML = satirlar + kendiSira;
  } catch (err) {
    el.innerHTML = `<p class="form-message error">${kacisliMetin(err.message)}</p>`;
  }
}

function puanDetayiniKapat() {
  els.puanDetayModal.classList.add("hidden");
}

els.puanDetayKapatBtn.addEventListener("click", puanDetayiniKapat);
els.puanDetayModal.addEventListener("click", (e) => {
  if (e.target === els.puanDetayModal) puanDetayiniKapat();
});
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" && !els.puanDetayModal.classList.contains("hidden")) puanDetayiniKapat();
});

// ---------------------------------------------------------------------------
// UYUM RAPORU (PDF) INDIRME
// fetch ile indirilir cunku Authorization basligi gerekiyor; duz bir <a href>
// linki token gonderemez. Gelen govde blob'a alinip gecici bir link ile indirilir.
// ---------------------------------------------------------------------------
async function indirUyumRaporu() {
  if (!state.otelId) return;
  els.raporIndirBtn.disabled = true;
  setMessage("rapor", "Rapor hazırlanıyor…", false);

  try {
    const response = await fetch(`${API_BASE}/api/otel/${state.otelId}/uyum-raporu`, {
      headers: { Authorization: `Bearer ${state.token}` },
    });

    if (!response.ok) {
      // Hata durumunda govde PDF degil JSON olur.
      const hata = await response.json().catch(() => null);
      throw new Error(hata && hata.message ? hata.message : "Rapor indirilemedi");
    }

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `uyum-raporu-${state.otelId}.pdf`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    setMessage("rapor", "Rapor indirildi.", false);
  } catch (err) {
    setMessage("rapor", err.message, true);
  } finally {
    els.raporIndirBtn.disabled = false;
  }
}

// ---------------------------------------------------------------------------
// HARITA (Leaflet, yerel dosyadan - CDN kullanilmiyor)
// Tile'lar OpenStreetMap'ten geliyor, mobildeki OSMDroid ile ayni kaynak.
// ---------------------------------------------------------------------------
let harita = null;
let haritaKatmani = null;

function haritayiHazirla() {
  if (harita) return;
  harita = L.map("harita").setView([36.89, 30.71], 10);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "© OpenStreetMap katkıda bulunanları",
  }).addTo(harita);
  haritaKatmani = L.layerGroup().addTo(harita);
}

async function haritayiDoldur() {
  haritayiHazirla();
  // Gizliyken olusturulan harita yanlis boyutta kalir; gorunur olunca yenilenmeli.
  harita.invalidateSize();
  haritaKatmani.clearLayers();

  let kayitlar;
  try {
    kayitlar = await apiRequest("/api/yuva-kayitlari");
  } catch (err) {
    return;
  }

  const noktalar = [];
  kayitlar.forEach((kayit) => {
    if (typeof kayit.latitude !== "number" || typeof kayit.longitude !== "number") return;
    const renk = DURUM_RENK[kayit.durum] || "#555";
    const isaretci = L.circleMarker([kayit.latitude, kayit.longitude], {
      radius: 9,
      color: "#ffffff",
      weight: 2,
      fillColor: renk,
      fillOpacity: 0.95,
    });
    isaretci.bindPopup(
      `<strong>${kacisliMetin(kayit.tarih)}</strong><br>` +
      `${ce(DURUM_ETIKET[kayit.durum] || kacisliMetin(kayit.durum))}<br>` +
      `<em>${kayit.notlar ? kacisliMetin(kayit.notlar) : "Not yok"}</em>`
    );
    isaretci.addTo(haritaKatmani);
    noktalar.push([kayit.latitude, kayit.longitude]);
  });

  if (noktalar.length === 1) {
    harita.setView(noktalar[0], 14);
  } else if (noktalar.length > 1) {
    harita.fitBounds(L.latLngBounds(noktalar), { padding: [40, 40] });
  }
}

// ---------------------------------------------------------------------------
// KONUM SECICI (yuva formu) - artik TEK akis: enlem/boylam elle yazilamiyor,
// SADECE haritadan seciliyor. Harita ilk acildiginda tarayici konum izni
// varsa kullanicinin GERCEK konumuna, yoksa/hata olursa Antalya'ya odaklanir
// ve HER durumda bir isaretci hazir gelir (kullanici hic dokunmadan da
// gecerli bir konumla kayit gonderebilir). Kullanici haritaya tiklayarak
// isaretciyi gercek yuva konumuna tasiyabilir. Bu akis tamamen "haritadan
// secim" oldugu icin backend'e her zaman haritadanSecildiMi:true gonderilir
// (GPS'ten gelen baslangic konumu da nihayetinde haritadaki isaretcinin
// kendisidir, kullanici onu degistirmeden de birakabilir).
// ---------------------------------------------------------------------------
const ANTALYA_VARSAYILAN = [36.89, 30.71];

let konumSeciciHarita = null;
let konumSeciciIsaretci = null;
let seciliKonum = null; // { lat, lng } - null ise harita henuz hazir degil demektir

function konumSeciciHaritayiBaslat() {
  if (konumSeciciHarita) return;

  konumSeciciHarita = L.map("konum-secici-harita").setView(ANTALYA_VARSAYILAN, 9);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "© OpenStreetMap katkıda bulunanları",
  }).addTo(konumSeciciHarita);

  konumSeciciHarita.on("click", (e) => {
    konumuUygula(e.latlng.lat, e.latlng.lng, konumSeciciHarita.getZoom());
  });

  // Varsayilan olarak Antalya'da bir isaretci ile basla - GPS izni yoksa ya da
  // konum alinamazsa bile kullanici HER ZAMAN gecerli bir konumla kayit
  // gonderebilsin, uygulama asla "konum yok" durumuna dusmesin.
  konumuUygula(ANTALYA_VARSAYILAN[0], ANTALYA_VARSAYILAN[1], 9);

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => konumuUygula(pos.coords.latitude, pos.coords.longitude, 14),
      () => { /* izin reddedildi/hata - Antalya varsayilaninda kalinir, cokme yok */ },
      { timeout: 8000 }
    );
  }

  // Gizliyken (ornegin cikis yapilmis durumdayken) olusturulmus olabilecegi
  // icin dogru boyutta gorunmesi acisindan bir sonraki "tick"te dogrulanir.
  setTimeout(() => konumSeciciHarita.invalidateSize(), 0);
}

function konumuUygula(lat, lng, zoom) {
  seciliKonum = { lat, lng };
  if (konumSeciciIsaretci) {
    konumSeciciIsaretci.setLatLng([lat, lng]);
  } else {
    konumSeciciIsaretci = L.marker([lat, lng]).addTo(konumSeciciHarita);
  }
  konumSeciciHarita.setView([lat, lng], zoom);
  els.konumSeciliDeger.textContent = `📍 Seçili konum: ${lat.toFixed(6)}, ${lng.toFixed(6)}`;
}

// ---------------------------------------------------------------------------
// ALT SEKME CUBUGU (bottom navigation)
// Sekmeler role gore uretilir: otel calisanina ozel sekme, normal kullanici
// icin DOM'a HIC eklenmez.
// ---------------------------------------------------------------------------
const IKON = {
  anasayfa: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V21h14V9.5"/></svg>',
  liste: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 6h13M8 12h13M8 18h13"/><path d="M3 6h.01M3 12h.01M3 18h.01"/></svg>',
  harita: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21s7-6.2 7-11a7 7 0 1 0-14 0c0 4.8 7 11 7 11Z"/><circle cx="12" cy="10" r="2.6"/></svg>',
  otel: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 20h18"/><path d="M5 20V7l7-4 7 4v13"/><path d="M10 20v-5h4v5"/></svg>',
};

function altSekmeleriKur() {
  const otelCalisani = state.role === "OTEL_CALISANI" && Boolean(state.otelId);

  const sekmeler = [
    { hedef: "panel-section", etiket: t("sekme.anasayfa"), ikon: IKON.anasayfa },
    { hedef: "dashboard-section", etiket: t("sekme.kayitlarim"), ikon: IKON.liste },
    { hedef: "harita-alani", etiket: t("sekme.harita"), ikon: IKON.harita, haritaAc: true },
  ];
  if (otelCalisani) {
    sekmeler.push({ hedef: "otel-panel-section", etiket: t("sekme.otelPaneli"), ikon: IKON.otel });
  }

  els.altSekme.innerHTML = sekmeler
    .map((s) => `<button type="button" class="alt-sekme-btn" data-hedef="${s.hedef}">${s.ikon}<span>${s.etiket}</span></button>`)
    .join("");

  els.altSekme.querySelectorAll(".alt-sekme-btn").forEach((btn, i) => {
    btn.addEventListener("click", async () => {
      const sekme = sekmeler[i];
      // Harita sekmesi: gizliyse once acilir, sonra oraya kaydirilir.
      if (sekme.haritaAc && els.haritaAlani.classList.contains("hidden")) {
        els.haritaAlani.classList.remove("hidden");
        els.haritaToggleBtn.textContent = t("yuva.haritayiGizle");
        await haritayiDoldur();
      }
      const hedefEl = document.getElementById(sekme.hedef);
      if (hedefEl) hedefEl.scrollIntoView({ behavior: "smooth", block: "start" });
      aktifSekmeyiIsaretle(sekme.hedef);
    });
  });

  aktifSekmeyiIsaretle("panel-section");
  gozlemciyiKur(sekmeler.map((s) => s.hedef));
}

function aktifSekmeyiIsaretle(hedef) {
  els.altSekme.querySelectorAll(".alt-sekme-btn").forEach((btn) => {
    btn.classList.toggle("aktif", btn.dataset.hedef === hedef);
  });
}

// Sayfa kaydirilirken hangi bolumdeysek o sekme isaretlensin.
let sekmeGozlemci = null;
function gozlemciyiKur(hedefler) {
  if (sekmeGozlemci) sekmeGozlemci.disconnect();
  if (!("IntersectionObserver" in window)) return;

  sekmeGozlemci = new IntersectionObserver(
    (girdiler) => {
      const gorunur = girdiler.filter((g) => g.isIntersecting)
        .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
      if (gorunur) aktifSekmeyiIsaretle(gorunur.target.id);
    },
    { rootMargin: "-45% 0px -45% 0px", threshold: [0, 0.25, 0.5] }
  );

  hedefler.forEach((id) => {
    const el = document.getElementById(id);
    if (el) sekmeGozlemci.observe(el);
  });
}

async function loadOtelSecenekleri() {
  try {
    const oteller = await apiRequest("/api/oteller");
    const secili = els.otelSelect.value;
    // Otel adi POST /api/oteller ile girilen serbest metin - kacis SART.
    // (Tarayicinin <select> ayristirma kurallari bugun kurtariyor ama bu bir
    //  guvenlik sinirit degil, tesadufi davranis - ona guvenilmez.)
    els.otelSelect.innerHTML = '<option value="">Otel seç...</option>' +
      oteller.map((otel) => `<option value="${kacisliMetin(otel.id)}">${kacisliMetin(otel.ad)}</option>`).join("");
    if (secili) els.otelSelect.value = secili;
  } catch (err) {
    setMessage("yeni-otel", err.message, true);
  }
}

async function loadUyumOrani(otelId) {
  try {
    const veri = await apiRequest(`/api/otel/${otelId}/uyum-orani`);
    els.uyumOraniGauge.style.setProperty("--pct", veri.uyumOrani);
    els.uyumOraniGauge.textContent = `%${veri.uyumOrani}`;
    els.uyumOraniDetay.textContent =
      `${veri.otelAdi} — son ${veri.donemGunSayisi} günde ${veri.kanitYuklenenGunSayisi} gün kanıt yüklendi (${veri.donemBaslangic} – ${veri.donemBitis})`;
  } catch (err) {
    els.uyumOraniDetay.textContent = err.message;
  }
}

const UYELIK_DURUM_ETIKET = { DENEME: "Deneme", AKTIF: "Aktif", PASIF: "Pasif" };

async function loadYoneticiPaneli() {
  if (!state.otelId) return;
  try {
    const durum = await apiRequest(`/api/otel/${state.otelId}/uyelik-durumu`);
    const etiket = ce(UYELIK_DURUM_ETIKET[durum.uyelikDurumu] || durum.uyelikDurumu);
    els.uyelikDurumEtiketi.textContent = durum.premiumMu ? `${etiket} (Premium)` : etiket;
    els.koltukKullanilan.textContent = durum.kullanilanKoltukSayisi;
    els.koltukSatinAlinan.textContent = durum.satinAlinanKoltukSayisi;
    els.yoneticiDavetKodu.textContent = durum.davetKodu || "-";
  } catch (err) {
    els.uyelikDurumEtiketi.textContent = "Yüklenemedi";
  }

  try {
    const calisanlar = await apiRequest(`/api/otel/${state.otelId}/calisanlar`);
    els.calisanListesi.innerHTML = calisanlar.length
      ? calisanlar.map(calisanSatiriUret).join("")
      : '<p class="bos-durum-metin">Henüz çalışan yok. Davet kodunu paylaşarak çalışan ekleyebilirsin.</p>';

    els.calisanListesi.querySelectorAll("[data-calisan-id]").forEach((btn) => {
      btn.addEventListener("click", () => calisanDurumunuDegistir(btn.dataset.calisanId, btn.dataset.yeniDurum === "true"));
    });
  } catch (err) {
    els.calisanListesi.innerHTML = `<p class="form-message error">${kacisliMetin(err.message)}</p>`;
  }

  try {
    const kayitlar = await apiRequest(`/api/otel/${state.otelId}/bolgesel-yuva-kayitlari`);
    els.bolgeselKayitListesi.innerHTML = kayitlar.length
      ? kayitlar.map(bolgeselKayitSatiriUret).join("")
      : '<p class="bos-durum-metin">Bölgede henüz kayıt yok.</p>';
  } catch (err) {
    els.bolgeselKayitListesi.innerHTML = `<p class="form-message error">${kacisliMetin(err.message)}</p>`;
  }
}

function bolgeselKayitSatiriUret(kayit) {
  const durumEtiket = ce(DURUM_ETIKET[kayit.durum] || kacisliMetin(kayit.durum));
  return `
    <div class="calisan-satir">
      <span>📍 ${kayit.latitude.toFixed(4)}, ${kayit.longitude.toFixed(4)} — ${kacisliMetin(kayit.tarih)}</span>
      <span class="rozet rozet-basarili">${durumEtiket}</span>
      <span>${kacisliMetin(kayit.kaydedenEtiketi)}</span>
      ${kayit.notlar ? `<span>${kacisliMetin(kayit.notlar)}</span>` : ""}
    </div>
  `;
}

function calisanSatiriUret(calisan) {
  const durumMetni = calisan.aktif ? "Aktif" : "Pasif";
  const yeniDurum = !calisan.aktif;
  const butonMetni = calisan.aktif ? "Pasif Yap" : "Aktif Yap";
  return `
    <div class="calisan-satir">
      <span>${kacisliMetin(calisan.email)}</span>
      <span class="rozet ${calisan.aktif ? "rozet-basarili" : "rozet-uyari"}">${durumMetni}</span>
      <button type="button" class="btn btn-outline-dark" data-calisan-id="${calisan.id}" data-yeni-durum="${yeniDurum}">${butonMetni}</button>
    </div>
  `;
}

async function calisanDurumunuDegistir(calisanId, yeniDurum) {
  try {
    await apiRequest(`/api/otel/${state.otelId}/calisanlar/${calisanId}/durum`, {
      method: "PATCH",
      body: JSON.stringify({ aktif: yeniDurum }),
    });
    loadYoneticiPaneli();
  } catch (err) {
    setMessage("koltuk-satin-alma", err.message, true);
  }
}

els.koltukSatinAlmaForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const formData = new FormData(els.koltukSatinAlmaForm);
  try {
    const sonuc = await apiRequest(`/api/otel/${state.otelId}/koltuk-satin-alma`, {
      method: "POST",
      body: JSON.stringify({ koltukSayisi: Number(formData.get("koltukSayisi")) }),
    });
    setMessage("koltuk-satin-alma", "Stripe ödeme sayfasına yönlendiriliyorsun…", false);
    window.location.href = sonuc.checkoutUrl;
  } catch (err) {
    setMessage("koltuk-satin-alma", err.message, true);
  }
});

els.tabBtns.forEach((btn) => btn.addEventListener("click", () => showTab(btn.dataset.tab)));

els.heroCta.addEventListener("click", () => {
  els.authSection.scrollIntoView({ behavior: "smooth" });
  showTab("register");
});

els.navLoginBtn.addEventListener("click", () => {
  els.authSection.scrollIntoView({ behavior: "smooth" });
  showTab("login");
});

els.navLogoutBtn.addEventListener("click", () => {
  clearSession();
  updateAuthUI();
});

// ---------------------------------------------------------------------------
// GOOGLE ILE GIRIS - Google Identity Services (GIS). GOOGLE_CLIENT_ID henuz
// gercek bir Google Cloud Console degeriyle DEGISTIRILMEDI - bu placeholder
// kaldigi surece Google butonu sessizce gorunmez/calismaz, sayfanin geri
// kalani ETKILENMEZ.
// ---------------------------------------------------------------------------
const GOOGLE_CLIENT_ID = "BURAYA-GERCEK-GOOGLE-CLIENT-ID-YAZILACAK.apps.googleusercontent.com";
let sonGoogleIdToken = null;

function googleGirisiniBaslat() {
  if (!window.google || GOOGLE_CLIENT_ID.includes("BURAYA-GERCEK")) return; // henuz yapilandirilmadi
  google.accounts.id.initialize({ client_id: GOOGLE_CLIENT_ID, callback: googleGirisCallback });
  google.accounts.id.renderButton(document.getElementById("google-giris-alani"), { theme: "outline", size: "large" });
}

async function googleGirisCallback(response) {
  sonGoogleIdToken = response.credential;
  try {
    const veri = await apiRequest("/api/auth/google", {
      method: "POST",
      body: JSON.stringify({ idToken: sonGoogleIdToken }),
    });
    if (veri.yeniKullaniciMi && !veri.authResponse) {
      document.getElementById("google-rol-secim-alani").classList.remove("hidden");
      setMessage("google", "Hesabın ilk defa giriyor, lütfen kayıt türünü seç.", false);
      return;
    }
    girisSonrasiHazirla(veri.authResponse);
  } catch (err) {
    setMessage("google", err.message, true);
  }
}

document.getElementById("google-tamamla-btn").addEventListener("click", async () => {
  const role = document.getElementById("google-rol-select").value;
  const otelGerekli = role === "OTEL_CALISANI" || role === "OTEL_YONETICISI";
  const otelId = otelGerekli ? Number(document.getElementById("google-otel-id").value) || null : null;
  const otelDavetKodu = otelGerekli ? document.getElementById("google-davet-kodu").value || null : null;

  try {
    const veri = await apiRequest("/api/auth/google", {
      method: "POST",
      body: JSON.stringify({ idToken: sonGoogleIdToken, role, otelId, otelDavetKodu }),
    });
    girisSonrasiHazirla(veri.authResponse);
  } catch (err) {
    setMessage("google", err.message, true);
  }
});

window.addEventListener("load", () => setTimeout(googleGirisiniBaslat, 500));

function girisSonrasiHazirla(data) {
  saveSession(data.token, data.email, data.role, data.otelId);
  updateAuthUI();
  loadPanelOzeti();
  loadYuvaKayitlari();
  if (state.role === "OTEL_CALISANI" && state.otelId) {
    loadUyumOrani(state.otelId);
  }
}

els.registerRole.addEventListener("change", () => {
  const otelGerekli = els.registerRole.value === "OTEL_CALISANI" || els.registerRole.value === "OTEL_YONETICISI";
  els.otelSecimAlani.classList.toggle("hidden", !otelGerekli);
  if (otelGerekli) loadOtelSecenekleri();
});

els.yeniOtelEkleBtn.addEventListener("click", async () => {
  try {
    const otel = await apiRequest("/api/oteller", {
      method: "POST",
      body: JSON.stringify({
        ad: els.yeniOtelAdInput.value,
        latitude: Number(els.yeniOtelLatInput.value),
        longitude: Number(els.yeniOtelLngInput.value),
      }),
    });
    await loadOtelSecenekleri();
    els.otelSelect.value = otel.id;
    els.otelDavetKoduInput.value = otel.davetKodu;
    setMessage(
      "yeni-otel",
      `Otel eklendi. Davet kodu: ${otel.davetKodu} — bu kodu not al, çalışanların kayıt olurken kullanacak.`,
      false
    );
  } catch (err) {
    setMessage("yeni-otel", err.message, true);
  }
});

els.registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const formData = new FormData(els.registerForm);
  const role = formData.get("role") || "KULLANICI";
  const otelGerekli = role === "OTEL_CALISANI" || role === "OTEL_YONETICISI";
  const otelId = otelGerekli ? Number(formData.get("otelId")) || null : null;
  const otelDavetKodu = otelGerekli ? formData.get("otelDavetKodu") || null : null;

  if (otelGerekli && !otelId) {
    setMessage("register", "Lütfen bir otel seçin veya yeni bir otel ekleyin.", true);
    return;
  }

  if (otelGerekli && !otelDavetKodu) {
    setMessage("register", "Otel çalışanı/yöneticisi kaydı için davet kodu zorunlu.", true);
    return;
  }

  try {
    const data = await apiRequest("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({
        email: formData.get("email"),
        password: formData.get("password"),
        role,
        otelId,
        otelDavetKodu,
      }),
    });
    setMessage("register", "", false);
    girisSonrasiHazirla(data);
  } catch (err) {
    setMessage("register", err.message, true);
  }
});

els.loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const formData = new FormData(els.loginForm);
  try {
    const data = await apiRequest("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email: formData.get("email"), password: formData.get("password") }),
    });
    setMessage("login", "", false);
    girisSonrasiHazirla(data);
  } catch (err) {
    setMessage("login", err.message, true);
  }
});

els.kapanisKanitiForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  // Boyut kontrolu ISTEMCIDE de yapiliyor. Nihai kontrol backend'de (400 doner),
  // buradaki sadece kullanici deneyimi icin: hata aninda ve net gorunsun, 10MB'i
  // asan bir dosya bosuna aga verilmesin. Mobil uygulamada da ayni kontrol var.
  const dosya = els.kapanisKanitiForm.querySelector('input[type="file"]').files[0];
  if (dosya && dosya.size > MAKS_FOTO_BAYT) {
    setMessage("kapanis-kaniti", "Fotoğraf 10MB'dan büyük. Lütfen daha düşük çözünürlüklü bir fotoğraf seçin.", true);
    return;
  }

  const formData = new FormData(els.kapanisKanitiForm);
  try {
    await apiUpload("/api/kapanis-kaniti", formData);
    setMessage("kapanis-kaniti", "Kapanış kanıtı yüklendi.", false);
    els.kapanisKanitiForm.reset();
    if (state.otelId) loadUyumOrani(state.otelId);
    loadPanelOzeti();
  } catch (err) {
    setMessage("kapanis-kaniti", err.message, true);
  }
});

els.yuvaForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  // Enlem/boylam artik elle girilmiyor - SADECE haritadaki isaretcinin konumu
  // kullanilir. konumSeciciHaritayiBaslat() girişte hemen bir varsayilan
  // isaretci koydugu icin bu normalde hep dolu olur; yine de savunma amacli
  // kontrol ediliyor (ornegin harita hic baslatilamadiysa).
  if (!seciliKonum) {
    setMessage("yuva", "Konum henüz belirlenemedi, lütfen birkaç saniye bekleyip tekrar deneyin.", true);
    return;
  }

  // Backend artik multipart/form-data bekliyor (opsiyonel fotograf destegi icin) -
  // JSON govde yerine FormData kullanilir, mevcut form alanlari + fotograf birlikte gider.
  const formData = new FormData(els.yuvaForm);
  const gonderilecek = new FormData();
  gonderilecek.set("latitude", seciliKonum.lat);
  gonderilecek.set("longitude", seciliKonum.lng);
  gonderilecek.set("tarih", formData.get("tarih"));
  gonderilecek.set("durum", formData.get("durum"));
  if (formData.get("notlar")) gonderilecek.set("notlar", formData.get("notlar"));
  gonderilecek.set("haritadanSecildiMi", "true");
  const fotografDosyasi = formData.get("fotograf");
  if (fotografDosyasi && fotografDosyasi.size > 0) {
    if (fotografDosyasi.size > MAKS_FOTO_BAYT) {
      setMessage("yuva", "Fotoğraf 10MB'dan büyük. Lütfen daha düşük çözünürlüklü bir fotoğraf seçin.", true);
      return;
    }
    gonderilecek.set("fotograf", fotografDosyasi);
  }

  try {
    await apiUpload("/api/yuva-kayitlari", gonderilecek);
    setMessage("yuva", "Kayıt eklendi.", false);
    els.yuvaForm.reset();
    loadYuvaKayitlari();
    loadPanelOzeti();
    if (!els.haritaAlani.classList.contains("hidden")) haritayiDoldur();
  } catch (err) {
    setMessage("yuva", err.message, true);
  }
});

// Yeni olay dinleyicileri
els.haritaToggleBtn.addEventListener("click", () => {
  const gizli = els.haritaAlani.classList.toggle("hidden");
  els.haritaToggleBtn.textContent = gizli ? t("yuva.haritadaGoster") : t("yuva.haritayiGizle");
  if (!gizli) haritayiDoldur();
});

els.raporIndirBtn.addEventListener("click", indirUyumRaporu);
els.panelTekrarBtn.addEventListener("click", loadPanelOzeti);

// Sayfa lang="tr" ile yazildigi icin ilk uygulamada data-i18n elemanlari zaten
// dogru Turkce metni tasiyor - ama kullanici daha once Ingilizce secmisse
// (localStorage'da kayitliysa) sayfa acilir acilmaz Ingilizceye gecsin diye
// yine de cagrilir; diliUygula kendi ic yukleme cagrilarini yapar.
diliUygula(state.dil);
updateAuthUI();
loadKapsamAlani(); // herkese acik, giris durumundan BAGIMSIZ her zaman yuklenir
if (state.token) {
  loadPanelOzeti();
  loadYuvaKayitlari();
  if (state.role === "OTEL_CALISANI" && state.otelId) {
    loadUyumOrani(state.otelId);
  }
}
