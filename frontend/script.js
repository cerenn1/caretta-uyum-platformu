const API_BASE = "http://localhost:8080";

const state = {
  token: localStorage.getItem("caretta_token") || null,
  email: localStorage.getItem("caretta_email") || null,
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
  els.authSection.classList.toggle("hidden", loggedIn);
  els.dashboardSection.classList.toggle("hidden", !loggedIn);
  els.navLoginBtn.classList.toggle("hidden", loggedIn);
  els.navLogoutBtn.classList.toggle("hidden", !loggedIn);
  els.userEmailLabel.textContent = state.email ? `Giriş yapan: ${state.email}` : "";
}

function saveSession(token, email) {
  state.token = token;
  state.email = email;
  localStorage.setItem("caretta_token", token);
  localStorage.setItem("caretta_email", email);
}

function clearSession() {
  state.token = null;
  state.email = null;
  localStorage.removeItem("caretta_token");
  localStorage.removeItem("caretta_email");
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

els.registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const formData = new FormData(els.registerForm);
  try {
    const data = await apiRequest("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ email: formData.get("email"), password: formData.get("password") }),
    });
    saveSession(data.token, data.email);
    setMessage("register", "", false);
    updateAuthUI();
    loadYuvaKayitlari();
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
    saveSession(data.token, data.email);
    setMessage("login", "", false);
    updateAuthUI();
    loadYuvaKayitlari();
  } catch (err) {
    setMessage("login", err.message, true);
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
if (state.token) loadYuvaKayitlari();
