# Proje Brifi: Canlı Seçim Anketi (iPhone)

> Bu dosya, claude.ai'de yürütülen uzun bir tasarım sohbetinin özetidir. O sohbeti göremiyorsun. Bu belge ve `design-reference/` klasörü senin tek kaynağın. Burada yazan kararlar kullanıcıyla tek tek netleştirildi. Bir kararı değiştirmeden önce mutlaka kullanıcıya sor.

---

## 0. Senden beklenen çalışma şekli

1. **Önce oku, sonra sor.** Bu dosyanın tamamını ve `design-reference/v2-onaylanan/` içindeki dosyaları oku. Bölüm 13'teki açık soruları kullanıcıya sor. Kod yazmadan önce teknik planını (mimari, minimum iOS sürümü, klasör yapısı, aşamalar) özetle ve onay al.
2. **Aşamalı ilerle** (Bölüm 12). Her aşamanın sonunda SwiftUI Preview ya da simülatör ekran görüntüsüyle kullanıcıya göster, onay almadan sonrakine geçme.
3. **Tasarıma sadık kal.** Renk, ölçü, yazı boyutu ve etkileşim kararları aşağıda kesin olarak verildi. "Daha iyi olur" diye kendi başına değiştirme; önerin varsa sor.
4. **Kullanıcı Türkçe konuşuyor.** Arayüz metinleri Türkçe, yanıtların Türkçe olsun. Kod, değişken ve dosya adları İngilizce olabilir.
5. **Platform kısıtı varsa açıkça söyle.** iOS bir şeye izin vermiyorsa bunu gizleme, alternatifleri sun (Bölüm 3).

---

## 1. Ürün özeti

Türkiye'deki seçimler için **çevrim içi, canlı ve temsili bir anket uygulaması**. Kullanıcılar şunları yapabilir:

- Anketlere katılır, oy verir ve istediği anketi takip eder.
- Desteklediği partiyi ve lideri seçer. Bazı anketlerde "asla oy vermem" dediği partiyi de seçer.
- Sonuçları özellikle **kilit ekranında, saatin hemen altındaki bir şeritten** sezgisel olarak takip eder.

**Ürünün kalbi** kilit ekranı şerididir. Kullanıcı bir parti logosuna basılı tutar ve parmağını kaldırmadan diğer logolara kaydırır. Şerit yerinde büyür ve parmağın altındaki partinin parti ve lider oylarını mumlarla gösterir.

**Duygusal hedef:** "Bu mumlar vatandaşın oyları." Oy verme ise gerçek bir oy pusulasına mühür basma hissi vermeli.

**Görsel hedef:** iOS dilinde, çok sade, çok kaliteli, kompakt, ilk bakışta anlaşılır.

---

## 2. Kesin kurallar (pazarlık konusu değil)

- **Tüm veriler örnek veridir.** Partiler, liderler ve rakamlar uydurmadır. Gerçek parti, kişi ya da gerçek anket sonucu kullanma. Arayüzde "temsili anket / örnek veri" ibaresi görünür olmalı.
- **Mumlar tek ve sabit bir renktedir.** Parti renkleri mumlarda kullanılmaz, yalnızca logolarda bulunur. Gerekçe: oylar vatandaşa aittir.
- **Mum düzeni:** Logo merkezdedir ve sıfır çizgisi gibi davranır.
  - Logonun **üstünde**, birbirine bitişik iki mum yukarı uzanır: **solda parti oyu, sağda liderin oyu**.
  - Logonun **altında** iki eksi mum aşağı uzanır: **solda parti eksi, sağda lider eksi**.
  - Eksi mumlar yalnızca o logo basılı tutulurken görünür.
- **Dynamic Island benzeri bir açılma yok.** Şerit başka bir şeye dönüşmez, yerinde aşağı doğru büyür.
- **Mahremiyet:** Kilit ekranında kullanıcının kendi tercihi ("senin partin" gibi) gösterilmez. Kilit ekranı herkese görünür bir yerdir.
- **Oylar anonimdir.** Bir kullanıcı bir ankette yalnızca bir kez oy kullanır (çözüm önerini sun, Bölüm 10).
- **Hukuki not:** Türkiye'de seçimden önceki son günlerde anket yayınına yasak uygulanır. Uygulamanın bu dönemde nasıl davranacağı (yayını durdurma vb.) bir hukukçuyla netleştirilmeli. Bunu kullanıcıya hatırlat; kendi başına hukuki karar verme.

---

## 3. iOS gerçeği: tasarım bir konsept, uygulamada karar gerekiyor

Tasarım, kilit ekranında basılı tut + kaydır etkileşimi **gerçekten çalışıyormuş gibi** çizildi. iOS bugün buna doğrudan izin vermiyor:

- **Widget türleri:** Kilit ekranı widget'ları (WidgetKit `accessoryRectangular` / `accessoryCircular` / `accessoryInline`) küçüktür. Sistem bunları çoğunlukla tek renkli veya yarı saydam ("vibrant") çizer, yani parti renkleri kaybolabilir.
- **Etkileşim:** Widget'larda jest (uzun basma, sürükleme) yoktur. iOS 17+ ile yalnızca App Intents tabanlı `Button` / `Toggle` desteklenir. Kilit ekranında uzun basmak sistemin özelleştirme modunu açar.
- **Güncelleme:** Widget'ların günlük yenilenme bütçesi sınırlıdır. Gerçek zamana yakın güncelleme için **Live Activity** (ActivityKit, push ile güncelleme) gerekir. Live Activity kilit ekranının alt bölümünde görünür ve süresi sınırlıdır.

Önerilecek seçenekler (kullanıcıya sun, birlikte karar verin):

- **A) Hibrit (öneri):**
  - Kilit ekranında `accessoryRectangular` widget'ında mum çiftleri sade şekilde görünür.
  - Dokununca uygulama, kilit ekranını birebir taklit eden tam ekran bir "Şerit" görünümüyle açılır. Basılı tut + kaydır deneyimi burada yaşanır.
  - Seçim gecesi gibi dönemlerde Live Activity ile canlı özet gösterilir.
- **B)** Yalnızca uygulama içi şerit ve ana ekran widget'ı.
- **C)** Live Activity merkezli yaklaşım. Etkileşim, App Intents düğmeleriyle "önceki / sonraki parti" şeklinde olur.

Hangi seçenek seçilirse seçilsin, **uygulama içindeki "Şerit" görünümü tasarımın birebir aynısı olmalı.**

---

## 4. Tasarım sistemi

### Renkler

| Token | Değer | Kullanım |
|---|---|---|
| `ink` | `#3F3596` | Tek vurgu rengi: düğmeler, seçili sekmeler, mühür, mühür özetleri |
| `inkTint` | `#ECEAF7` | Vurgunun açık zemini (pasif kapsül, ikon zemini) |
| `voteCandle` | `#F3EBDD` | Tüm mumlar (kâğıt beyazı) |
| `lockWallpaper` | `#161C27` | Kilit ekranı zemini (tasarımda `#1A2130` tonunda büyük bir daire derinlik veriyor) |
| `stripGlass` | `rgba(255,255,255,0.10)` + `blur(30px) saturate(140%)` + iç kenar `0.5px rgba(255,255,255,0.22)` | Şerit zemini |
| `groupedBg` | `#F2F2F7` | Uygulama zemini (iOS systemGroupedBackground) |
| `card` | `#FFFFFF` | Gruplu liste kartları |
| `secondaryText` | `#6D6D72` | İkincil metin |
| `separator` | `#D8D8DD` (0.5 px) | Liste ayırıcıları |
| `paper` | `#FBF8F1` | Pusula kâğıdı |
| `paperLine` | `#D2C8B3` | Pusula kutu çizgileri |
| `paperInk` | `#2E2A22` / `#4A453B` / `#6A6458` | Pusula üzerindeki yazılar |
| `kraft` | `#D6BC8E`, `#CBAE7C`, `#E1CCA4` | Zarf |
| `switchOn` | `#34C759` | iOS anahtarı |

Parti renkleri yalnızca logo yer tutucularında kullanılır (Bölüm 9). Karanlık mod: kilit ekranı zaten koyu. Uygulama ekranları için karanlık mod paletini öner ve onay al.

### Yazı (SF Pro, iOS ölçeği)

- **Boyutlar:** Large Title 34/700 · Title 28/700 · Headline 17/600 · Body 17/400 · Subhead 15 · Footnote 13 · Caption 12 · Caption2 11.
- **En küçük boyut 11 pt.** 11'in altına inme.
- **Kilit ekranı saati:** 96 pt / 700, harf aralığı −3, beyaz %94. Tarih: 17 pt / 600, beyaz %82.
- **Rakamlar her yerde sabit genişlikte** (`.monospacedDigit()`).
- **Dynamic Type desteklenmeli.** Kilit ekranı şeridi sabit kalabilir, uygulama ekranları ölçeklenmeli.

### Ölçüler

- Kenar boşluğu 16 pt. Liste satırı en az 64 pt (iki satırlı) / 48 pt (ayar satırı).
- Kart köşesi 14. Şerit köşesi 26. Ana düğme yüksekliği 50, köşesi 14.
- Dokunma hedefi en az 44 × 44 pt.

### Hareket ve dokunsal geri bildirim

- **Mumlar:** Yükseklik değişimleri yay eğrisiyle (yaklaşık 260 ms, cubic-bezier(.2,.8,.2,1) karşılığı `spring(response: 0.3, dampingFraction: 0.85)`).
- **Logo büyümesi:** Yaklaşık 200 ms.
- **Mühür:** 1.6× ölçekten 0.94'e, oradan 1'e iner. −12° döndürülmüş durur, yaklaşık 300 ms sürer, opaklığı 0.9 olur.
- **Zarf:** Yukarıdan sandığa düşer, yaklaşık 900 ms.
- **Haptik:**
  - Basılı tutma başlarken hafif bir impact.
  - Parmak başka bir logoya her geçtiğinde `selectionChanged`.
  - Mühür basılırken orta şiddette impact.
  - Oy gönderilince success.

---

## 5. Ekran 1–3: Kilit ekranı şeridi (ürünün kalbi)

Referans: `design-reference/v2-onaylanan/LockRest`, `LockPress`, `LockDrag`.

### Yerleşim

- Saatin 12 pt altında, **350 pt genişliğinde**, cam görünümlü bir şerit.
- Şerit **en çok olumlu oy alan 8 partiyi** soldan sağa sıralar. Sütunlar eşit genişliktedir.

### Dinlenme hâli (dokunulmadan)

- **Başlık satırı:** Solda anket adı ("Genel Seçim Anketi", 12/600), sağda nokta + "Canlı".
- **Her sütun:**
  - Üstte 34 pt'lik bir alanda iki mum (genişlik 7 pt, aralarında 1 pt). Mumların üst köşeleri 3 pt, alt köşeleri 1 pt yuvarlak.
  - Altında 26 pt logo, ince yarı saydam kenarlıklı.
- Parti kodları ve sayaçlar gösterilmez; sadelik bilinçli bir karar.
- **Şeridin altında yardım metni** (12/500, beyaz %62): "Sol mum parti, sağ mum lider · Basılı tut".

### Basılı tutma hâli

- **Başlık satırı değişir:**
  - Solda: parti adı (15/600) ve altında "Lider adı · N. sıra" (12, beyaz %72).
  - Sağda küçük bir tablo (11/600, rakamlar sağa hizalı):

    |   | Parti | Lider |
    |---|---|---|
    | ▲ | %16,4 | %18,9 |
    | ▼ | %22,1 | %25,4 |

- **Mumlar:**
  - Artı mum alanı 34 → **92 pt** olur, mum genişliği 7 → **10 pt**.
  - Basılı logonun altında **70 pt'lik bir alanda eksi mumlar** aşağı uzanır. Diğer sütunlarda eksi mum görünmez.
- **Odak:** Aktif logo 1.18× büyür ve 2 pt beyaz halka alır. Diğer sütunlar %28 opaklığa iner.
- **Ölçek:** Tüm mumlar ortak bir ölçek kullanır (tasarımda tavan değer %27). Böylece partiler birbiriyle karşılaştırılabilir. Tavanı, anketteki en yüksek değere göre dinamik yap.
- **Sürükleme:** Parmak kaldırılmadan yatayda kaydırılır. Aktif sütun `floor(x / genişlik × 8)` ile bulunur (kenarlarda 0–7 aralığına sıkıştırılır). Değişince mumlar yeni değerlere akar ve haptik verilir.
- **Bırakma:** Parmak kalkınca her şey dinlenme hâline döner.
- **Basılı tutma eşiği:** 0,25–0,35 sn öner. Basılı tutmadan yapılan kaydırma şeridi tetiklememeli.

### Erişilebilirlik

- Her sütunun VoiceOver etiketi şu biçimde olmalı: "Parti adı: parti %x, lider %y; olumsuz parti %z, lider %w".
- Odaklanılan sütun basılı hâli göstermeli.
- VoiceOver'da sola/sağa kaydırma ile partiler arasında gezilebilmeli (adjustable).

---

## 6. Ekran 4: Anketler (iOS gruplu liste)

Referans: `Polls`.

- **Başlık:** Large Title "Anketler", altında footnote "Temsili anketler · örnek veri".
- **Bölüm "KİLİT EKRANINDA":** Beyaz kartın içinde, kilit ekranındaki şeridin küçük ve koyu bir önizlemesi (yalnızca dinlenme hâli). Altında açıklama: "Başka bir anketi göstermek için satırdaki kilit simgesine dokun."
- **Bölüm "KATILDIKLARIM":** Her satırda şunlar var:
  - 32 pt mürekkep zeminli ikon (anket türü glifi: ✓ / ✗ / ✓✗).
  - Başlık (16/600) ve altında "Tür · oy sayısı".
  - **Zil düğmesi:** takip / bildirim. Doluysa mürekkep rengi, boşsa gri.
  - **Kilit düğmesi:** bu anketi kilit ekranında göster. Aynı anda yalnızca bir anket seçili olabilir.
- **Bölüm "KATILABİLECEĞİN ANKETLER":** Satırların sağında "Katıl" kapsülü bulunur ve oy kabinine götürür.
- **Anket türleri:** `Destek + Asla`, `Yalnız destek`, `Yalnız asla`. "Asla oy vermem" seçimi yalnızca bu türü içeren anketlerde çıkar.
- **Sekme çubuğu (3 sekme):** Anketler · Oy Kabini · Kilit Ekranı (uygulama içi şerit görünümü). Aktif sekme mürekkep rengindedir.

---

## 7. Ekran 5: Oy kabini ve pusula

Referans: `Ballot`.

- **Gezinme çubuğu:** "‹ Anketler" geri düğmesi, ortada "Oy Kabini" (17/600), sağda "N mühür".
- **Segment kontrolü:** Parti · Lider · Asla. Tamamlanan bölümde mürekkep renginde ✓ görünür. Sekmeler anket türüne göre gösterilir.
- **Pusula kâğıdı:**
  - Zemin `paper`, köşe 4, yumuşak gölge.
  - Başlık: "TEMSİLİ ANKET PUSULASI" (11/700, harf aralığı 1.6), anket bölümü adı (15/600) ve ipucu (12).
  - Kutular **4 sütunlu ızgarada**, 0.5 pt `paperLine` çizgileriyle ayrılır.
- **Parti kutusu:** 40 pt logo, kod (13/700), parti adı (11, en fazla 2 satır). Altta **boş bir mühür alanı** (62 pt), gerçek pusuladaki gibi. Burada daire, radyo düğmesi vb. olmayacak.
- **Lider kutusu:** 42×50 fotoğraf yeri (şimdilik baş harfler), liderin adı (12/700), parti kodu.
- **Mühür:** Kutuya dokununca boş alana 54 pt çift halkalı mürekkep mührü basılır.
  - Parti ve Lider sekmelerinde "EVET", Asla sekmesinde "ASLA" yazar.
  - Her bölümde tek seçim yapılır. Aynı kutuya tekrar dokunmak mührü kaldırır.
- **Alt kısım:**
  - Ana düğme: "Pusulayı katla ve zarfa koy" (mürekkep rengi, 50 pt).
  - Altında: "Lider ve Asla bölümleri isteğe bağlıdır."
  - Parti mührü yoksa düğme pasif olmalı (tasarımda yok, ekle).
- **Hedef his:** Gerçekten oy veriyormuş gibi; sessiz, ciddi, kabin atmosferi.

---

## 8. Ekran 6: Zarf ve sandık

Referans: `Sealed`.

- **Çizim:** Şeffaf, geometrik bir sandık: koyu kapak ve yarık, yarı saydam gövde, içinde birkaç kraft zarf. Üstten bir zarf sandığa düşer. Gerekirse vektörle (SwiftUI Shape) yeniden çiz.
- **Metin:** Başlık "Oyun sandıkta" (28/700). Altında: "Tercihin anonim olarak … sonuçlarına eklendi." (15, ikincil renk).
- **Mühür özeti (gruplu liste):** Parti / Lider / Asla satırları, her birinin sağında küçük bir mürekkep "EVET" / "ASLA" rozeti.
- **Ayar satırı:** "Kilit ekranında takip et" iOS anahtarı.
- **Ana düğme:** "Kilit ekranında gör".

---

## 9. Örnek veri (Genel Seçim Anketi)

Bu tabloyu mock JSON olarak kullan. Değerler yüzde cinsindendir.

| # | Kod | Parti | Lider | Logo rengi | Parti ▲ | Lider ▲ | Parti ▼ | Lider ▼ |
|---|---|---|---|---|---|---|---|---|
| 1 | YUP | Yeni Ufuk Partisi | Selin Arda | #2F5FD0 | 16,4 | 18,9 | 22,1 | 25,4 |
| 2 | ADP | Anadolu Demokrasi Partisi | Kemal Tunç | #B0441A | 15,9 | 13,2 | 23,8 | 26,9 |
| 3 | BHP | Birlik ve Huzur Partisi | Murat Ergin | #0E6F57 | 11,2 | 14,8 | 9,2 | 7,8 |
| 4 | HLK | Halkın Kalkınma Partisi | Ayşe Doğan | #B42D45 | 8,7 | 7,1 | 6,4 | 8,1 |
| 5 | ÇEP | Çevre ve Emek Partisi | Deniz Kaya | #3B7F2A | 6,3 | 8,4 | 3,1 | 2,6 |
| 6 | GSP | Gelecek Sesi Partisi | Emre Yalın | #6A44C2 | 5,1 | 4,2 | 4,8 | 5,5 |
| 7 | MİD | Milli Dayanışma Partisi | Hakan Sezer | #7A5C0F | 4,4 | 5,6 | 7,9 | 6,2 |
| 8 | ÖTP | Özgür Toplum Partisi | Leyla Aksoy | #A8327A | 3,8 | 3,1 | 2,2 | 2,9 |

- Toplam 40 parti vardır. Kalan 32 parti `design-reference/v2-onaylanan/Ballot.dc.html` içindeki `RAW` dizisinde ve v1 dosyalarında bulunur.
- Logolar şimdilik **parti renginde daire + kodun ilk harfi** şeklinde yer tutuculardır. Gerçek logo varlıkları için bir `PartyLogo` bileşeni tasarla ki sonradan kolayca değiştirilebilsin.
- **Diğer örnek anketler:**

  | Anket | Tür | Oy sayısı | Durum |
  |---|---|---|---|
  | Genel Seçim Anketi | Destek + Asla | 128.460 | katıldı |
  | Genç Seçmen Anketi | Destek + Asla | 23.118 | katıldı |
  | Lider Tercihi Anketi | Yalnız destek | 84.215 | katılmadı |
  | Asla Oy Vermem Anketi | Yalnız asla | 61.902 | katılmadı |

- **Örnek kullanıcı oyu:** Parti BHP – EVET, Lider Murat Ergin – EVET, Asla ADP.

---

## 10. Veri modeli ve canlı altyapı (öneri – onay al)

```text
Party      { id, code, name, leaderName, color, logoAsset? }
Poll       { id, title, type: support | never | supportAndNever, includesLeader: Bool,
             participantCount, isLive, partyIds[] }
PollResult { pollId, updatedAt, rows: [ { partyId, partyPos%, leaderPos%, partyNeg%, leaderNeg%,
             partyPosCount, leaderPosCount, partyNegCount, leaderNegCount } ] }   // olumlu orana göre sıralı
Ballot     { pollId, partyYes: partyId?, leaderYes: partyId?, never: partyId?, castAt }
UserPrefs  { followedPollIds[], lockScreenPollId?, notificationsByPoll{} }
```

Önerini sunarken şunları ele al:

- **Canlı güncelleme:** WebSocket, Supabase Realtime veya Firebase gibi seçenekleri artı ve eksileriyle karşılaştır. Widget ve Live Activity için push güncellemeyi planla.
- **Tek kişi – tek oy ve sahteciliğe karşı koruma:** Sign in with Apple, DeviceCheck / App Attest, hız sınırlama.
- **Anonimlik:** Oy kaydı kimlikle ilişkilendirilmemeli. Toplu sayımlar dışında kişisel tercih saklanmamalı (ya da yalnızca cihazda saklanmalı).
- **Widget ile uygulama arasında veri paylaşımı:** App Group.

---

## 11. Kapsam dışı / eski taslak

`design-reference/v1-eski-taslak/` **onaylanmamış** ilk taslaktır. Yalnızca fikir kaynağı olarak bak; görsel dilini kullanma.

v1'de olup v2'de bulunmayan özellikler şunlar: 8 sütunlu tam pusula ızgarası, 1./2./3. öncelik seçimi, sıralı canlı sonuç listesi, lider sıralama ekranı, küçük/orta/büyük ana ekran widget'ları. Bunların ürüne girip girmeyeceğini kullanıcıya sor (Bölüm 13).

---

## 12. Önerilen aşamalar

0. **Keşif:** Dosyaları oku, soruları sor, mimari planı sun, onay al.
1. **Temel:** Xcode projesi (SwiftUI, önerilen minimum iOS 17), tasarım token'ları, `PartyLogo`, `VoteCandlePair` (artı/eksi, animasyonlu) bileşenleri, mock veri katmanı.
2. **Şerit:** Uygulama içi kilit ekranı replikası (Ekran 1–3), basılı tut + kaydır jesti, haptikler, VoiceOver. **En çok özen gösterilecek aşama budur.**
3. **Oy akışı:** Anketler → Oy Kabini (pusula ve mühür) → Zarf ve sandık.
4. **WidgetKit:** `accessoryRectangular` kilit ekranı widget'ı (seçenek A), tek renkli moda uygun mum çizimi, App Group.
5. **Live Activity** (seçilirse).
6. **Backend:** Canlı sonuçlar ve tek oy güvencesi.

Her aşamanın sonunda: SwiftUI Preview ekran görüntüsü, kısa bir değişiklik özeti ve küçük, anlamlı commit'ler.

---

## 13. Kullanıcıya sorulacak açık sorular

1. iOS kısıtı için hangi yol izlenecek: A (hibrit, öneri), B veya C?
2. Platform: yalnızca iPhone mu? Minimum iOS sürümü 17 olabilir mi?
3. Backend tercihi var mı? (Supabase, Firebase, kendi sunucusu)
4. Kimlik doğrulama: Sign in with Apple zorunlu mu, yoksa anonim cihaz doğrulaması mı?
5. v1'deki özellikler (öncelik sıralaması, sonuç listesi, ana ekran widget'ları) ürüne girecek mi?
6. Kilit ekranında 8 parti sabit mi kalsın, yoksa kullanıcı kendi takip ettiği partileri seçebilsin mi?
7. Mum rengi: kâğıt beyazı (`#F3EBDD`) mı kalsın, yoksa mühür mürekkebiyle (`ink`) birleştirilsin mi? (Uygulama içinde mürekkep rengi, kilit ekranında kâğıt beyazı da olabilir.)
8. Uygulama ekranları için karanlık mod?
9. Anket yayın yasağı dönemlerinde uygulama ne yapacak? (Hukukçu görüşü alınmalı.)

---

## 14. Referans dosyalar hakkında not

- `design-reference/**/*.dc.html` dosyaları claude.ai tasarım tuvaline ait HTML prototiplerdir. Kendi çalışma ortamları (`support.js`) olmadan tarayıcıda düzgün açılmayabilirler.
- Bu dosyaları **spesifikasyon olarak oku.** Tüm ölçüler, renkler, boşluklar ve yazı boyutları satır içi `style` niteliklerinde, veriler ve hesaplamalar ise dosyanın sonundaki `<script>` bloğunda (`renderVals`) bulunur.
- **En önemli dosya `LockRest.dc.html`.** Jest mantığı (`colFrom`, `down`, `move`, `up`) ve mum ölçeklemesi oradadır.

---

## 15. Aşama 0 kararları (kullanıcı onayladı, 25 Ekim 2026)

Bölüm 13'teki sorular ve keşifte çıkan ek konular şöyle karara bağlandı. Değiştirmeden önce kullanıcıya sor.

1. **iOS kısıtı:** A (hibrit). Tasarımın birebir aynısı uygulamadaki "Kilit Ekranı" sekmesinde; gerçek kilit ekranında sade `accessoryRectangular` widget (en fazla yarım genişlik, ≈160 × 72 pt, tek ton); seçim gecesi Live Activity.
2. **Platform:** Yalnızca iPhone, en az iOS 17. Dikey yön.
3. **Backend:** Aşama 6'da kesinleşecek; eğilim Supabase. Aşama 1–5 `PollRepository` protokolünün arkasındaki örnek veriyle çalışır.
4. **Kimlik:** Oy vermek için Sign in with Apple + App Attest; gezinmek için giriş yok. Sunucu yalnızca "bu kişi bu ankette oy kullandı" bilgisini, Apple kimliğinden tek yönlü türetilmiş bir kodla tutar. Tercih kimliğe bağlanmadan toplam sayılara eklenir; pusula yalnızca cihazda kalır.
5. **v1 özellikleri:** Şimdilik yok; v2 bitince yeniden bakılacak.
6. **Kilit ekranındaki partiler:** Sıralamada en çok oy alan 8 parti; kullanıcı seçmez (seçerse tercihini ele verebilir).
7. **Mum rengi:** `#F3EBDD` her yerde.
8. **Karanlık mod:** Token'lar açık/koyu çift olarak tanımlı (`Palette`). Koyu palet onaylanana kadar uygulama ekranları açık modda sabit.
9. **Yayın yasağı:** Hukukçuya danışılacak (siyasi görüş KVKK'da özel nitelikli veri; anonimlik tasarımı da gösterilecek). Teknik hazırlık: sunucudan açılıp kapanan bir "yayın durduruldu" anahtarı.
10. **Sekme ve gezinme çubukları:** Sistem çubukları kullanılır (iOS 26'da Liquid Glass görünümü).
11. **Bundle ID:** `com.salihcanbolat.secimanketi`. Ücretli geliştirici hesabı Aşama 4'ten önce gerekli (App Group, widget, Sign in with Apple, Live Activity).
12. **Ekran görüntüleri:** GitHub Actions'ın macOS makinesinde üretilir (bulut oturumunda Xcode yok).
13. **Hareket:** Kullanıcının isteği: animasyonlar çok güzel ve iOS tarzında olmalı. Tüm geçişler iOS yay eğrileriyle yapılır ve yarıda kesilebilir.

## 16. Geliştirme notları

- Xcode projesi `project.yml`'den üretilir (`brew install xcodegen && xcodegen generate`); `.xcodeproj` depoya girmez.
- `Packages/PollCore`: arayüzsüz çekirdek (modeller, örnek veri, `CandleScale`, `StripGeometry`); `swift test` ile test edilir.
- `Packages/DesignSystem`: token'lar, `PartyLogo`, `VoteCandlePair`.
- Örnek veri `tools/generate_mock_data.py` ile üretilir; JSON'u elle düzenleme.
- CI (`.github/workflows/ios.yml`) her push'ta derler ve test eder. Commit mesajında `[ekran]` varsa ekran görüntülerini ve videoyu aynı dalda `docs/screenshots/latest/` altına yazar; sonraki push'tan önce `git pull --rebase` gerekir.
