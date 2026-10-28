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
)

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

    init {
        Anketler.tumu.forEach { anket ->
            ziller[anket.id] = tercihler?.getBoolean(ZIL + anket.id, false) ?: false
            tercihler?.getString(OY + anket.id, null)?.let { oyuCoz(anket.id, it) }?.let { oylar[anket.id] = it }
        }
    }

    val kilitAnketi: Anket get() = Anketler.bul(kilitAnketiId)

    fun zilAcikMi(anketId: String): Boolean = ziller[anketId] == true
    fun oy(anketId: String): Oy? = oylar[anketId]
    fun katildiMi(anketId: String): Boolean = oylar.containsKey(anketId)

    fun kilideAl(anketId: String) {
        kilitAnketiId = anketId
        tercihler?.edit()?.putString(KILIT, anketId)?.apply()
    }

    fun zilDegistir(anketId: String) {
        val yeni = !zilAcikMi(anketId)
        ziller[anketId] = yeni
        tercihler?.edit()?.putBoolean(ZIL + anketId, yeni)?.apply()
    }

    /** Bir ankette yalnızca bir kez oy verilir; ikinci kayıt yok sayılır. */
    fun oyKaydet(oy: Oy): Boolean {
        if (katildiMi(oy.anketId)) return false
        oylar[oy.anketId] = oy
        tercihler?.edit()?.putString(OY + oy.anketId, oyuYaz(oy))?.apply()
        return true
    }

    private fun oyuYaz(oy: Oy) = listOf(oy.partiId, oy.liderPartiId, oy.aslaPartiId).joinToString("|") { it ?: "" } + "|" + oy.zaman

    private fun oyuCoz(anketId: String, metin: String): Oy? {
        val parcalar = metin.split('|')
        if (parcalar.size != 4) return null
        fun bos(s: String) = s.ifEmpty { null }
        return Oy(anketId, bos(parcalar[0]), bos(parcalar[1]), bos(parcalar[2]), parcalar[3].toLongOrNull() ?: 0L)
    }

    companion object {
        private const val DOSYA = "uygulama_durumu"
        private const val KILIT = "kilit_anketi"
        private const val ZIL = "zil:"
        private const val OY = "oy:"

        fun yukle(context: Context) = UygulamaDurumu(context.getSharedPreferences(DOSYA, Context.MODE_PRIVATE))
    }
}
