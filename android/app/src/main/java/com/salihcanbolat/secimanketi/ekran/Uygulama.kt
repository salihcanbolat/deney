package com.salihcanbolat.secimanketi.ekran

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
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.bilesen.altBosluk
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeEkrani
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.veri.Anket
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
 * @param onKilitAnketiDegisti Kilit ekranındaki anket değişince (bildirimi güncellemek için).
 */
@Composable
fun Uygulama(
    durum: UygulamaDurumu,
    modifier: Modifier = Modifier,
    onKilitAnketiDegisti: (Anket) -> Unit = {},
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

    BackHandler(enabled = yigin.size > 1 || yigin.last() == Hedef.Canli) { geri() }
    LaunchedEffect(durum.kilitAnketiId) { onKilitAnketiDegisti(durum.kilitAnketi) }

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
                    onHesap = { /* Google ile giriş, sunucu aşamasında bağlanacak. */ },
                    bugun = bugun,
                    altBoslukEk = gezinmeEk,
                )
                Hedef.Canli -> CanliEkrani(durum.kilitAnketi, bugun = bugun, altBoslukEk = gezinmeEk)
                is Hedef.Kilit -> KilitIncelemeEkrani(anket = Anketler.bul(hedef.anketId))
                is Hedef.OyVerme -> OyVermeEkrani(
                    anket = Anketler.bul(hedef.anketId),
                    onGeri = ::geri,
                    onGonder = { oy ->
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
    }
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
