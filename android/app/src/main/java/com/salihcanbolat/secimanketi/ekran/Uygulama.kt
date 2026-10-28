package com.salihcanbolat.secimanketi.ekran

import android.content.pm.ApplicationInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.bilesen.GirisGerekliIcerigi
import com.salihcanbolat.secimanketi.bilesen.altBosluk
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeEkrani
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.OrnekDurumlar
import com.salihcanbolat.secimanketi.veri.SonucGorunumu
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.UygulamaDurumu
import java.time.LocalDate

/** Uygulamadaki ekranlar. Yığının ilk öğesi her zaman bir sekme kökü (Anketler ya da Canlı). */
sealed interface Hedef {
    data object AnketListesi : Hedef
    data object Canli : Hedef
    data class Kilit(val anketId: String) : Hedef
    data class OyVerme(val anketId: String) : Hedef
    /** [oncekiKilitAnketi]: "Kilit ekranında takip et" kapatılınca geri dönülecek anket. */
    data class OyGonderildi(val anketId: String, val oncekiKilitAnketi: String) : Hedef

    val kok: Boolean get() = this == AnketListesi || this == Canli
}

private val YiginKaydedici = Saver<List<Hedef>, ArrayList<String>>(
    save = { yigin ->
        ArrayList(
            yigin.map {
                when (it) {
                    Hedef.AnketListesi -> "anketler"
                    Hedef.Canli -> "canli"
                    is Hedef.Kilit -> "kilit:${it.anketId}"
                    is Hedef.OyVerme -> "oy:${it.anketId}"
                    is Hedef.OyGonderildi -> "gonderildi:${it.anketId}:${it.oncekiKilitAnketi}"
                }
            },
        )
    },
    restore = { kayit ->
        kayit.mapNotNull {
            val p = it.split(':')
            when (p[0]) {
                "anketler" -> Hedef.AnketListesi
                "canli" -> Hedef.Canli
                "kilit" -> Hedef.Kilit(p[1])
                "oy" -> Hedef.OyVerme(p[1])
                "gonderildi" -> Hedef.OyGonderildi(p[1], p[2])
                else -> null
            }
        }.ifEmpty { listOf(Hedef.AnketListesi) }
    },
)

/**
 * Uygulamanın gezinmesi. Alt gezinme yalnızca sekme köklerinde görünür. Geçişler iOS'taki gibi
 * yatay itme/geri çekme; kilit ekranı görünümüne geçişte yalnızca solma (ekran "kilit ekranı" olur).
 *
 * @param onKilitAnketiDegisti Kilit ekranındaki anket ya da durumu değişince (bildirimi güncellemek için).
 */
@Composable
fun Uygulama(
    durum: UygulamaDurumu,
    modifier: Modifier = Modifier,
    onKilitAnketiDegisti: (Anket, AnketDurumu) -> Unit = { _, _ -> },
    baslangic: List<Hedef> = listOf(Hedef.AnketListesi),
    bugun: LocalDate = LocalDate.now(),
) {
    var yigin by rememberSaveable(stateSaver = YiginKaydedici) { mutableStateOf(baslangic) }
    var ileri by remember { mutableStateOf(true) }

    fun git(hedef: Hedef) {
        ileri = true
        yigin = yigin + hedef
    }
    fun yerineGec(hedef: Hedef) {
        ileri = true
        yigin = yigin.dropLast(1) + hedef
    }
    fun kokeDon(hedef: Hedef) {
        ileri = false
        yigin = listOf(hedef)
    }
    fun geri() {
        ileri = false
        yigin = if (yigin.size > 1) yigin.dropLast(1) else listOf(Hedef.AnketListesi)
    }

    // Giriş gerekli paneli (Ekran 6): hesap düğmesinden ve giriş zorunluyken gönderirken açılır.
    var girisPaneli by rememberSaveable { mutableStateOf(false) }
    var girisBilgisi by remember { mutableStateOf<String?>(null) }
    fun girisiKapat() {
        girisPaneli = false
        girisBilgisi = null
    }

    BackHandler(enabled = yigin.size > 1 || yigin.last() == Hedef.Canli) { geri() }
    BackHandler(enabled = girisPaneli) { girisiKapat() }
    val kilitDurumu = durum.anketDurumu(durum.kilitAnketiId)
    LaunchedEffect(durum.kilitAnketiId, kilitDurumu) { onKilitAnketiDegisti(durum.kilitAnketi, kilitDurumu) }

    // Hata ayıklama sürümünde, sunucu yokken Ekran 6 durumlarını telefonda görebilmek için.
    val hataAyiklama = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    val ust = yigin.last()
    val azalt = LocalHareketAzalt.current
    val gezinmeEk = altBosluk(26.dp) - 26.dp

    Box(modifier.fillMaxSize().background(Renk.Zemin)) {
        AnimatedContent(
            targetState = ust,
            transitionSpec = { gecis(initialState, targetState, ileri, azalt) },
            label = "gezinme",
        ) { hedef ->
            when (hedef) {
                Hedef.AnketListesi -> AnketlerEkrani(
                    durum = durum,
                    onIncele = { git(Hedef.Kilit(it.id)) },
                    onKatil = { git(Hedef.OyVerme(it.id)) },
                    onHesap = { girisPaneli = true },
                    bugun = bugun,
                    altBoslukEk = gezinmeEk,
                    onYenidenDene = {
                        // Sunucu gelene kadar çevrimdışı durum yalnızca önizlemede; yeniden dene onu kapatır.
                        val id = durum.kilitAnketiId
                        durum.anketDurumunuGuncelle(id, durum.anketDurumu(id).copy(baglantiVar = true))
                    },
                    onDurumOnizleme = if (hataAyiklama) {
                        {
                            val id = durum.kilitAnketiId
                            durum.anketDurumunuGuncelle(id, sonrakiOnizleme(durum.anketDurumu(id)))
                        }
                    } else {
                        null
                    },
                )
                Hedef.Canli -> CanliEkrani(durum.kilitAnketi, kilitDurumu, bugun = bugun, altBoslukEk = gezinmeEk)
                is Hedef.Kilit -> KilitIncelemeEkrani(anket = Anketler.bul(hedef.anketId), anketDurumu = durum.anketDurumu(hedef.anketId))
                is Hedef.OyVerme -> OyVermeEkrani(
                    anket = Anketler.bul(hedef.anketId),
                    onGeri = ::geri,
                    onGonder = { oy ->
                        if (UygulamaDurumu.GIRIS_ZORUNLU && !durum.girisYapildi) {
                            girisPaneli = true
                            return@OyVermeEkrani
                        }
                        val onceki = durum.kilitAnketiId
                        if (durum.oyKaydet(oy)) {
                            // "Kilit ekranında takip et" varsayılan olarak açık.
                            durum.kilideAl(oy.anketId)
                            yerineGec(Hedef.OyGonderildi(oy.anketId, onceki))
                        } else {
                            geri()
                        }
                    },
                )
                is Hedef.OyGonderildi -> {
                    val oy = durum.oy(hedef.anketId)
                    if (oy == null) {
                        LaunchedEffect(Unit) { kokeDon(Hedef.AnketListesi) }
                    } else {
                        OyGonderildiEkrani(
                            anket = Anketler.bul(hedef.anketId),
                            oy = oy,
                            kilitteTakip = durum.kilitAnketiId == hedef.anketId,
                            onTakipDegistir = { acik ->
                                if (acik) {
                                    durum.kilideAl(hedef.anketId)
                                } else if (hedef.oncekiKilitAnketi != hedef.anketId) {
                                    durum.kilideAl(hedef.oncekiKilitAnketi)
                                }
                            },
                            onKilitGorunumu = { yerineGec(Hedef.Kilit(hedef.anketId)) },
                            onAnketlereDon = { kokeDon(Hedef.AnketListesi) },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = ust.kok,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = if (azalt) EnterTransition.None else fadeIn(tween(300)) + slideInVertically(tween(450, easing = Hareket.Giris)) { it / 2 },
            exit = if (azalt) ExitTransition.None else fadeOut(tween(200)) + slideOutVertically(tween(300, easing = Hareket.Giris)) { it / 2 },
        ) {
            AltGezinme(
                secili = if (ust == Hedef.Canli) Sekme.CANLI else Sekme.ANKETLER,
                onSec = { sekme ->
                    when (sekme) {
                        Sekme.ANKETLER -> if (ust != Hedef.AnketListesi) kokeDon(Hedef.AnketListesi)
                        Sekme.KILIT -> git(Hedef.Kilit(durum.kilitAnketiId))
                        Sekme.CANLI -> if (ust != Hedef.Canli) kokeDon(Hedef.Canli)
                    }
                },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = altBosluk(26.dp)),
            )
        }

        GirisPaneli(
            gorunur = girisPaneli,
            bilgi = girisBilgisi,
            onGiris = { girisBilgisi = "Google ile giriş bu sürümde henüz yok; sunucu bağlanınca açılacak." },
            onKapat = ::girisiKapat,
        )
    }
}

/** Alttan açılan "Giriş gerekli" paneli; arkası kararır, dışına dokununca kapanır. */
@Composable
private fun BoxScope.GirisPaneli(gorunur: Boolean, bilgi: String?, onGiris: () -> Unit, onKapat: () -> Unit) {
    val azalt = LocalHareketAzalt.current
    AnimatedVisibility(
        visible = gorunur,
        enter = if (azalt) EnterTransition.None else fadeIn(tween(250)),
        exit = if (azalt) ExitTransition.None else fadeOut(tween(200)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(remember { MutableInteractionSource() }, null, onClickLabel = "Kapat", onClick = onKapat),
        )
    }
    AnimatedVisibility(
        visible = gorunur,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = if (azalt) EnterTransition.None else slideInVertically(tween(450, easing = Hareket.Giris)) { it } + fadeIn(tween(200)),
        exit = if (azalt) ExitTransition.None else slideOutVertically(tween(300, easing = Hareket.Giris)) { it } + fadeOut(tween(200)),
    ) {
        GirisGerekliIcerigi(
            onGiris = onGiris,
            onGozAt = onKapat,
            bilgi = bilgi,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = altBosluk(12.dp)),
        )
    }
}

/** Hata ayıklama önizlemesi: sembolik → henüz oy yok → yayın durduruldu → bağlantı yok → sembolik. */
private fun sonrakiOnizleme(d: AnketDurumu): AnketDurumu = when {
    d.cevrimdisi -> OrnekDurumlar.sembolik()
    d.gorunum == SonucGorunumu.YAYIN_DURDURULDU -> OrnekDurumlar.cevrimdisi(System.currentTimeMillis())
    d.gorunum == SonucGorunumu.HENUZ_OY_YOK -> OrnekDurumlar.yayinDurduruldu()
    d.gorunum == SonucGorunumu.SEMBOLIK -> OrnekDurumlar.henuzOyYok()
    else -> OrnekDurumlar.sembolik()
}

private fun gecis(onceki: Hedef, sonraki: Hedef, ileri: Boolean, azalt: Boolean): ContentTransform {
    val sure = 450
    return when {
        azalt -> EnterTransition.None togetherWith ExitTransition.None
        // Sekmeler arası: kısa solma
        onceki.kok && sonraki.kok -> fadeIn(tween(250)) togetherWith fadeOut(tween(200))
        // Kilit ekranı görünümü: ekran kayarak değil, solarak değişir
        onceki is Hedef.Kilit || sonraki is Hedef.Kilit ->
            fadeIn(tween(350, easing = Hareket.Yumusak)) togetherWith fadeOut(tween(300, easing = Hareket.Yumusak))
        // Oy gönderildi: yerinde, hafif büyüyerek belirir
        sonraki is Hedef.OyGonderildi ->
            (fadeIn(tween(400)) + scaleIn(tween(sure, easing = Hareket.Giris), initialScale = 0.96f)) togetherWith fadeOut(tween(250))
        ileri ->
            slideInHorizontally(tween(sure, easing = Hareket.Giris)) { it } togetherWith
                (slideOutHorizontally(tween(sure, easing = Hareket.Giris)) { -it / 4 } + fadeOut(tween(sure)))
        else -> (
            (slideInHorizontally(tween(sure, easing = Hareket.Giris)) { -it / 4 } + fadeIn(tween(sure))) togetherWith
                slideOutHorizontally(tween(sure, easing = Hareket.Giris)) { it }
            ).apply { targetContentZIndex = -1f }
    }
}
