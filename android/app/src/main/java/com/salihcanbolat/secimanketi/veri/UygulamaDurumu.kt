package com.salihcanbolat.secimanketi.veri

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Kullanıcının bir ankette verdiği oy. Yalnızca bu cihazda saklanır; sunucuya kimlikle birlikte
 * gönderilmez, kilit ekranında ve bildirimde hiçbir zaman gösterilmez.
 */
data class Oy(
    val anketId: String,
    val partiId: String?,
    val liderPartiId: String?,
    val aslaPartiId: String?,
    val zaman: Long,
) {
    /** Geçerli bir oyda en az parti ya da "asla" vardır; hepsi boşsa özet bu cihazdan silinmiştir. */
    val ozetSilindi: Boolean get() = partiId == null && liderPartiId == null && aslaPartiId == null
}

/**
 * Uygulamanın cihazdaki durumu: kilit ekranındaki anket, zil (bildirim) tercihleri ve verilen oylar.
 * [tercihler] null ise (önizleme ve testler) yalnızca bellekte tutulur.
 */
class UygulamaDurumu(
    private val tercihler: SharedPreferences? = null,
    varsayilanKilitAnketi: String = Anketler.genelSecim.id,
) {
    /** Kilit ekranında gösterilen anket. Aynı anda yalnızca bir anket seçili olabilir. */
    var kilitAnketiId by mutableStateOf(tercihler?.getString(KILIT, null) ?: varsayilanKilitAnketi)
        private set

    private val ziller = mutableStateMapOf<String, Boolean>()
    private val oylar = mutableStateMapOf<String, Oy>()

    /** Anketlerin sonuç durumu (Ekran 6). Sunucu gelene kadar hepsi sembolik. */
    private val anketDurumlari = mutableStateMapOf<String, AnketDurumu>()

    /**
     * Google ile giriş yapıldı mı. Giriş sunucu aşamasında bağlanacak; o zamana kadar false ve
     * oy göndermek için zorunlu tutulmuyor ([GIRIS_ZORUNLU]).
     */
    var girisYapildi by mutableStateOf(false)
        private set

    /**
     * İlk açılış tanıtımı görüldü mü. Bellekteki durumda (önizleme ve testler) görülmüş sayılır;
     * cihazda ilk açılışta bir kez gösterilir.
     */
    var tanitimGoruldu by mutableStateOf(tercihler?.getBoolean(TANITIM, false) ?: true)
        private set

    fun tanitimiTamamla() {
        tanitimGoruldu = true
        tercihler?.edit()?.putBoolean(TANITIM, true)?.apply()
    }

    /** Kilit ekranı bildirimi açık mı (Hesap ve ayarlar). */
    var bildirimAcik by mutableStateOf(tercihler?.getBoolean(BILDIRIM, true) ?: true)
        private set

    fun bildirimiAc(acik: Boolean) {
        bildirimAcik = acik
        tercihler?.edit()?.putBoolean(BILDIRIM, acik)?.apply()
    }

    init {
        Anketler.tumu.forEach { anket ->
            ziller[anket.id] = tercihler?.getBoolean(ZIL + anket.id, false) ?: false
            tercihler?.getString(OY + anket.id, null)?.let { oyuCoz(anket.id, it) }?.let { oylar[anket.id] = it }
            tercihler?.getString(DURUM + anket.id, null)?.let(::durumuCoz)?.let { anketDurumlari[anket.id] = it }
        }
    }

    val kilitAnketi: Anket get() = Anketler.bul(kilitAnketiId)

    fun anketDurumu(anketId: String): AnketDurumu = anketDurumlari[anketId] ?: SembolikVeri.durum()

    /** Sunucudan (ya da hata ayıklama önizlemesinden) gelen durum. */
    fun anketDurumunuGuncelle(anketId: String, durum: AnketDurumu) {
        anketDurumlari[anketId] = durum
        tercihler?.edit()?.putString(DURUM + anketId, durumuYaz(durum))?.apply()
    }

    /** Kilit ekranındaki anketin şerit öğeleri, durumuna göre (yayın yasağında boş). */
    fun serit(anket: Anket): List<SeritOgesi> = SembolikVeri.serit(anket, anketDurumu(anket.id))

    fun zilAcikMi(anketId: String): Boolean = ziller[anketId] == true
    fun oy(anketId: String): Oy? = oylar[anketId]
    fun katildiMi(anketId: String): Boolean = oylar.containsKey(anketId)

    /** Bu cihazda silinmemiş en az bir oy özeti var mı. */
    val ozetVarMi: Boolean get() = oylar.values.any { !it.ozetSilindi }

    fun kilideAl(anketId: String) {
        kilitAnketiId = anketId
        tercihler?.edit()?.putString(KILIT, anketId)?.apply()
    }

    fun zilDegistir(anketId: String) {
        val yeni = !zilAcikMi(anketId)
        ziller[anketId] = yeni
        tercihler?.edit()?.putBoolean(ZIL + anketId, yeni)?.apply()
    }

    /**
     * Cihazdaki oy özetlerini (kime oy verildiği) siler. Katılım bilgisi kalır: bir ankette tek oy
     * kuralı sürer; sunucu da yalnızca "oy kullandı" bilgisini tutar.
     */
    fun oyOzetleriniSil() {
        oylar.values.toList().forEach { oy ->
            val bos = oy.copy(partiId = null, liderPartiId = null, aslaPartiId = null)
            oylar[oy.anketId] = bos
            tercihler?.edit()?.putString(OY + oy.anketId, oyuYaz(bos))?.apply()
        }
    }

    /** Bir ankette yalnızca bir kez oy verilir; ikinci kayıt yok sayılır. */
    fun oyKaydet(oy: Oy): Boolean {
        if (katildiMi(oy.anketId)) return false
        oylar[oy.anketId] = oy
        tercihler?.edit()?.putString(OY + oy.anketId, oyuYaz(oy))?.apply()
        return true
    }

    private fun oyuYaz(oy: Oy) = listOf(oy.partiId, oy.liderPartiId, oy.aslaPartiId).joinToString("|") { it ?: "" } + "|" + oy.zaman

    /** Son bilinen durum (bağlantı yokken gösterilir). Kişisel bilgi içermez. */
    private fun durumuYaz(d: AnketDurumu) = listOf(
        d.katilimciSayisi, d.sonGuncelleme ?: "", d.sembolikMi, d.yayinYasagiMi, d.yayinYasagiBitis ?: "", d.baglantiVar,
    ).joinToString("|")

    private fun durumuCoz(metin: String): AnketDurumu? {
        val p = metin.split('|')
        if (p.size != 6) return null
        return AnketDurumu(
            katilimciSayisi = p[0].toIntOrNull() ?: return null,
            sonGuncelleme = p[1].toLongOrNull(),
            sembolikMi = p[2].toBoolean(),
            yayinYasagiMi = p[3].toBoolean(),
            yayinYasagiBitis = p[4].toLongOrNull(),
            baglantiVar = p[5].toBoolean(),
        )
    }

    private fun oyuCoz(anketId: String, metin: String): Oy? {
        val parcalar = metin.split('|')
        if (parcalar.size != 4) return null
        fun bos(s: String) = s.ifEmpty { null }
        return Oy(anketId, bos(parcalar[0]), bos(parcalar[1]), bos(parcalar[2]), parcalar[3].toLongOrNull() ?: 0L)
    }

    companion object {
        /** Google ile giriş bağlanınca true olacak; oy göndermeden önce giriş istenir. */
        const val GIRIS_ZORUNLU = false

        private const val DOSYA = "uygulama_durumu"
        private const val KILIT = "kilit_anketi"
        private const val ZIL = "zil:"
        private const val OY = "oy:"
        private const val DURUM = "durum:"
        private const val BILDIRIM = "bildirim_acik"
        private const val TANITIM = "tanitim_goruldu"

        fun yukle(context: Context) = UygulamaDurumu(context.getSharedPreferences(DOSYA, Context.MODE_PRIVATE))
    }
}
