# Canlı Seçim Anketi — Tasarım Teslim Paketi (Android)

Bu paket, tasarımın uygulamaya birebir aktarılması için gereken her şeyi içerir.
Platform: **Yalnızca Android.** Arayüz dili Türkçe, yalnızca dikey kullanım, koyu tema.

## Paket içeriği

| Klasör / dosya | Ne işe yarar |
|---|---|
| `TASARIM.md` | Bu belge: ekranlar, davranışlar, kurallar, Android notları |
| `tokens.json` | Renkler, yazı tipleri, köşe yarıçapları, ölçüler, animasyon eğrileri |
| `ekranlar/*.html` | Her ekranın tasarım kaynağı. Düzen, stil ve etkileşim mantığı burada. **Referans olarak okunur, doğrudan çalıştırılmaz** (tasarım aracına ait bir çalışma ortamı gerektirir). |
| `varliklar/logolar/` | Kırpılmış ve optik olarak eşitlenmiş parti logoları (128×128, şeffaf PNG). Dosya adındaki sayı Meclis sandalye sırasıdır. |
| `varliklar/liderler/` | Yüzü ortalanmış lider fotoğrafları (112×112 JPEG, ekranda daire içinde gösterilir). |

Ekran HTML dosyalarında her ekranın en altındaki `<script>` bölümü veri ve davranış mantığını
(sıralama, basılı tutma, kaydırma, durum değişkenleri) içerir. Görsel ölçüler inline `style`
değerlerindedir; değerler `tokens.json` ile aynıdır.

## Tasarım dili

- **Zemin:** Mürekkep laciverti. **Metin ve parti mumu:** oy pusulası fildişisi. **Vurgu ve lider mumu:** seçim mavisi.
- **Turuncu** yalnızca "asla" değerleri içindir.
- **Parti renkleri arayüzde hiçbir yerde kullanılmaz.** Partiler yalnızca kendi logolarıyla görünür; arayüz tarafsız kalır.
- Yazı tipi: **Geist** (metin), **Geist Mono** (etiketler). En küçük yazı 11 sp.
- Cam yüzeyler: yarı saydam lacivert + arka plan bulanıklığı. Android 12+ için `RenderEffect` blur; daha eski sürümlerde düz `cam_kart` rengi.

## Ana bileşen: Mum şeridi

8 partinin yan yana sütunlar halinde gösterildiği şerit. Uygulamanın kimliği bu bileşen.

- Sütun içeriği, yukarıdan aşağıya:
  1. Canlı parti oyu yüzdesi (11 sp)
  2. **Yapışık iki mum:** solda parti oyu (fildişi), sağda lider oyu (mavi). Aralarında boşluk yok; dış köşeler yuvarlak, birleşen kenar düz. Her mumun üstünde ince bir fitil var.
  3. Parti logosu (34×34, açık renkli kutu içinde)
- **Sıralama:** Parti oyuna göre en çoktan en aza, soldan sağa. Eşitlikte Meclis sandalye sırası.
- **Sembolik veri:** Gerçek oy yokken tüm partiler eşit yükseklikte, yüzde yerine `%—` gösterilir ve "Sembolik · eşit" etiketi görünür. Hiçbir parti önde ya da geride görünmez.
- **Canlı parıltı:** Yeni veri geldiğinde mum uçları soldan sağa sırayla kısa bir parıltı yapar.

## Ekranlar

### 1 · Anketler (`1-anketler.html`)
- Başlık, "Her oy eşit sayılır. Her ses anonim kalır." alt yazısı, hesap düğmesi (Google ile giriş).
- Üstte "Kilit ekranında" kartı: seçili anketin adı ve mum şeridinin küçük bir hali. Dokununca 4 numaralı ekran açılır.
- "Katıldıklarım" ve "Katılabileceklerim" listeleri. Her ankette:
  - Bildirim zili (aç/kapat)
  - "Kilit ekranına al" düğmesi. Aynı anda yalnızca bir anket seçili olabilir.
  - "Katıl" ya da "Sonuçlar" düğmesi
- Alt gezinme: Anketler / Kilit ekranı / Canlı.

### 2 · Oy verme (`2-oy-verme.html`)
- Üç bölüm, anket türüne göre görünür:
  - **Parti:** zorunlu
  - **Lider:** isteğe bağlı
  - **Asla:** isteğe bağlı
- Her bölümde tek seçim. Seçime tekrar dokunmak ya da "Geri al" düğmesi seçimi kaldırır.
- **Lider seçimi partiden bağımsızdır.** Parti seçildiğinde o partinin lideri listenin başına gelir ve "Partinin lideri" etiketi alır; başka bir lider seçmek serbesttir.
- Desteklenen parti "Asla" bölümünde seçilemez (soluk görünür).
- Eş genel başkanlı partilerde (DEM Parti, DBP) iki lider birlikte gösterilir ve birlikte seçilir.
- Altta sabit özet çubuğu ve gönder düğmesi. Parti seçilmeden gönderilemez. Gönderim Google hesabıyla onaylanır.

### 3 · Oy gönderildi (`3-oy-gonderildi.html`)
- Yayılan halkalar ve çizilerek beliren onay işareti animasyonu.
- Seçimlerin özeti. "Bu özet yalnızca bu cihazda saklanır" notu.
- "Kilit ekranında takip et" anahtarı.
- Kilit ekranı görünümüne geçiş düğmesi.

### 4 · Kilit ekranında inceleme (`4-kilit-ekraninda-inceleme.html`)
**En önemli ekran.** 5 numaralı kilit ekranındaki bildirime dokununca açılır.
- **Kilit açılmadan, kilit ekranının üstünde gösterilir.**
- **5 numaralı ekranla piksel piksel aynıdır:** aynı duvar kâğıdı, aynı saat, şerit aynı konumda. Geçişte hiçbir öğe yerinden oynamaz. Kullanıcı "başka sayfaya geçtim" hissi yaşamamalı.
- Değişen tek şeyler:
  - Şeridin çevresinde kısa bir mavi parıltı
  - Sağ alttaki ipucunun "Basılı tut, kaydır" olması
- **Basılı tutma (240 ms):**
  - Tutulan sütun yay animasyonuyla aşağı doğru büyür.
  - Tepesinde lider fotoğrafı belirir; eş genel başkanlarda iki fotoğraf üst üste.
  - Mumlar logodan yukarı doğru uzar. Diğer sütunlar solar.
  - Sütunun yanında küçük bilgi kartı açılır: parti adı, sırası, lider adı, parti desteği, lider desteği. Kart ilk 4 partide sütunun sağında, son 4 partide solunda açılır.
- **Kaydırma:** Parmak kaldırılmadan yana kaydırılınca açık sütun parmağın altındaki partiye geçer. Her geçişte hafif titreşim.
- **Bırakınca:** Şerit kompakt haline döner.
- **Erişilebilirlik:** Tek dokunuş da sütunu açar, tekrar dokunuş kapatır. Ekran okuyucu her sütunda sırayı, parti adını, lider adını ve dört değeri okur.
- **Çıkış:** Geri hareketi ya da güç düğmesi ile kilit ekranına dönülür. Kilit hiç açılmaz.

### 5 · Android kilit ekranı (`5-android-kilit-ekrani-bildirim.html`)
- Saatin hemen altında kalıcı bir "Canlı Anket" bildirimi. İçinde kompakt mum şeridi ve "Parti / Lider / Sembolik · eşit" açıklaması bulunur.
- Bildirime dokununca 4 numaralı ekran açılır.

### 6 · Durumlar (`6-durumlar.html`)
| Durum | Ne görünür |
|---|---|
| Sembolik veri | Eşit taralı çubuklar ve "SEMBOLİK VERİ" etiketi |
| Henüz oy yok | 0 katılımcı ve "İlk oyu ver" düğmesi |
| Yayın durduruldu | Seçim öncesi yayın yasağı. Sonuçlar bulanık ve kilitli; yeniden yayın tarihi gösterilir. Oy vermeye devam edilebilir. |
| Bağlantı yok | Son bilinen veri ve saati, "Yeniden dene" düğmesi. Oy gönderilmeye hazır bekletilir. |
| Giriş gerekli | "Google ile giriş yap" ve "Göz atmaya devam et" seçenekleri. Göz atmak için giriş gerekmez. |

Her durumun kilit ekranı bildirimindeki karşılığı da tasarlanmalı (6 numaralı ekranın alt kısımları).

## Kesin kurallar

1. **Mahremiyet:** Kilit ekranı herkese görünür. Kullanıcının kendi tercihi kilit ekranında ve bildirimde **asla** gösterilmez.
2. **Anonimlik:** Kimin kime oy verdiği yalnızca kullanıcının cihazında saklanır.
3. **Tek oy:** Her Google hesabı bir ankette bir kez oy verir.
4. **Yayın yasağı:** Yasak döneminde sonuçlar hiçbir yerde gösterilmez (uygulama, bildirim, kilit ekranı).
5. **Erişilebilirlik:**
   - Dokunma alanları en az 48 dp.
   - Yazılar sistem yazı boyutuyla büyür.
   - "Animasyonları kaldır" ayarına uyulur.
6. **Tarafsızlık:**
   - Logolar ve fotoğraflar aynı boyda ve aynı çerçevede gösterilir.
   - Logolar düzenlenmez ya da yeniden renklendirilmez.
   - Lider fotoğrafları aynı tarzda olmalı (düz arka plan, yakın çekim, nötr ifade).

## Android uygulama notları

- **Önerilen teknoloji:** Kotlin + Jetpack Compose.
- **Kilit ekranı bildirimi (5):**
  - Kalıcı bildirim, özel görünüm (`NotificationCompat.DecoratedCustomViewStyle` + `RemoteViews`).
  - `RemoteViews` mumları doğrudan çizemez. Mum şeridini uygulama içinde bir `Bitmap`'e çizip `setImageViewBitmap` ile göster ve veri değiştikçe güncelle.
  - Bildirim içeriği kişisel tercih içermediği için `VISIBILITY_PUBLIC` kullanılabilir. Kullanıcı "hassas içeriği gizle" ayarını açtıysa şerit görünmez; bu sistem davranışı, değiştirilemez.
  - Android 14 ve sonrasında kullanıcılar kalıcı bildirimleri kaydırarak kapatabiliyor. Seçim gecesi gibi dönemlerde ön plan hizmeti (foreground service) değerlendirilmeli.
- **İnceleme ekranı (4):**
  - Ayrı bir `Activity`.
  - Kilit açılmadan görünmesi için `android:showWhenLocked="true"`.
  - `requestDismissKeyguard` **çağrılmaz**, kilit açık kalmaz.
  - Bildirimin `PendingIntent`'i bu Activity'yi açar. Açılış animasyonu kapalı olmalı (`overridePendingTransition(0, 0)` ya da eşdeğeri), böylece ekran değişmiyormuş gibi görünür.
  - Duvar kâğıdı ve saat, kilit ekranındakiyle aynı konum ve ölçüde çizilmeli.
- **Basılı tut ve kaydır:**
  - Compose'da `pointerInput` + `detectDragGesturesAfterLongPress` ile parmağın X konumundan sütun indeksi hesaplanır.
  - Titreşim: `HapticFeedbackType.LongPress`, sütun değişiminde `TextHandleMove`.
- **Veri modeli (öneri):**
  - `Parti { id, kisaAd, uzunAd, logo, liderler[1..2], sandalyeSirasi }`
  - `Anket { id, ad, tur: DESTEK_ASLA | YALNIZ_DESTEK | YALNIZ_ASLA }`
  - `Sonuc { partiId, partiDestek, liderDestek, partiyeAsla, lidereAsla }`
  - `AnketDurumu { katilimciSayisi, sonGuncelleme, sembolikMi, yayinYasagiMi, yayinYasagiBitis }`
- **Giriş:** Google ile giriş için Google'ın resmî giriş düğmesi ve kuralları kullanılmalı.

## Eksikler

- **Logolar:** YENİ Parti, TİP, DSP, Demokrat Parti, Anahtar Parti
- **Lider fotoğrafları:** Yeni Yol, HÜDA PAR, Yeniden Refah, TİP, DBP (2), EMEP, Saadet, DSP, Demokrat Parti, Zafer, Anahtar Parti
- **Fotoğraf tarzı:** Mevcut fotoğrafların tarzı birbirinden farklı. Tarafsızlık için hepsi aynı tarz resmî portrelerle değiştirilmeli (TBMM fotoğrafları önerilir).
- **Kullanım izinleri:** Logoların ve fotoğrafların yayında kullanımı için izin ve lisans kontrolü yapılmalı.
