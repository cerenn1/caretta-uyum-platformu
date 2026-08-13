# Kurulum — Adım Adım (Kendi Bilgisayarında Yapman Gerekenler)

Bunları ben senin bilgisayarında yapamıyorum, kendi ortamında sırayla uygulaman gerekiyor.

## 1. VSCode'a Claude Eklentisi
Zaten yüklüyse atla. Yoksa: VSCode → Extensions (Ctrl+Shift+X) → "Claude Code" ara → Install → sağ üstteki panelden hesabınla giriş yap.

## 2. Docker Kur
[docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop) üzerinden işletim sistemine uygun Docker Desktop'ı indir, kur, aç. Kurulduğunu terminalde şu komutla doğrula:
```
docker --version
```

## 3. Proje Klasörünü Oluştur ve Dosyaları Yerleştir
Bu oturumda oluşturduğum şu dosyaları proje klasörünün köküne kopyala:
- `CLAUDE.md`
- `.gitignore`
- `.env.example` (buradan kopyalayıp `.env` adında yeni bir dosya oluştur, gerçek değerleri oraya gir)
- `docker-compose.yml`

## 4. Supabase Projesi Aç
[supabase.com](https://supabase.com) üzerinden ücretsiz bir hesap/proje oluştur. Project Settings → API bölümünden `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` değerlerini al, `.env` dosyana yapıştır. Database → Connection String'den `DATABASE_URL`'i al.

## 5. GitHub Reposu Oluştur ve Bağla
```bash
# Proje klasöründe:
git init
git add .
git commit -m "chore(init): proje iskeleti oluşturuldu"
```
GitHub'da yeni, **boş** bir repo oluştur (README eklemeden), sonra:
```bash
git remote add origin https://github.com/kullanici-adin/repo-adi.git
git branch -M main
git push -u origin main
```
**Push etmeden önce mutlaka `.env` dosyasının `git status` çıktısında görünmediğini kontrol et** — görünüyorsa `.gitignore` doğru çalışmıyor demektir, push etme.

## 6. Docker ile Ayağa Kaldır
```bash
docker compose up
```
Bu; backend, PostgreSQL (PostGIS ile) ve Redis'i birlikte başlatır.

## 7. Commit Kuralı — Her Zaman
```
tip(scope): açıklama
```
Örnek: `feat(auth): kayıt formu endpoint'i eklendi`

## Cuma Teslimi İçin Hatırlatma
Minimum kapsam: landing page + kayıt formu + başarılı kayıt akışı + ana özellik (yuva/gözlem kaydı ekleme). Bittiğinde [loom.com](https://www.loom.com) ile sesli anlatımlı bir demo videosu çek, linkini paylaş.
