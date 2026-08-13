# CLAUDE.md — Proje Bağlamı (Claude Code için)

Bu dosya, VSCode'daki Claude Code eklentisinin projeyi anlaması için hazırlanmıştır. Kod yazarken bu dosyadaki kurallara **kesinlikle** uy.

## Proje Nedir

**Kaplumbağa Yuvalama Bölgeleri için Kıyı Turizmi Sürdürülebilirlik Uyum Platformu.** Antalya ve çevresindeki caretta caretta yuvalama sahillerindeki otellerin ve halk plajlarının yasal koruma yükümlülüklerine (gece 20:00–08:00 sahil giriş yasağı, ışıklandırma kısıtlaması) uyup uymadığını izleyen, ihlal riskini önceden haber veren ve denetim/sertifikasyon (Travelife, Green Key) için otomatik rapor üreten bir B2B/B2G SaaS.

Tam proje planı (problem, gelir modeli, mimari gerekçeler, riskler) `docs/proje_plani.md` içinde — kod yazmadan önce oraya bak.

## Zorunlu Kurallar (Staj Sorumlusu Talebi — Pazarlık Konusu Değil)

1. **Conventional Commits + scope zorunlu.** Format: `tip(scope): açıklama`
   - Örnek: `feat(auth): kayıt formu endpoint'i eklendi`
   - Örnek: `fix(yuva-takip): koordinat doğrulama hatası düzeltildi`
   - Kullanılabilecek tipler: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `perf`
2. **`.env` dosyaları asla commit edilmeyecek.** `.gitignore`'da olmalı, repo'da sadece `.env.example` (gerçek değer olmadan) bulunur.
3. **API anahtarları / secret'lar koda gömülmeyecek.** Sadece `.env` üzerinden `process.env.X` gibi okunacak.
4. **Proje Docker + docker-compose ile çalışacak.** `docker compose up` tek komutuyla ayağa kalkmalı.
5. **Veritabanı: PostgreSQL (Supabase üzerinden), `pgvector`/PostGIS destekli.** Gerekçe: proje konumsal veri (yuva koordinatları, koruma bölgesi sınırları) üzerine kurulu; PostGIS "bu nokta koruma bölgesi içinde mi" gibi sorguları doğrudan destekliyor. NoSQL (MongoDB/Firebase) burada doğru seçim değil çünkü veriler arasında güçlü ilişkisel bağ (kullanıcı → otel → yuva kaydı → rapor) var.

## Teknoloji Yığını (Karar Verildi)

- **Backend:** Java + Spring Boot — Clean Architecture'ı katman katman (controller/service/repository) uygulamaya uygun, staj sorumlusunun kaynaklarında en çok önerilen yığın.
- **Build aracı:** Maven.
- **ORM:** Spring Data JPA (Hibernate).
- **Veritabanı:** PostgreSQL (Supabase) + PostGIS.
- **Cache:** Redis.
- **Kimlik doğrulama:** Spring Security + JWT.
- **Container:** Docker + docker-compose.
- **Frontend (MVP için minimal):** basit bir HTML/CSS/JavaScript landing page + kayıt formu yeterli (ayrı bir `frontend/` klasöründe), karmaşık bir SPA gerekmiyor.

## Mimari

**Modular Monolith + Clean Architecture.** Modüller: `auth`, `yuvatakip`, `isikuyum`, `raporlama`, `bildirim`, `puansistemi`.

Klasör yapısı önerisi (Spring Boot paket bazlı):
```
src/main/java/com/caretta/proje/
  auth/
    controller/
    service/
    repository/
    entity/
  yuvatakip/
    controller/
    service/
    repository/
    entity/
  isikuyum/
  raporlama/
  bildirim/
  puansistemi/
  common/
  CarettaApplication.java
```

Kullanılacak tasarım desenleri (nerede/neden):
- **Repository Pattern** — her modülde veritabanı erişimini iş mantığından ayırmak için (Spring Data JPA repository arayüzleri).
- **Strategy** — sertifikasyon standardına göre farklı rapor formatı üretmek için.
- **Observer** — risk tespit edildiğinde ilgili kullanıcıya bildirim göndermek için.
- **Chain of Responsibility** — vatandaş bildiriminin doğrulama akışında (gönderim → moderasyon → onay).

## Cuma Teslim Kapsamı (MVP — Sadece Bunlar)

Staj sorumlusunun istediği minimum lokal versiyon, **başka hiçbir şey değil**:

1. Landing page (proje ne yapıyor, tek ekran).
2. Kayıt formu (email + şifre ile basit `register` endpoint'i).
3. Başarılı kayıt akışı (kayıt olunca giriş yapılabiliyor, JWT dönüyor).
4. Ana özellik: **yuva/gözlem kaydı ekleme** — giriş yapmış kullanıcı bir yuva/gözlem kaydını (konum, tarih, not) sisteme ekleyebiliyor ve eklediği kayıtları listeleyebiliyor.

**Not:** Kesin bir teslim tarihi henüz teyit edilmedi (staj sorumlusuyla netleştirilecek) — yukarıdaki 4 madde yine de mantıklı bir ilk MVP kapsamı olarak kalıyor, aceleye gerek yok.

Bunun dışındaki her şey (risk çapraz kontrolü, Claude ile rapor üretimi, puan sistemi, vatandaş modülü) **sonraki fazlara** bırakılıyor — şimdi kapsamı büyütmeye çalışma, önce bu 4 madde çalışsın.

## Terim Sözlüğü

Staj sorumlusunun paylaştığı mimari terimlerin (Monolith, Microservices, CQRS, JWT, RAG, Redis, Kafka vb.) birer-ikişer cümlelik açıklamaları `docs/terim_sozlugu.md` içinde — kullanmak zorunlu değil ama sunumda sorulursa bilinmeli.
