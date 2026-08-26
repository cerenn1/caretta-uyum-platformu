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
- **Frontend:** Proje mobil uygulamaya da geçti (aşağıdaki "Mobil Geçiş" bölümüne bak), AMA web arayüzü (`frontend/`, HTML/CSS/JS) **hâlâ aktif geliştiriliyor** — 17 Ağustos 2026'daki ilk mobil geçiş kararının aksine, sonraki oturumlarda kullanıcı her yeni özelliği önce web'de yapıp test etmeyi, SONRA mobile (OSMDroid/Java ile) birebir aynı davranışla taşımayı istedi. Yeni bir özellik isteğinde, aksi açıkça belirtilmedikçe ÖNCE web'de uygula ve test et, SONRA mobile geç — `frontend/`'i "eski/referans" sanıp atlama.

## Mimari

**Modular Monolith + Clean Architecture.** Modüller: `auth`, `yuvatakip`, `isikuyum`, `raporlama`, `bildirim`, `puansistemi`, `uyelik`, `istatistik`.

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

## Güncellenmiş Öncelik (Staj Sorumlusunun Geri Bildirimi)

Staj sorumlusu, projenin **daha çok otel çalışanlarına ve otellere yönelik, ticari amaçlı** ilerlemesini istiyor. Bu, yukarıdaki MVP'nin üzerine eklenen bir sonraki adım (Faz 1.5):

**Yeni özellik: Otel Kapanış Kanıtı ve Uyum Oranı Modülü**

- Yeni kullanıcı rolü: `OTEL_CALISANI` (User entity'sine `role` alanı eklenir).
- Yeni entity: `Otel` (id, ad, konum) — bir kullanıcı bir otele bağlı olur (`User.otelId`).
- Yeni entity: `KapanisKaniti` (id, otelId, kullaniciId, tarih, fotoğrafUrl/dosyaYolu, oluşturulmaZamani).
- Endpoint: `POST /api/kapanis-kaniti` — otel çalışanı, o günkü kapanış kanıtı fotoğrafını yükler.
- Endpoint: `GET /api/otel/{id}/uyum-orani` — o otelin, belirli bir dönemdeki (örn. son 30 gün) kanıt yükleme oranını yüzde olarak hesaplayıp döner. Hesaplama: (kanıt yüklenen gün sayısı / dönemdeki toplam gün sayısı) × 100.
- Frontend: Otel çalışanının günlük fotoğraf yükleyebildiği basit bir panel + o otelin güncel uyum oranını gösteren bir gösterge.

**Ticari çerçeve (staj sorumlusuna anlatım için):** Bu modül, platformun asıl para karşılığı sunduğu değer — otel, bu uyum oranı raporunu hem iç denetimde hem uluslararası eko-sertifika (Travelife, Green Key) başvurusunda somut kanıt olarak kullanıyor. Gelir modeli: otel başına yıllık abonelik (bkz. `docs/proje_plani.md` Bölüm 7).

Bunun dışındaki her şey (risk çapraz kontrolü, Claude ile rapor üretimi, puan sistemi, vatandaş modülü) **sonraki fazlara** bırakılıyor — şimdi kapsamı büyütmeye çalışma, önce bu 4 madde çalışsın.

## Ticari Yol Haritası (Sonraki Fazlar — Şimdi Kodlama, Sadece Plan)

Kapanış kanıtı modülü tek ticari özellik olmasın diye, otel/otel çalışanı odaklı ek ticari fikirler burada **belgeleniyor**. Aşağıdakilerin hiçbiri şu an Claude Code'a yazdırılmıyor — kapanış kanıtı modülü bitip test edilmeden bunlara başlanmayacak.

1. **Otomatik Sertifika Raporu (PDF):** Uyum oranı verisinden, Green Key/Travelife başvurusunda kullanılabilecek resmi bir PDF rapor otomatik üretilir. Otelin normalde danışmanlık ücreti ödediği bir işi otomatikleştirir — premium katman.
2. **Zincir Otel Paneli:** Birden fazla tesisi olan otel gruplarının tüm otellerini tek panelden yönetmesi. Daha büyük müşteri = daha büyük sözleşme.
3. **Bölgesel Karşılaştırma (Benchmarking):** "Bölgenizdeki otellerin ortalama uyum oranı %X, siz %Y'desiniz." Oteli daha fazla kullanmaya/ödemeye teşvik eden rekabet unsuru.
4. **Misafire Açık Şeffaflık Sayfası:** Otelin kendi sitesine ekleyebileceği, güncel uyum oranını gösteren halka açık sayfa — pazarlama aracı (gezginlerin sürdürülebilirliğe önem verdiği araştırmayla destekleniyor, bkz. `docs/proje_plani.md`).
5. **Otomatik Hatırlatma Sistemi:** Otel birkaç gündür kanıt yüklemediyse otomatik uyarı — aboneliği aktif kullanılan bir araç haline getirip iptal riskini azaltır.

## Mobil Geçiş (17 Ağustos 2026 — Güncel Durum)

Staj sorumlusu projenin **web değil, mobil uygulama (Android)** olarak geliştirilmesini istedi. Bu bölüm, VSCode dışında (Android Studio'da) yapılan hazırlığın güncel durumunu özetliyor — Claude Code kod yazmaya başlamadan önce bunu bilmeli.

**Yapılanlar:**
- Android Studio kuruldu, yeni bir Android projesi oluşturuldu: proje adı `CarettaApp`, konumu `C:\Users\Lenovo\AndroidStudioProjects\CarettaApp` (bu proje, backend'in bulunduğu `CarettaProje` klasöründen **ayrı bir klasörde**).
- Dil: **Java** (Kotlin değil — kullanıcı Java biliyor, backend de zaten Java).
- Şablon: Empty Views Activity (klasik XML layout tabanlı, Jetpack Compose değil).
- Paket adı: `com.caretta.proje` — backend ile aynı kök, tutarlılık için.
- Minimum SDK: API 24 (Android 7.0).
- Test için sanal cihaz (emülatör) oluşturuldu: "Medium Phone", Android 17.0 ("CinnamonBun").
- İlk `MainActivity.java` (boş/varsayılan) oluşturuldu ve emülatörde ilk kez çalıştırıldı.

**Sıradaki adımlar (Claude Code'un yapması gerekenler):**
1. Mevcut backend'deki (Spring Boot, `CarettaProje` klasörü) endpoint'lere bağlanacak gerçek ekranları yaz: kayıt/giriş ekranı, yuva kaydı ekleme/listeleme ekranı, otel kapanış kanıtı + uyum oranı paneli (yukarıdaki "Güncellenmiş Öncelik" bölümündeki özellik, artık mobilde).
2. Ağ isteklerinde (Retrofit veya HttpURLConnection, hangisi daha basitse) backend adresi olarak `http://10.0.2.2:8080` kullanılmalı — Android emülatöründen bilgisayarın kendi `localhost`'una bu şekilde ulaşılıyor, normal `localhost` çalışmaz.
3. Fotoğraf yükleme (kapanış kanıtı) için Android'in kamera/galeri izinlerini ve dosya seçme akışını ekle.
4. Backend'i (`CarettaProje` klasöründe, `docker compose up` ile) her test öncesi ayrıca ayakta tutmak gerekiyor — iki proje ayrı klasörlerde, birbirine API üzerinden bağlanıyor.

**Kullanıcı için not:** Kullanıcı (Ceren) Java biliyor ama hem Spring Boot'u hem Android geliştirmeyi yeni öğreniyor. Her önemli kod parçasını yazdıktan sonra 2-3 cümleyle basit bir dille ne işe yaradığını açıkla. Büyük kararlar öncesinde (yeni bir kütüphane eklemek, mimariyi değiştirmek gibi) mutlaka sor, kendi başına ilerleme. Zorunlu kurallar (Conventional Commits + scope, .env gizliliği) mobil tarafta da geçerli.

## Detaylı Uygulama Planı (Bu Haftanın Teknik Kapsamı)

Staj sorumlusu: "Bu hafta projenin teknik tüm kısmı bitmiş olmalı ki go-to-market vb. şeylerle geliştirebilelim." Aşağıdaki adımlar **sırayla** yapılacak. Her adım bitince Conventional Commits formatında commit atılacak ve GitHub'a push edilecek.

### Adım 1 — Backend Güvenlik ve Yetkilendirme Sertleştirmesi
- [ ] `/api/oteller` endpoint'i şu an kimlik doğrulaması olmadan **yazma** işlemine de açık. `GET` herkese açık kalsın (kayıt formu için gerekli), ama `POST` (yeni otel ekleme) sadece kimliği doğrulanmış kullanıcıya açılsın.
- [ ] Rol bazlı yetkilendirme: `OTEL_CALISANI` rolü olmayan bir kullanıcı `POST /api/kapanis-kaniti` çağıramasın (Spring Security `@PreAuthorize` veya SecurityConfig kuralı).
- [ ] Yatay yetki kontrolü: bir otel çalışanı yalnızca **kendi oteline** ait kapanış kanıtı yükleyebilsin ve yalnızca kendi otelinin uyum oranını görebilsin (`GET /api/otel/{id}/uyum-orani` çağrısında token'daki `otelId` ile URL'deki `id` karşılaştırılsın).
- [ ] Dosya yükleme güvenliği: yalnızca `image/jpeg` ve `image/png` içerik tipi kabul edilsin, maksimum 10MB, yüklenen dosya adı sunucuda **yeniden üretilsin** (UUID) — kullanıcının gönderdiği dosya adı doğrudan diske yazılmasın (path traversal riski).
- [ ] JWT: secret `.env`'den okunsun (koda gömülü olmasın), token süresi makul olsun (örn. 7 gün), süresi dolmuş token reddedilsin.
- [ ] Şifreler BCrypt ile hash'lensin (Spring Security'nin `BCryptPasswordEncoder`'ı) — düz metin şifre asla saklanmasın.
- [ ] CORS: mobil uygulama ve yerel test için gerekli origin'ler açık olsun, `*` (herkese açık) bırakılmasın.
- [ ] Hata mesajlarında stack trace / veritabanı detayı sızmasın (`GlobalExceptionHandler` zaten var, kontrol edilsin).

### Adım 2 — Backend'i GitHub'a Bağla
- [ ] GitHub'da boş bir repo oluştur (README/gitignore/license **seçmeden**).
- [ ] `git remote add origin <repo-url>` → `git branch -M master` → `git push -u origin master`.
- [ ] Push öncesi `git status` ile `.env` dosyasının **kesinlikle** gönderilmediğini doğrula.

### Adım 3 — Mobil Uygulama: Ağ Katmanı
- [ ] Android projesine ağ kütüphanesi ekle (Retrofit + Gson veya OkHttp — en basiti tercih edilsin).
- [ ] `AndroidManifest.xml`'e `INTERNET` izni ekle.
- [ ] Emülatör için backend adresi: `http://10.0.2.2:8080` (bilgisayarın `localhost`'una emülatörden bu adresle ulaşılır).
- [ ] Geliştirme sırasında HTTP (HTTPS değil) kullanıldığı için `network_security_config.xml` ile sadece bu adrese cleartext izni verilsin — tüm trafiğe değil.
- [ ] JWT token'ı cihazda güvenli sakla (`EncryptedSharedPreferences`), düz `SharedPreferences` kullanma.

### Adım 4 — Mobil Uygulama: Ekranlar
- [ ] Giriş ekranı (`LoginActivity`) — email + şifre, başarılı girişte token saklanır.
- [ ] Kayıt ekranı (`RegisterActivity`) — email, şifre, rol seçimi, otel seçimi/ekleme.
- [ ] Ana ekran (`MainActivity`) — kullanıcı rolüne göre yönlendirme.
- [ ] Yuva kaydı ekranı — kayıt ekleme formu + mevcut kayıtların listesi.
- [ ] Otel kapanış kanıtı paneli — fotoğraf çekme/seçme, yükleme, güncel uyum oranı göstergesi.

### Adım 5 — Test ve Doğrulama
- [ ] Her ekran emülatörde elle test edilsin (kayıt → giriş → yuva kaydı → kapanış kanıtı → uyum oranı).
- [ ] Backend'e en az birkaç temel otomatik test yazılsın (auth ve uyum oranı hesaplaması).
- [ ] Yetkilendirme testi: başka bir otelin uyum oranını çekmeye çalışınca `403` dönmeli.

### Adım 6 — Mobil Uygulamayı GitHub'a Bağla
- [ ] `CarettaApp` klasöründe `git init`, uygun `.gitignore` (Android Studio'nun ürettiği yeterli), ilk commit ve ayrı bir repo olarak push.

## Agent'lar (Claude Code Subagent Tanımları)

Bu projede tekrar eden işleri ayrı ayrı uzmanlaşmış agent'lara bölüyoruz. Tanım dosyaları `.claude/agents/` klasöründe:

| Agent | Dosya | Ne İşe Yarar |
|---|---|---|
| `backend-gelistirici` | `.claude/agents/backend-gelistirici.md` | Spring Boot tarafında entity/endpoint/servis yazar, katmanlı mimariye ve Repository Pattern'e uyar. |
| `mobil-gelistirici` | `.claude/agents/mobil-gelistirici.md` | Android (Java, XML layout) ekranlarını ve ağ katmanını yazar, `10.0.2.2` kuralını bilir. |
| `guvenlik-denetci` | `.claude/agents/guvenlik-denetci.md` | Kod yazmaz; yazılan kodu güvenlik/yetkilendirme açısından denetler, `.env` sızıntısı ve açık endpoint arar. |
| `test-dogrulayici` | `.claude/agents/test-dogrulayici.md` | Yazılan özelliğin gerçekten çalıştığını uçtan uca doğrular, test yazar, kırık akışları raporlar. |

Claude Code bu agent'ları uygun işlerde kendisi çağırmalı; özellikle her özellik bitiminde `guvenlik-denetci` ve `test-dogrulayici` çalıştırılmalı.

## Bilinen Riskler / İleride Yapılacaklar

Güvenlik denetiminde tespit edilen, MVP için engelleyici **olmayan** ama ilgili faza geçmeden önce mutlaka ele alınması gereken maddeler. Bilerek ertelendi — unutulmasın diye buraya yazıldı.

1. **Fotoğraf EXIF verisi temizlenmeli — "Misafire Açık Şeffaflık Sayfası" fazına geçmeden ÖNCE.**
   Kapanış kanıtı fotoğrafları şu an ham haliyle yükleniyor; EXIF meta verisi (GPS koordinatı, cihaz modeli/markası) temizlenmiyor. Bugün bir sorun değil çünkü fotoğraflar yalnızca backend'de duruyor ve otelin iç denetiminde kullanılıyor. Ancak yukarıdaki Ticari Yol Haritası'nın 4. maddesi (otelin kendi sitesine koyacağı halka açık şeffaflık sayfası) hayata geçerse bu fotoğraflar herkese açılır ve EXIF'teki konum/cihaz bilgisi istenmeden dışarı sızar. O faza başlamadan önce yükleme akışında (istemcide veya backend'de) EXIF temizliği eklenmeli.

2. **Magic-byte (dosya imzası) doğrulaması gerekli — kanıt fotoğrafını dışarı sunan bir endpoint eklenirse.**
   Dosya tipi doğrulaması şu an `Content-Type` başlığına dayanıyor ve bu başlık istemci tarafından uydurulabilir. Şu an aktif bir risk oluşturmuyor çünkü yüklenen dosyaları geri sunan hiçbir endpoint yok (`uploads/` klasörü dışarı açılmamış durumda). Fotoğrafları görüntületen bir endpoint (örn. `GET /api/kapanis-kaniti/{id}/fotograf`) veya statik dosya sunumu eklenirse, dosyanın gerçek içeriği (magic byte / dosya imzası) doğrulanmalı — aksi halde `image/png` etiketiyle yüklenmiş zararlı bir dosya tarayıcıda çalıştırılabilir hale gelir.

3. **Harita tile istekleri, bakılan bölgeyi üçüncü tarafa gösterir — hassasiyet artarsa kendi tile sunucusu gerekir.**
   Mobil uygulamada harita için OSMDroid (OpenStreetMap) kullanılıyor; API anahtarı gerektirmemesi nedeniyle bilinçli olarak seçildi. Bunun bedeli şu: harita her açıldığında hangi bölgeye zoom yapıldığı bilgisi OpenStreetMap'in tile sunucularına gider. Yuva koordinatlarının kendisi gönderilmiyor, yalnızca görüntülenen kaba alan. Proje planında kaçak avlanma riski açıkça bir tehdit olarak geçtiği için bu farkındalıkla taşınmalı; yuva konumlarının gizliliği kritik hale gelirse kendi tile sunucunuzu barındırmak (veya haritayı yalnızca yetkili rollere açmak) değerlendirilmeli.

4. **Rate limit (istek sınırlaması) yok — ödeme yapan birden fazla müşteri onboard edilmeden önce eklenmeli.**
   Uygulamada hiçbir endpoint'te kullanıcı/IP başına istek sınırı bulunmuyor. Beş nokta öne çıkıyor: (a) `POST /api/auth/register` herkese açık — spam hesap açma ve otel davet kodu deneme yolu; kod uzayı (32 karakterli alfabe, 8 hane ≈ 1,1 trilyon kombinasyon) kaba kuvveti pratikte imkânsız kıldığı için bugün kritik değil, ama tek savunma bu. (b) `GET /api/otel/{id}/uyum-raporu` uygulamanın en pahalı endpoint'i — 365 güne kadar tablo üretip PDF oluşturuyor. Yetkilendirme atlatılamıyor ve tek isteğin maliyeti düşük (~40-50KB, 1 saniyenin altı), ancak kimliği doğrulanmış (veya çalınmış token'a sahip) bir kullanıcı art arda büyük rapor isteyerek sunucu iş parçacıklarını meşgul edebilir. (c) `GET /api/istatistikler` (fon başvurusu/ortaklık istatistik endpoint'i, Ağustos 2026'da eklendi) bilerek `permitAll` — yani (a) ve (b)'nin aksine bu endpoint'i tetiklemek için JWT bile gerekmiyor, saldırı yüzeyini kimliksiz bir noktaya taşıyor; her istekte aktif otel sayısı kadar ek sorgu çalıştırıyor. (d) `GET /api/katki-sertifikasi` (Ağustos 2026'da eklendi) kimliği doğrulanmış her kullanıcının tek sayfalık bir PDF ürettiği endpoint — `uyum-raporu`'ndan daha ucuz (takvim/sayfalama yok) ama yine de her istekte CPU/IO harcıyor, aynı token-taşıyan-kullanıcı riski (b) ile aynı. (e) `GET /api/kapsam-alani` (Ağustos 2026'da eklendi, "Etkimiz/Kapsam Alanımız" için) da `permitAll` — her istekte **tüm** `yuva_kayitlari` tablosunu belleğe çekip 21 sabit referans noktasıyla Haversine karşılaştırması yapıyor (O(N×21)); kayıt sayısı arttıkça (c)'den daha pahalı hale gelebilir, kimlik doğrulama gerektirmediği için tek savunma IP bazlı sınırlama olur. Kullanıcı/IP başına basit bir sınır (örn. Bucket4j) tüm bu PDF/agregat endpoint'lerini kapsayacak şekilde eklenmelidir; `/api/istatistikler` ve `/api/kapsam-alani` için ayrıca sonucu birkaç dakika cache'lemek de (zaten günlük hassasiyette bir veri) makul bir ek önlem olur.

5. **Demo amaçlı "manuel premium işaretleme" aracı GERÇEK bir admin-rol sistemi DEĞİL — üretime geçmeden önce değiştirilmeli.**
   `POST /api/admin/otel/{id}/premium-durum` (Ağustos 2026'da eklendi, koltuk bazlı üyelik sisteminin parçası) sunumda ödeme akışından geçmeden premium özellik gösterebilmek için var. Projede henüz bir "admin kullanıcı/rol" kavramı olmadığı için bu endpoint, kimlik doğrulama (JWT) yerine paylaşılan bir gizli anahtarla (`X-Admin-Key` header, `.env`'deki `ADMIN_API_KEY` ile sabit-zamanlı karşılaştırma) korunuyor. Bu bilinçli bir kısayol — bugün için risk oluşturmuyor çünkü anahtar sadece geliştiricide/`.env`'de duruyor ve `.env` asla commit edilmiyor. Ancak gerçek bir admin ekibi (birden fazla kişi) olursa veya bu anahtar bir şekilde sızarsa, herkes herhangi bir oteli premium yapabilir — o noktaya gelmeden önce gerçek bir `ADMIN` rolü + kullanıcı hesabı sistemine geçilmeli.

## Terim Sözlüğü

Staj sorumlusunun paylaştığı mimari terimlerin (Monolith, Microservices, CQRS, JWT, RAG, Redis, Kafka vb.) birer-ikişer cümlelik açıklamaları `docs/terim_sozlugu.md` içinde — kullanmak zorunlu değil ama sunumda sorulursa bilinmeli.