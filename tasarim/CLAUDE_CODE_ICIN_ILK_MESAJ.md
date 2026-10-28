# Claude Code'a yapıştırılacak ilk mesaj

Aşağıdaki metni, paketi projeye ekledikten sonra Claude Code'a gönder.

---

Projenin kökünde `tasarim/` klasöründe bir tasarım teslim paketi var. Uygulamayı bu tasarıma birebir uygun şekilde, yalnızca Android için geliştirmeni istiyorum.

1. Önce `tasarim/TASARIM.md` dosyasını baştan sona oku, sonra `tasarim/tokens.json` dosyasını oku.
2. `tasarim/ekranlar/` içindeki HTML dosyaları her ekranın tasarım kaynağı. Bunları çalıştırma; düzeni, ölçüleri, renkleri, animasyonları ve en alttaki `<script>` bölümündeki davranış mantığını referans olarak oku.
3. Logolar ve fotoğraflar `tasarim/varliklar/` içinde, hazır kırpılmış halde. Yeniden düzenleme ya da renklendirme yapma.
4. Kotlin + Jetpack Compose kullan. Renkleri, yazı tiplerini, köşe yarıçaplarını ve animasyon eğrilerini `tokens.json`'dan tek bir tema dosyasına aktar; ekranlarda sabit değer kullanma.
5. Şu sırayla ilerle ve her adımdan sonra bana neyi yaptığını ve tasarımdan sapmak zorunda kaldığın yerleri söyle:
   1. Tema ve ortak bileşenler, özellikle **mum şeridi**.
   2. **4 numaralı ekran:** basılı tut, kaydır, titreşim, bilgi kartı.
   3. **5 numaralı ekran:** kilit ekranı bildirimi ve bildirime dokununca 4'ün kilit açılmadan açılması.
   4. 1, 2 ve 3 numaralı ekranlar.
   5. 6 numaralı ekrandaki durumlar.
6. `TASARIM.md` içindeki "Kesin kurallar" bölümüne mutlaka uy. Özellikle kullanıcının kendi oyu kilit ekranında hiçbir zaman görünmemeli.
7. Şimdilik gerçek veri yok. Tüm partiler eşit, sembolik değerlerle gösterilsin ve bu açıkça belirtilsin.
