package com.salihcanbolat.secimanketi.veri

import androidx.annotation.DrawableRes
import java.util.Locale

private val Turkce = Locale.forLanguageTag("tr")

data class Lider(
    val ad: String,
    @DrawableRes val foto: Int? = null,
) {
    /** "Recep Tayyip Erdoğan" → "RE" (ilk ve son adın baş harfleri). */
    val basHarfler: String
        get() {
            val kelimeler = ad.split(' ').filter { it.isNotBlank() }
            val ilk = kelimeler.firstOrNull()?.take(1) ?: ""
            val son = if (kelimeler.size > 1) kelimeler.last().take(1) else ""
            return (ilk + son).uppercase(Turkce)
        }
}

data class Parti(
    val id: String,
    /** Etiketlerde kullanılan kısa ad ("AK Parti"). */
    val kisaAd: String,
    /** Resmî ad ("Adalet ve Kalkınma Partisi"). */
    val uzunAd: String,
    /** Logosuz kutudaki kısa kod ("YENİ"). */
    val kod: String,
    @DrawableRes val logo: Int?,
    /** Logo yokken gösterilen nötr glif (24×24 SVG yolu). */
    val glif: String,
    /** Bir ya da iki lider (eş genel başkanlar birlikte gösterilir ve birlikte seçilir). */
    val liderler: List<Lider>,
    /** Meclis sandalye sırası (1 = en çok sandalye). Eşitlikte sıralama ölçütü. */
    val sandalyeSirasi: Int,
) {
    val liderAdlari: String get() = liderler.joinToString(" & ") { it.ad }
}

enum class AnketTuru(
    /** Kartlardaki tür etiketi. */
    val etiket: String,
    /** Katılınmamış anketin açıklaması. */
    val aciklama: String,
) {
    DESTEK_ASLA("DESTEK + ASLA", "Parti, lider ve “asla” · 3 seçim"),
    YALNIZ_DESTEK("YALNIZ DESTEK", "Parti ve lider · 2 seçim"),
    YALNIZ_ASLA("YALNIZ ASLA", "Yalnızca “asla” bölümü · 1 seçim");

    val destekVar: Boolean get() = this != YALNIZ_ASLA
    val aslaVar: Boolean get() = this != YALNIZ_DESTEK
}

data class Anket(
    val id: String,
    val ad: String,
    val tur: AnketTuru,
    /** Kilit ekranı etiketlerindeki kısa ad ("GENEL · SEMBOLİK"). */
    val kisaAd: String = ad,
)

/** Değerler 0–1 aralığında oran; anket o bölümü toplamıyorsa null. */
data class Sonuc(
    val partiId: String,
    val partiDestek: Float?,
    val liderDestek: Float?,
    val partiyeAsla: Float?,
    val lidereAsla: Float?,
)

data class AnketDurumu(
    val katilimciSayisi: Int,
    val sonGuncelleme: Long?,
    /** Gerçek oy yok: tüm partiler eşit, yüzde yerine "%—" gösterilir. */
    val sembolikMi: Boolean,
    val yayinYasagiMi: Boolean,
    val yayinYasagiBitis: Long?,
    /** Bağlantı yoksa son bilinen sonuçlar gösterilir (Ekran 6, "Bağlantı yok"). */
    val baglantiVar: Boolean = true,
) {
    /** Ekran 6: sonuçların nasıl gösterileceği. Yayın yasağı her şeyin önündedir. */
    val gorunum: SonucGorunumu
        get() = when {
            yayinYasagiMi -> SonucGorunumu.YAYIN_DURDURULDU
            sembolikMi -> SonucGorunumu.SEMBOLIK
            katilimciSayisi == 0 -> SonucGorunumu.HENUZ_OY_YOK
            else -> SonucGorunumu.CANLI
        }

    /** Bağlantı yok: son bilinen veri gösteriliyor. Yayın yasağında sonuç zaten gösterilmez. */
    val cevrimdisi: Boolean get() = !baglantiVar && !yayinYasagiMi

    val sembolikGosterim: Boolean get() = gorunum == SonucGorunumu.SEMBOLIK
}

enum class SonucGorunumu {
    /** Gerçek oylar, canlı. */
    CANLI,
    /** Gerçek oy gelene kadar tüm partiler eşit. */
    SEMBOLIK,
    /** Anket açık, ilk oy gelmedi: logolar Meclis sırasıyla, mum yok. */
    HENUZ_OY_YOK,
    /**
     * Seçim öncesi yayın yasağı: hiçbir yerde sonuç gösterilmez. Logoların sırası da sonucu ele
     * vereceği için şerit hiç çizilmez.
     */
    YAYIN_DURDURULDU,
}

/** Şeritteki bir sütun. */
data class SeritOgesi(val parti: Parti, val sonuc: Sonuc)

/** Parti oyuna göre en çoktan en aza; eşitlikte Meclis sandalye sırası. Şerit ilk 8'i gösterir. */
fun seritSirasi(partiler: List<Parti>, sonuclar: List<Sonuc>, adet: Int = 8): List<SeritOgesi> {
    val sonucMap = sonuclar.associateBy { it.partiId }
    return partiler
        .mapNotNull { parti -> sonucMap[parti.id]?.let { SeritOgesi(parti, it) } }
        .sortedWith(compareByDescending<SeritOgesi> { it.sonuc.partiDestek ?: 0f }.thenBy { it.parti.sandalyeSirasi })
        .take(adet)
}

/** 0.164 → "%16,4"; sembolik veride ya da değer yokken "%—". */
fun yuzde(oran: Float?, sembolik: Boolean): String {
    if (sembolik || oran == null) return "%—"
    return "%" + String.format(Locale.ROOT, "%.1f", oran * 100).replace('.', ',')
}
