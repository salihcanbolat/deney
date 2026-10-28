package com.salihcanbolat.secimanketi.bilesen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.SeritOgesi
import com.salihcanbolat.secimanketi.veri.yuzde
import kotlinx.coroutines.delay

/** Şeridin durumu: hangi sütun açık, parmak basılı mı. */
@Stable
class SeritDurumu(acik: Int? = null, tutuluyor: Boolean = false) {
    var acik by mutableStateOf(acik)
    var tutuluyor by mutableStateOf(tutuluyor)
}

@Composable
fun rememberSeritDurumu(acik: Int? = null): SeritDurumu = remember { SeritDurumu(acik) }

private enum class DokunusSonu { Dokunus, Kaydirma, Iptal }

/**
 * Mum şeridi: 8 parti yan yana sütunlar halinde (uygulamanın kimliği).
 *
 * Etkileşim (Ekran 4):
 * - 240 ms basılı tutunca parmağın altındaki sütun açılır, hafif titreşim.
 * - Parmak kaldırılmadan kaydırılınca açık sütun parmağın altındaki partiye geçer; her geçişte titreşim.
 * - Bırakınca şerit kompakt hâline döner.
 * - Tek dokunuş da sütunu açar, tekrar dokunuş kapatır (erişilebilirlik).
 * - Basılı tutmadan yapılan kaydırma şeridi açmaz.
 */
@Composable
fun MumSeridi(
    ogeler: List<SeritOgesi>,
    sembolik: Boolean,
    durum: SeritDurumu,
    modifier: Modifier = Modifier,
    etkilesimli: Boolean = true,
    pariltiSurumu: Int = 0,
) {
    val haptik = LocalHapticFeedback.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val sutunSayisi = ogeler.size.coerceAtLeast(1)
        val adim: Dp = (maxWidth + Olcu.SutunAraligi) / sutunSayisi

        val jest = if (!etkilesimli) Modifier else Modifier.pointerInput(sutunSayisi) {
            fun sutun(x: Float) = (x / size.width * sutunSayisi).toInt().coerceIn(0, sutunSayisi - 1)
            awaitEachGesture {
                val basma = awaitFirstDown(requireUnconsumed = false)
                val sonuc = withTimeoutOrNull(Hareket.BasiliTutmaEsigiMs) {
                    var bitis: DokunusSonu? = null
                    while (bitis == null) {
                        val degisim = awaitPointerEvent().changes.firstOrNull { it.id == basma.id }
                        bitis = when {
                            degisim == null -> DokunusSonu.Iptal
                            !degisim.pressed -> DokunusSonu.Dokunus
                            (degisim.position - basma.position).getDistance() > viewConfiguration.touchSlop -> DokunusSonu.Kaydirma
                            else -> null
                        }
                    }
                    bitis
                }
                when (sonuc) {
                    null -> {
                        // Basılı tutma eşiği aşıldı.
                        durum.acik = sutun(basma.position.x)
                        durum.tutuluyor = true
                        haptik.performHapticFeedback(HapticFeedbackType.LongPress)
                        while (true) {
                            val degisim = awaitPointerEvent().changes.firstOrNull { it.id == basma.id }
                            if (degisim == null || !degisim.pressed) break
                            degisim.consume()
                            val yeni = sutun(degisim.position.x)
                            if (yeni != durum.acik) {
                                durum.acik = yeni
                                haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                        durum.tutuluyor = false
                        durum.acik = null
                    }
                    DokunusSonu.Dokunus -> {
                        val dokunulan = sutun(basma.position.x)
                        durum.acik = if (durum.acik == dokunulan) null else dokunulan
                    }
                    DokunusSonu.Kaydirma, DokunusSonu.Iptal -> Unit
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("serit")
                .then(jest),
            horizontalArrangement = Arrangement.spacedBy(Olcu.SutunAraligi),
            verticalAlignment = Alignment.Top,
        ) {
            ogeler.forEachIndexed { sira, oge ->
                val acik = durum.acik == sira
                SeritSutunu(
                    oge = oge,
                    sira = sira,
                    acik = acik,
                    tutuluyor = durum.tutuluyor,
                    baskasiAcik = durum.acik != null && !acik,
                    sembolik = sembolik,
                    pariltiSurumu = pariltiSurumu,
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            contentDescription = sutunEtiketi(oge, sira, sembolik)
                            stateDescription = if (acik) "Açık" else "Kapalı"
                            if (etkilesimli) {
                                onClick(label = if (acik) "Kapat" else "Aç") {
                                    durum.acik = if (acik) null else sira
                                    true
                                }
                            }
                        },
                )
            }
        }

        BilgiKartiYerlesimi(ogeler, durum.acik, sembolik, adim, maxWidth)
    }
}

/** Ekran okuyucu: sıra, parti adı, lider adı ve dört değer. */
private fun sutunEtiketi(oge: SeritOgesi, sira: Int, sembolik: Boolean): String {
    val liderler = oge.parti.liderler.joinToString(" ve ") { it.ad }
    val degerler = if (sembolik) {
        "Parti desteği ve lider desteği: sembolik veri."
    } else {
        val s = oge.sonuc
        listOfNotNull(
            s.partiDestek?.let { "parti desteği ${yuzde(it, false)}" },
            s.liderDestek?.let { "lider desteği ${yuzde(it, false)}" },
            s.partiyeAsla?.let { "partiye asla ${yuzde(it, false)}" },
            s.lidereAsla?.let { "lidere asla ${yuzde(it, false)}" },
        ).joinToString(", ").ifEmpty { "Henüz oy yok" } + "."
    }
    return "${sira + 1}. sıra, ${oge.parti.kisaAd}, lider $liderler. $degerler"
}

@Composable
private fun SeritSutunu(
    oge: SeritOgesi,
    sira: Int,
    acik: Boolean,
    tutuluyor: Boolean,
    baskasiAcik: Boolean,
    sembolik: Boolean,
    pariltiSurumu: Int,
    modifier: Modifier = Modifier,
) {
    val azalt = LocalHareketAzalt.current
    val sekil = RoundedCornerShape(Kose.Sutun)
    val yukseklik by animateDpAsState(
        if (acik) Olcu.SutunAcikYukseklik else Olcu.SutunKompaktYukseklik,
        gecis(Hareket.YayBuyumeMs, Hareket.YayBuyume),
        label = "sutunYuksekligi",
    )
    val olcek by animateFloatAsState(if (acik && tutuluyor) 1.03f else 1f, gecis(Hareket.BasmaMs, Hareket.Basma), label = "sutunOlcegi")
    val opaklik by animateFloatAsState(if (baskasiAcik) 0.6f else 1f, gecis(400, Hareket.Yumusak), label = "sutunOpakligi")
    val kenar by animateColorAsState(
        if (acik) Renk.Vurgu.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.06f),
        gecis(400, Hareket.Yumusak), label = "sutunKenari",
    )
    val zemin by animateColorAsState(
        if (acik) Renk.Vurgu.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f),
        gecis(400, Hareket.Yumusak), label = "sutunZemini",
    )
    val golge by animateDpAsState(if (acik) 22.dp else 0.dp, gecis(400, Hareket.Yumusak), label = "sutunGolgesi")

    // Canlı parıltı: yeni veri geldiğinde mum uçları soldan sağa sırayla kısa bir parıltı yapar.
    val parilti = remember { Animatable(0f) }
    LaunchedEffect(pariltiSurumu) {
        if (pariltiSurumu > 0 && !azalt) {
            delay(sira * Hareket.CanliPariltiSutunKademeMs.toLong())
            parilti.animateTo(1f, tween(320))
            parilti.animateTo(0f, tween(420))
        }
    }

    val partiOrani = oge.sonuc.partiDestek ?: 0f
    val liderOrani = oge.sonuc.liderDestek ?: 0f

    Column(
        modifier = modifier
            .height(yukseklik)
            .graphicsLayer {
                scaleX = olcek
                scaleY = olcek
                alpha = opaklik
            }
            .shadow(golge, sekil, clip = false, ambientColor = Renk.Vurgu, spotColor = Renk.Vurgu)
            .clip(sekil)
            .background(zemin)
            .border(1.dp, kenar, sekil)
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (acik) {
            BelirenIcerik { LiderAvatarlari(oge.parti.liderler) }
        } else {
            Text(
                text = yuzde(oge.sonuc.partiDestek, sembolik),
                style = Yazi.EnKucuk,
                color = Renk.MetinIkincil,
            )
        }
        // Kompakt hâlde mumlar alanın %30 fazlasına uzanır; açıkken doğrudan oran.
        MumCifti(
            parti = if (acik) partiOrani else partiOrani + 0.3f,
            lider = if (acik) liderOrani else liderOrani + 0.3f,
            genislik = if (acik) Olcu.MumGenisligiAcik else Olcu.MumGenisligiKompakt,
            fitil = if (acik) 8.dp else 3.dp,
            parilti = parilti.value,
            modifier = Modifier.weight(1f),
        )
        LogoKutusu(oge.parti, acik)
    }
}

/** Açılan içerik: yukarıdan hafifçe inerek ve büyüyerek belirir (tasarımdaki "fade"). */
@Composable
fun BelirenIcerik(icerik: @Composable () -> Unit) {
    val azalt = LocalHareketAzalt.current
    val ilerleme = remember { Animatable(if (azalt) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!azalt) {
            ilerleme.animateTo(
                1f,
                tween(Hareket.BilgiKartiMs, delayMillis = Hareket.BilgiKartiGecikmeMs, easing = Hareket.BilgiKarti),
            )
        }
    }
    val kayma = with(LocalDensity.current) { 6.dp.toPx() }
    Box(
        Modifier.graphicsLayer {
            alpha = ilerleme.value
            translationY = -kayma * (1f - ilerleme.value)
            val o = 0.92f + 0.08f * ilerleme.value
            scaleX = o
            scaleY = o
        },
    ) { icerik() }
}

/**
 * Bilgi kartı: açık sütunun yanında. İlk 4 partide sütunun sağında, son 4 partide solunda açılır.
 * Sütunlar arasında geçerken kayarak yer değiştirir.
 */
@Composable
private fun BilgiKartiYerlesimi(ogeler: List<SeritOgesi>, acik: Int?, sembolik: Boolean, adim: Dp, genislik: Dp) {
    if (acik == null || acik !in ogeler.indices) return
    val sagda = acik < ogeler.size / 2
    val hedefX = if (sagda) adim * (acik + 1) else genislik - adim * (ogeler.size - acik) - Olcu.BilgiKartiGenislik
    val x by animateDpAsState(hedefX, gecis(500, Hareket.YayBuyume), label = "bilgiKartiX")
    Box(
        Modifier
            .zIndex(2f)
            .offset(x = x, y = Olcu.BilgiKartiUst)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        BelirenIcerik { BilgiKarti(ogeler[acik], acik, sembolik) }
    }
}

@Composable
fun BilgiKarti(oge: SeritOgesi, sira: Int, sembolik: Boolean, modifier: Modifier = Modifier) {
    val sekil = RoundedCornerShape(Kose.BilgiKarti)
    Column(
        modifier = modifier
            .width(Olcu.BilgiKartiGenislik)
            .shadow(18.dp, sekil, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(sekil)
            .background(Renk.BilgiKarti)
            .border(1.dp, Renk.Vurgu.copy(alpha = 0.35f), sekil)
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp)
            .testTag("bilgi-karti"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(oge.parti.kisaAd, style = Yazi.KartBasligi, color = Renk.Metin, modifier = Modifier.weight(1f, fill = false))
            Text("${sira + 1}.", style = Yazi.EnKucukMono, color = Renk.MetinEnSoluk)
        }
        Text(oge.parti.liderAdlari, style = Yazi.Etiket12, color = Renk.MetinSoluk)
        Row(Modifier.padding(top = 6.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DegerKutusu("Parti", Renk.MumParti, yuzde(oge.sonuc.partiDestek, sembolik), Renk.Metin, Modifier.weight(1f))
            DegerKutusu("Lider", Renk.MumLider, yuzde(oge.sonuc.liderDestek, sembolik), Renk.VurguAcik, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DegerKutusu(etiket: String, mumRengi: Color, deger: String, degerRengi: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(width = 6.dp, height = 10.dp).clip(RoundedCornerShape(2.dp)).background(mumRengi))
            Text(etiket, style = Yazi.EnKucuk.copy(fontWeight = FontWeight.Normal), color = Renk.MetinEnSoluk)
        }
        Text(deger, style = Yazi.BuyukDeger, color = degerRengi)
    }
}
