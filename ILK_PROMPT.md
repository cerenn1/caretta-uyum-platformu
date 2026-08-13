Bu metni "Ask Claude to edit..." kutusuna kopyala-yapıştır:

---

CLAUDE.md dosyasını oku ve projenin tüm bağlamını (fikir, mimari kararlar,
teknoloji yığını, zorunlu kurallar) anla.

Buna göre sırayla ilerle:
1. Java + Spring Boot ile temel proje iskeletini oluştur (Maven kullan,
   CLAUDE.md'deki paket yapısına uygun: auth, yuvatakip vb.)
2. Spring Data JPA'yı PostgreSQL için kur, temel entity'leri oluştur
   (User, YuvaKaydi)
3. Basit bir kayıt (register) ve giriş (login) endpoint'i yaz, Spring
   Security + JWT kullan
4. Yuva/gözlem kaydı ekleme ve listeleme endpoint'lerini yaz
5. Basit bir landing page ekle (ayrı bir frontend/ klasöründe, düz
   HTML/CSS/JavaScript yeterli)

Her adımda CLAUDE.md'deki zorunlu kurallara (Conventional Commits + scope,
.env dosyasının commit edilmemesi, Docker uyumluluğu) mutlaka uy.

Ben Java biliyorum ama Spring Boot'u yeni öğreniyorum — her önemli kod
parçasını yazdıktan sonra ne işe yaradığını 2-3 cümleyle basit bir dille
açıkla. Adım adım ilerle, büyük kararlar öncesinde (örn. ek bir kütüphane
eklemeden önce) bana sor.
