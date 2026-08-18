const API_BASE = "http://localhost:8080";

const state = {
  token: localStorage.getItem("caretta_token") || null,
  email: localStorage.getItem("caretta_email") || null,
  role: localStorage.getItem("caretta_role") || null,
  otelId: localStorage.getItem("caretta_otel_id") || null,
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
  userEmailLabel: document.getElementById("user-email-label"),
  registerRole: document.getElementById("register-role"),
  otelSecimAlani: document.getElementById("otel-secim-alani"),
  otelSelect: document.getElementById("otel-select"),
  yeniOtelAdInput: document.getElementById("yeni-otel-ad"),
  yeniOtelLatInput: document.getElementById("yeni-otel-lat"),
  yeniOtelLngInput: document.getElementById("yeni-otel-lng"),
  yeniOtelEkleBtn: document.getElementById("yeni-otel-ekle-btn"),
  yeniOtelGirisNotu: document.getElementById("yeni-otel-giris-notu"),
  otelPanelSection: document.getElementById("otel-panel-section"),
  uyumOraniGauge: document.getElementById("uyum-orani-gauge"),
  uyumOraniDetay: document.getElementById("uyum-orani-detay"),
  kapanisKanitiForm: document.getElementById("kapanis-kaniti-form"),
};

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

function clearSession() {
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
    els.yuvaList.innerHTML = `<p class="empty error">${err.message}</p>`;
  }
}

function renderYuvaCard(kayit) {
  const durumEtiket = { AKTIF: "Aktif", CIKIS_YAPTI: "Çıkış Yaptı", RISK_ALTINDA: "Risk Altında" }[kayit.durum] || kayit.durum;
  return `
    <div class="yuva-card durum-${kayit.durum.toLowerCase()}">
      <div class="yuva-card-header">
        <span class="durum-badge">${durumEtiket}</span>
        <span class="tarih">${kayit.tarih}</span>
      </div>
      <p class="konum">📍 ${kayit.latitude}, ${kayit.longitude}</p>
      ${kayit.notlar ? `<p class="not">${kayit.notlar}</p>` : ""}
    </div>
  `;
}

async function loadOtelSecenekleri() {
  try {
    const oteller = await apiRequest("/api/oteller");
    const secili = els.otelSelect.value;
    els.otelSelect.innerHTML = '<option value="">Otel seç...</option>' +
      oteller.map((otel) => `<option value="${otel.id}">${otel.ad}</option>`).join("");
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
    setMessage("yeni-otel", "Otel eklendi ve seçildi.", false);
  } catch (err) {
    setMessage("yeni-otel", err.message, true);
  }
});

els.registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const formData = new FormData(els.registerForm);
  const role = formData.get("role") || "KULLANICI";
  const otelId = role === "OTEL_CALISANI" ? Number(formData.get("otelId")) || null : null;

  if (role === "OTEL_CALISANI" && !otelId) {
    setMessage("register", "Lütfen bir otel seçin veya yeni bir otel ekleyin.", true);
    return;
  }

  try {
    const data = await apiRequest("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ email: formData.get("email"), password: formData.get("password"), role, otelId }),
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
  const formData = new FormData(els.kapanisKanitiForm);
  try {
    await apiUpload("/api/kapanis-kaniti", formData);
    setMessage("kapanis-kaniti", "Kapanış kanıtı yüklendi.", false);
    els.kapanisKanitiForm.reset();
    if (state.otelId) loadUyumOrani(state.otelId);
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
      }),
    });
    setMessage("yuva", "Kayıt eklendi.", false);
    els.yuvaForm.reset();
    loadYuvaKayitlari();
  } catch (err) {
    setMessage("yuva", err.message, true);
  }
});

updateAuthUI();
if (state.token) {
  loadYuvaKayitlari();
  if (state.role === "OTEL_CALISANI" && state.otelId) {
    loadUyumOrani(state.otelId);
  }
}
