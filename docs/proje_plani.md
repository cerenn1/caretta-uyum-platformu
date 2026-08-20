# Kaplumbağa Yuvalama Bölgeleri için Kıyı Turizmi Sürdürülebilirlik Uyum Platformu
## Nihai Proje Dosyası

---

## 1. Proje Özeti

Antalya ve çevresindeki (Muğla, Mersin, Adana, Hatay dahil) caretta caretta yuvalama sahillerinde faaliyet gösteren otellerin ve halk plajlarının, yasal koruma yükümlülüklerine uyup uymadığını izleyen, ihlal riskini önceden haber veren, sahadaki gözlemleri kayıt altına alan ve bunları hem denetimlerde hem uluslararası sürdürülebilirlik sertifikalarında (Travelife, Green Key) kullanılabilecek resmi bir rapora dönüştüren bir B2B/B2G SaaS platformu.

Tek cümleyle: **"Yuvayı kim koruyor, nasıl koruyor, bunu kim kanıtlıyor?" sorusuna dijital ve güvenilir bir cevap.**

---

## 2. Problem ve Neden Şimdi

- Türkiye'de deniz kaplumbağası yuvalama sahilleri 2872 sayılı Çevre Kanunu kapsamında **Birinci Derece Koruma Bölgesi, İkinci Derece Koruma Bölgesi, Tampon Bölge ve Etki Alanı** olarak sınıflandırılmıştır.
- Yuvalama döneminde (Mayıs–Eylül), Muğla, Antalya, Mersin, Adana ve Hatay'daki **21 kumsalda gece 20:00–08:00 arası giriş yasağı** uygulanır.
- 2026 için idari para cezası bireyde **699.245 TL**, kurumsal işletmelerde (otel gibi) bunun **3 katı — 2.097.000 TL'ye kadar.**
- Bu teorik bir risk değil: **Mart 2024'te iki otel**, yuvalama alanındaki ~250 m²'lik sahili düzleştirdiği için toplam **482.790 TL** idari para cezasına çarptırıldı.
- Antalya'nın Lara–Kundu bölgesinde tek başına **400 yuva** tespit edilmiş durumda; oteller bu yuvaları koruma sorumluluğu taşıyor.
- En büyük ihlal kaynağı gece ışıklandırması: yavru kaplumbağalar denize değil, otel/plaj ışıklarına yönelip yön kaybediyor.
- Buna rağmen bu süreç çoğu yerde **kayıt altına alınmadan, dağınık ve sözlü** şekilde yürütülüyor — bir denetimde ya da sertifika başvurusunda "biz dikkat ediyoruz" demek yeterli değil, tarih damgalı somut kanıt gerekiyor.
- Aynı zamanda uluslararası eko-sertifikalar (Travelife, Green Key) başvurularında "biyoçeşitlilik koruma kanıtı" isteniyor; oteller bunu hazırlamakta zorlanıyor.

---

## 3. Çözüm

Sahadaki gözlemi (kimin zaten yaptığı bir işi) dijital, izlenebilir ve **başka departmanlara/otoritelere ulaştırılabilir** bir sisteme dönüştüren; toplanan veriyi hem önleyici uyarıya hem de resmi rapora çeviren bir platform.

Önemli bir netlik: sahildeki gözlemci (otel personeli veya gönüllü) zaten yuvaları görüyor — bunu biliyoruz. Platformun asıl değeri, bu bilgiyi **(a)** o bilgiye ihtiyacı olan ama sahili gezmeyen kişilere (etkinlik ekibi, tesis müdürü, taşeronlar) otomatik ulaştırmak ve **(b)** zamanla kaybolan sözlü bilgiyi, denetimde/sertifikada işe yarayan resmi bir kayda çevirmektir.

---

## 4. Kullanıcı Tipleri ve Akışları

### 4.1 Saha Gözlemcisi (otel personeli / EKAD gönüllüsü)
Sahilde yürürken tespit ettiği yuvanın konumunu, tarihini ve durumunu (aktif / çıkış yaptı / risk altında) sisteme girer. Bu, bugün zaten yaptığı işin dijital hali.

### 4.2 Otel Yöneticisi / Tesis Müdürü
- Kayıtlı yuva konumlarını haritada görür.
- Planladığı bir etkinlik, bakım çalışması veya yeni aydınlatma kurulumu bir yuva bölgesine yakınsa, sistem **önceden** ("bu alan bir yuvaya X metre mesafede, ihlal riski taşıyor") uyarı gönderir — bu, sahili gezmeyen departmanın haberi olmadığı bir riski proaktif olarak yakalar.
- Akşam 20:00–sabah 08:00 arası sahilin kullanıma kapalı olduğunu gösteren **fotoğraf kanıtı** yükler (bkz. Bölüm 5.6).
- Ayda bir, Claude tarafından üretilen otomatik uyum raporunu indirir; bunu hem iç kayıt hem sertifika başvurusu için kullanır.

### 4.3 Vatandaş (halk plajı ziyaretçisi)
- Uygulama üzerinden gördüğü bir yuvayı veya ihlali (gece ışığı, ateş, araç girişi) bildirir.
- Bildirimi onaylandığında puan kazanır (bkz. Bölüm 5.7).

### 4.4 Belediye / İl Çevre Müdürlüğü Yetkilisi
- Bölgesindeki tüm kumsalların (otel + halk plajı) uyum durumunu tek ekrandan izler.
- Vatandaş bildirimlerini onaylar/reddeder (moderasyon).
- Bakanlığa sunacağı raporu sistemden otomatik alır.

---

## 5. Özellik Detayları

### 5.1 Yuva ve Gözlem Takibi
Konum, tarih, durum bilgisiyle her yuva kaydedilir; zaman içindeki değişim (yeni yuva, çıkış yapan yuva, kaybolan yuva) izlenir.

### 5.2 Işıklandırma Uyum Kaydı
Otel, kıyı aydınlatma armatürlerinin türünü (sarı ışık / beyaz ışık, perdeli / perdesiz) kaydeder, düzenli saha fotoğrafı yükler.

### 5.3 Otomatik Risk Tarama ve Çapraz Kontrol
Sistem, yuva konumlarıyla planlanan etkinlik/bakım/aydınlatma kayıtlarını çakıştırıp riskli noktaları otomatik işaretler ve ilgili karar vericiye (sahildeki gözlemciye değil, riski bilmesi gereken kişiye) bildirim gönderir.

### 5.4 Uyum Raporu Üretimi (Claude'un Rolü)
Claude, dağınık ham verileri (yuva sayısı, risk kayıtları, alınan önlemler, fotoğraf kanıtları) alıp hem iç kullanım için özet hem sertifika başvurusunda doğrudan kullanılabilecek resmi dilde bir metin üretir. Claude görüntüden kaplumbağa/yuva tanımıyor — bu MVP kapsamı dışında bırakıldı; Claude'un işi veriyi okunabilir/kullanılabilir hale getirmek.

### 5.5 Vatandaş Bildirim Modülü ve Doğrulama Akışı
Halk plajlarında sabit bir gözlemci olmadığı için, herkesin bildirim yapabildiği bir form vardır. Bildirimler otomatik "doğru" sayılmaz: *bildirim gelir → moderatöre (belediye yetkilisi veya EKAD) düşer → onaylanır/reddedilir → onaylanan kayıt resmi veri setine girer.*

### 5.6 YENİ — Fotoğraflı Kapanış Kanıtı Sistemi (Staj Sorumlusunun Önerisi)
Otel, her gün akşam 20:00–sabah 08:00 arası sahilin kullanıma kapalı olduğunu gösteren zaman damgalı bir fotoğraf (boş sahil, kapalı bariyer, kapatılmış şezlong alanı vb.) yükler. Bu kayıtlar takvimde birikir ve ay sonunda "şu ay X günün Y'sinde kapanış kanıtı yüklendi" şeklinde otomatik bir **uyum oranı** hesaplanır. Bu oran, hem iç takip hem sertifika başvurusunda somut, tarih damgalı bir kanıt olarak sunulur — yani "biz uyuyoruz" demek yerine "işte %92 uyum oranımızın kanıtı" denebiliyor. Bu özelliğin en güçlü yanı, sertifikasyon ihtiyacını doğrudan karşılayan, kolay anlaşılır bir çıktı üretmesi.

### 5.7 YENİ — Puan / Ödül Sistemi (Staj Sorumlusunun Önerisi)
Halk plajı kullanıcıları (vatandaşlar) ve isteğe bağlı olarak otel personeli, doğrulanmış her bildirim/gözlem kaydı için puan kazanır (örn. yuva bildirimi 50 puan, ihlal bildirimi 30 puan, fotoğraf kanıtı 20 puan). Puanlar belirli eşiklere ulaştığında küçük ödüller kazandırır.

**Ödül kaynağı üzerine bir not (dürüst planlama):** Ödüllerin maliyetini platformun kendisinin karşılaması gerekmez — yerel işletmelerle (kafe, restoran, hediyelik eşya dükkanı) sponsorluk/ortaklık kurularak "indirim kuponu" şeklinde verilebilir. Bu hem ödül maliyetini sıfıra indirir hem de yerel işletmelere yeni müşteri getirir — iki taraflı kazan-kazan bir ortaklık modeli. Bu, projeyi turizm bölgesindeki diğer paydaşlara da bağlayan ek bir gelir/ortaklık kapısı olarak sunulabilir.

Bu sistemin asıl amacı katılımı artırmak: ne kadar çok kişi bildirim yaparsa, veri seti o kadar zenginleşir ve moderasyon ekibinin (Bölüm 5.5) işi kolaylaşır.

---

## 6. Hedef Kitle ve Müşteri Segmentleri

| Segment | Kim | Neden Öder |
|---|---|---|
| Otel (birincil) | Belek, Kundu, Lara, Kumluca, Patara gibi koruma bölgesindeki oteller | Ceza riskini (2 milyon TL'ye kadar) azaltmak + sertifika başvurusunu somut kanıtla desteklemek |
| Belediye / İl Çevre Müdürlüğü (ikincil, halk plajları için) | Bölgenin yasal sorumlusu kurumlar | Denetim ve Bakanlığa raporlama yükümlülüğü + bölge genelinde tam kapsam |
| Turizm Yatırımcıları Birliği (toplu satış) | Belek Turizm Yatırımcıları Birliği benzeri kurumlar | Zaten 32 yıldır EKAD'ın saha projesini bu şekilde finanse ediyorlar — kanıtlanmış bir finansman emsali |
| Yerel işletmeler (ortak, müşteri değil) | Kafe, restoran vb. | Puan/ödül sistemine sponsor olarak müşteri kazanımı |

---

## 7. Gelir Modeli

- **Otel başına yıllık abonelik:** 5.000–15.000 TL/yıl (otel büyüklüğüne göre kademeli).
- **Belediye/turizm birliği toplu lisansı:** bölge genelinde tam kapsam (otel + halk plajı) paketi.
- **Sertifikasyon destek ücreti:** Travelife/Green Key başvurusu için hazırlanmış özel rapor paketi.
- **Ödül ortaklığı:** yerel işletmelerden sponsorluk/reklam geliri (küçük ama ek bir gelir kalemi).

---

## 8. Mimari Yaklaşım (Kavramsal — Kod Değil, Planlama)

- **Veritabanı seçimi:** PostgreSQL + PostGIS. Proje temelde konumsal veri (yuva koordinatları, koruma bölgesi sınırları, etkinlik/aydınlatma konumları) üzerine kurulu; PostGIS "bu nokta koruma bölgesinin içinde mi", "planlanan etkinlik en yakın yuvaya kaç metre" gibi coğrafi sorguları doğrudan ve güvenilir şekilde destekliyor.
- **Mimari stil:** Modular Monolith + Clean Architecture. Modüller: `yuva-takip`, `isik-uyum`, `raporlama`, `bildirim`, `puan-sistemi`, `kullanici-yonetimi`. Tek kişilik geliştirme ölçeğinde net sınırlarla ayrılmış modüller, mikroservisin getirdiği karmaşıklık olmadan düzenliliği sağlıyor.
- **Kullanılan tasarım desenleri:**
  - **Strategy:** Farklı sertifikasyon standartlarına (Travelife, Green Key) göre farklı rapor formatı üretmek için.
  - **Observer:** Risk tespit edildiğinde ilgili karar vericiye (etkinlik/tesis müdürü) otomatik bildirim; puan eşiği aşıldığında ödül tetiklemek için.
  - **Chain of Responsibility:** Vatandaş bildiriminin doğrulama sürecinden (gönderim → moderasyon → onay) geçmesi için.
  - **Repository Pattern:** Coğrafi ve puan verilerine erişimi iş mantığından soyutlamak için.
  - **Pipeline:** Saha verisi girişinden risk analizine, oradan rapor üretimine kadar olan sürecin ayrı, test edilebilir adımlara bölünmesi için.
- **Zorunlu bileşenler:** Docker + docker-compose (backend, PostgreSQL/PostGIS, Redis birlikte ayağa kalkar); Git commit'leri Conventional Commits + scope formatında tutulur (`feat(puan-sistemi): ödül eşik mantığı eklendi` gibi); `.env` dosyaları git'e girmez.
- **Claude'un rolü:** Görüntüden kaplumbağa/yuva tanımıyor (ayrı bir bilgisayarlı görü işi, kapsam dışı). Asıl katkısı, dağınık ham veriyi (koordinat, tarih, fotoğraf listesi, ihlal kaydı) hem iç ekip hem denetleyici hem sertifika kurumu için okunabilir, tutarlı bir rapor metnine çevirmek.

---

## 9. Gerçek İlham Alınan Projeler ve Farklılaşma

| Referans | Ne Yapıyor | Bizim Farkımız |
|---|---|---|
| Wild Me / Wildbook, Xomnia | Fotoğraftan bireysel hayvan tanıma (bilgisayarlı görü), kâr amacı gütmeyen/grant finansmanlı | Tür tanıma değil, **turizm sektörünün ödeyeceği bir uyum/sertifikasyon ürünü** |
| BeCause, OxMaint, EarthCheck | Genel otel ESG/sürdürülebilirlik yazılımı (enerji, su, atık) | Biyoçeşitlilik onlarda tek bir genel metrik; biz **tek bir türe, tek bir yerel regülasyona** özel derinlik sunuyoruz |
| SEATURTLE.ORG (nestdb) | Araştırmacılar için yuva veritabanı | Ticari değil; biz bunu **gelir modeli olan bir B2B/B2G ürüne** çeviriyoruz |
| EKAD + Belek Turizm Yatırımcıları Birliği | 32 yıllık saha gözlemi + turizm sektörü finansmanı | Gelir modelimizin **gerçekte çalıştığının kanıtı** — bunu dijital, ölçeklenebilir bir SaaS'a çeviriyoruz |

**Araştırma notu:** Bu tam kombinasyonu (tür-spesifik yaban hayatı izleme + turizm işletmesi yasal uyum/sertifikasyon) birleştiren bir MIT veya Silikon Vadisi şirketine rastlamadım — bulabildiğim kadarıyla bu boş bir alan, ama küresel ölçekte her şeyi taramış olamam, bu iddia temkinli sunulmalı.

---

## 10. Yol Haritası

**Faz 1 (staj kapsamı — MVP):**
- Otel modülü: yuva takip, ışık uyum kaydı, çapraz risk kontrolü, fotoğraflı kapanış kanıtı, Claude ile rapor üretimi.
- Halk plajı modülü: vatandaş bildirimi + moderasyon + temel puan sistemi.
- Manuel/basit veri girişiyle çalışan, canlı bir bulut entegrasyonu gerektirmeyen bir prototip.

**Faz 2 (gelecek vizyonu, MVP dışında):**
- Kameradan otomatik yuva/iz tespiti (açık kaynak bir görüntü tanıma modeliyle).
- Ödül ortaklıkları için yerel işletme entegrasyon paneli.
- Gerçek otel/belediye pilot ortaklıkları.

**Faz 1.5 (Otel/Ticari Yol Haritası — kapanış kanıtı modülünden sonra):**
- Otomatik sertifika raporu (PDF) — Green Key/Travelife başvurusunda kullanılabilecek resmi rapor otomatik üretimi.
- Zincir otel paneli — birden fazla tesisi olan otel gruplarının tek panelden yönetimi.
- Bölgesel karşılaştırma (benchmarking) — otelin bölge ortalamasına göre uyum oranı kıyaslaması.
- Misafire açık şeffaflık sayfası — otelin kendi sitesine ekleyebileceği, güncel uyum oranını gösteren halka açık sayfa.
- Otomatik hatırlatma sistemi — kanıt yüklenmediğinde otel çalışanına otomatik uyarı.

---

## 11. Dürüstçe Belirtilmesi Gereken Riskler ve Sınırlamalar

- Görüntüden otomatik kaplumbağa/yuva tanıma MVP'de **yok**; ilk sürüm manuel saha veri girişine dayanıyor.
- Gerçek otel/belediye pilot ortaklığı kurmak zaman alır; sunumda bu bir **konsept ve teknik prototip** olarak sunulmalı, doğrulanmamış müşteri iddiası kullanılmamalı.
- Bireysel otel satışı belirsizliği yüksek; asıl güçlü kanıt turizm birliği/belediye toplu finansman modelinde (EKAD emsali).
- Vatandaş bildirim modülü doğrulama olmadan güvenilmez; moderasyon akışı opsiyonel değil, zorunlu bileşen.
- Puan/ödül sisteminin sürdürülebilirliği yerel işletme ortaklıklarının kurulmasına bağlı; bu bir varsayım, garanti değil.
- Regülasyon detayları (ceza tutarı, bölge sınıflandırması) haber kaynaklarından derlenmiştir; resmi ürünleştirmede Çevre, Şehircilik ve İklim Değişikliği Bakanlığı'nın güncel yönetmelik metniyle doğrulanmalıdır.