const API_BASE = "http://localhost:8080";

const state = {
  token: localStorage.getItem("caretta_token") || null,
  email: localStorage.getItem("caretta_email") || null,
  role: localStorage.getItem("caretta_role") || null,
  otelId: localStorage.getItem("caretta_otel_id") || null,
  // Konum haritadan mi secildi - backend'de +5 bonus puan tetikliyor (bkz.
  // mobildeki ayni mantik, YuvaKayitActivity.sonKonumHaritadanSecildiMi).
  // Kullanici lat/lng alanini elle degistirirse false'a sifirlanir.
  yuvaHaritadanSecildiMi: false,
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
  yuvaLatInput: document.getElementById("yuva-lat-input"),
  yuvaLngInput: document.getElementById("yuva-lng-input"),
  konumSeciciToggleBtn: document.getElementById("konum-secici-toggle-btn"),
  konumSeciciAlani: document.getElementById("konum-secici-alani"),
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
};

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

const DURUM_ETIKET = { AKTIF: "Aktif", CIKIS_YAPTI: "Çıkış Yaptı", RISK_ALTINDA: "Risk Altında" };
const DURUM_RENK = { AKTIF: "#2e7d32", CIKIS_YAPTI: "#1565c0", RISK_ALTINDA: "#c62828" };

// Backend'den gelen rozet degerleri (BRONZ/GUMUS/ALTIN) icin gosterim metni ve CSS sinifi.
// Esikler backend'de (Rozet.hesapla) sabit: Bronz 5, Gumus 20, Altin 50 yuva kaydi.
const ROZET_ETIKET = {
  BRONZ: { etiket: "🥉 Bronz Rozet", sinif: "bronz" },
  GUMUS: { etiket: "🥈 Gümüş Rozet", sinif: "gumus" },
  ALTIN: { etiket: "🥇 Altın Rozet", sinif: "altin" },
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

  els.authSection.classList.toggle("hidden", loggedIn);
  els.altSekme.classList.toggle("hidden", !loggedIn);
  document.body.classList.toggle("alt-sekme-acik", loggedIn);
  if (loggedIn) altSekmeleriKur();
  els.panelSection.classList.toggle("hidden", !loggedIn);
  els.dashboardSection.classList.toggle("hidden", !loggedIn);
  els.navLoginBtn.classList.toggle("hidden", loggedIn);
  els.navLogoutBtn.classList.toggle("hidden", !loggedIn);
  els.otelPanelSection.classList.toggle("hidden", !otelCalisani);
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
  const durumEtiket = DURUM_ETIKET[kayit.durum] || kacisliMetin(kayit.durum);
  const mevsimEtiket = MEVSIM_ETIKET[kayit.mevsim];
  return `
    <div class="yuva-card durum-${kayit.durum.toLowerCase()}">
      <div class="yuva-card-header">
        <span class="durum-badge">${durumEtiket}</span>
        <span class="tarih">${kacisliMetin(kayit.tarih)}</span>
      </div>
      ${mevsimEtiket ? `<span class="mevsim-badge mevsim-${mevsimEtiket.sinif}">${mevsimEtiket.etiket}</span>` : ""}
      <p class="konum">📍 ${kayit.latitude}, ${kayit.longitude}</p>
      ${kayit.notlar ? `<p class="not">${kacisliMetin(kayit.notlar)}</p>` : ""}
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
    <article class="panel-kart puan-kart">
      <h3 class="panel-kart-baslik">Puan ve Rozet</h3>
      <p class="panel-sayi">${toplamPuan} <span class="puan-birim">puan</span></p>
      ${rozetBilgi
        ? `<span class="rozet rozet-${rozetBilgi.sinif}">${rozetBilgi.etiket}</span>`
        : `<p class="panel-detay">Henüz rozet yok · Bronz rozete ${Math.max(0, 5 - Number(veri.yuvaKayitToplam || 0))} kayıt kaldı</p>`}
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
           <p class="panel-detay">Son kayıt: ${kacisliMetin(veri.sonYuvaKaydiTarih)} · ${DURUM_ETIKET[veri.sonYuvaKaydiDurum] || kacisliMetin(veri.sonYuvaKaydiDurum)}</p>`
        : `<p class="bos-durum-metin">Sahada gözlemlediğin yuvaları kaydederek başla.</p>
           <button type="button" id="panel-ilk-kayit-btn" class="btn btn-primary">İlk Kaydını Ekle</button>`}
    </article>
  `;

  return html;
}

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
      `${DURUM_ETIKET[kayit.durum] || kacisliMetin(kayit.durum)}<br>` +
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
// KONUM SECICI (yuva formu icin "Haritadan Sec") - mobildeki KonumSecActivity
// ile ayni amac: haritaya tiklayinca enlem/boylam otomatik doluyor ve backend'e
// haritadanSecildiMi:true gonderilip +5 bonus puan tetikleniyor.
// ---------------------------------------------------------------------------
let konumSeciciHarita = null;
let konumSeciciIsaretci = null;

function konumSeciciHaritayiHazirla() {
  if (konumSeciciHarita) return;
  konumSeciciHarita = L.map("konum-secici-harita").setView([36.89, 30.71], 9);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "© OpenStreetMap katkıda bulunanları",
  }).addTo(konumSeciciHarita);

  konumSeciciHarita.on("click", (e) => {
    konumdanSecilenNoktayiUygula(e.latlng.lat, e.latlng.lng);
  });
}

function konumdanSecilenNoktayiUygula(lat, lng) {
  // NOT: .value'ya programatik atama tarayicida "input" olayini TETIKLEMEZ,
  // yani asagidaki elle-duzenleme dinleyicisi burada devreye girmez - bayrak
  // dogrudan true kalir. Deger atamasindan SONRA true yazmak yine de mobildeki
  // (KonumSecActivity) sirayla tutarli olsun diye tercih edildi.
  els.yuvaLatInput.value = lat.toFixed(6);
  els.yuvaLngInput.value = lng.toFixed(6);
  state.yuvaHaritadanSecildiMi = true;

  if (konumSeciciIsaretci) {
    konumSeciciIsaretci.setLatLng([lat, lng]);
  } else {
    konumSeciciIsaretci = L.marker([lat, lng]).addTo(konumSeciciHarita);
  }
}

els.konumSeciciToggleBtn.addEventListener("click", () => {
  const gizli = els.konumSeciciAlani.classList.toggle("hidden");
  els.konumSeciciToggleBtn.textContent = gizli ? "Haritadan Seç" : "Haritayı Gizle";
  if (!gizli) {
    konumSeciciHaritayiHazirla();
    // Gizliyken olusturulan/guncellenen harita yanlis boyutta kalir.
    setTimeout(() => konumSeciciHarita.invalidateSize(), 0);
  }
});

// Kullanici enlem/boylami ELLE degistirirse (haritadan secim sonrasi bile
// olsa) bonus puan bayragi sifirlanir - aksi halde haritadan secip sonra
// elle duzenleyen kullanici haksiz bonus almaya devam eder (mobildeki
// TextWatcher ile ayni mantik).
els.yuvaLatInput.addEventListener("input", () => { state.yuvaHaritadanSecildiMi = false; });
els.yuvaLngInput.addEventListener("input", () => { state.yuvaHaritadanSecildiMi = false; });

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
    { hedef: "panel-section", etiket: "Ana Sayfa", ikon: IKON.anasayfa },
    { hedef: "dashboard-section", etiket: "Kayıtlarım", ikon: IKON.liste },
    { hedef: "harita-alani", etiket: "Harita", ikon: IKON.harita, haritaAc: true },
  ];
  if (otelCalisani) {
    sekmeler.push({ hedef: "otel-panel-section", etiket: "Otel Paneli", ikon: IKON.otel });
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
        els.haritaToggleBtn.textContent = "Haritayı Gizle";
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
  const otelCalisani = els.registerRole.value === "OTEL_CALISANI";
  els.otelSecimAlani.classList.toggle("hidden", !otelCalisani);
  if (otelCalisani) loadOtelSecenekleri();
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
  const otelId = role === "OTEL_CALISANI" ? Number(formData.get("otelId")) || null : null;
  const otelDavetKodu = role === "OTEL_CALISANI" ? formData.get("otelDavetKodu") || null : null;

  if (role === "OTEL_CALISANI" && !otelId) {
    setMessage("register", "Lütfen bir otel seçin veya yeni bir otel ekleyin.", true);
    return;
  }

  if (role === "OTEL_CALISANI" && !otelDavetKodu) {
    setMessage("register", "Otel çalışanı kaydı için davet kodu zorunlu.", true);
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
  const formData = new FormData(els.yuvaForm);
  try {
    await apiRequest("/api/yuva-kayitlari", {
      method: "POST",
      body: JSON.stringify({
        latitude: Number(formData.get("latitude")),
        longitude: Number(formData.get("longitude")),
        tarih: formData.get("tarih"),
        durum: formData.get("durum"),
        notlar: formData.get("notlar") || null,
        haritadanSecildiMi: state.yuvaHaritadanSecildiMi,
      }),
    });
    setMessage("yuva", "Kayıt eklendi.", false);
    els.yuvaForm.reset();
    state.yuvaHaritadanSecildiMi = false;
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
  els.haritaToggleBtn.textContent = gizli ? "Haritada Göster" : "Haritayı Gizle";
  if (!gizli) haritayiDoldur();
});

els.raporIndirBtn.addEventListener("click", indirUyumRaporu);
els.panelTekrarBtn.addEventListener("click", loadPanelOzeti);

updateAuthUI();
if (state.token) {
  loadPanelOzeti();
  loadYuvaKayitlari();
  if (state.role === "OTEL_CALISANI" && state.otelId) {
    loadUyumOrani(state.otelId);
  }
}
