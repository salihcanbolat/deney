package com.salihcanbolat.secimanketi.bilesen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.SeritOgesi
import kotlinx.coroutines.delay

/**
 * Kilit ekranındaki cam kart: başlık, mum şeridi ve açıklama satırı.
 * Ekran 5'teki bildirimle (etkileşimsiz) ve Ekran 4'teki incelemeyle (etkileşimli) aynı ölçüdedir.
 *
 * @param ipucu Sağ alttaki ipucu ("Dokun, incele →" ya da "Basılı tut, kaydır").
 * @param uyanis Açılışta şeridin çevresinde kısa bir mavi parıltı (Ekran 4).
 */
@Composable
fun SeritKarti(
    anket: Anket,
    ogeler: List<SeritOgesi>,
    sembolik: Boolean,
    durum: SeritDurumu,
    ipucu: String,
    modifier: Modifier = Modifier,
    ipucuVurgulu: Boolean = false,
    etkilesimli: Boolean = true,
    uyanis: Boolean = false,
    pariltiSurumu: Int = 0,
) {
    val sekil = RoundedCornerShape(Kose.Kart)
    val azalt = LocalHareketAzalt.current
    val kenar by animateColorAsState(
        if (durum.acik != null) Renk.Vurgu.copy(alpha = 0.35f) else Renk.Cizgi,
        gecis(400, Hareket.Yumusak), label = "kartKenari",
    )
    // Uyanış: 0 → %40'ta 3 dp mavi halka → 0, 1,2 sn, 0,15 sn gecikmeyle.
    val halka = remember { Animatable(0f) }
    LaunchedEffect(uyanis) {
        if (uyanis && !azalt) {
            delay(150)
            halka.animateTo(1f, keyframes {
                durationMillis = 1200
                1f at 480 using LinearOutSlowInEasing
                0f at 1200 using FastOutSlowInEasing
            })
            halka.snapTo(0f)
        }
    }
    Column(
        modifier = modifier
            .drawBehind {
                val a = halka.value
                if (a > 0f) {
                    val k = 3.dp.toPx()
                    drawRoundRect(
                        color = Renk.Vurgu.copy(alpha = 0.45f * a),
                        topLeft = Offset(-k / 2, -k / 2),
                        size = Size(size.width + k, size.height + k),
                        cornerRadius = CornerRadius(Kose.Kart.toPx() + k / 2),
                        style = Stroke(width = k),
                    )
                }
            }
            .clip(sekil)
            .background(Renk.CamKart)
            .border(1.dp, kenar, sekil)
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(Renk.Vurgu),
                contentAlignment = Alignment.Center,
            ) {
                Glif(GRAFIK_GLIFI, Renk.VurguUstuMetin, Modifier.size(12.dp), kalinlik = 3f)
            }
            Text("Canlı Anket · ${anket.ad}", style = Yazi.Etiket12, color = Renk.BaslikMetni, modifier = Modifier.weight(1f), maxLines = 1)
            CanliEtiketi()
        }
        Spacer(Modifier.size(10.dp))
        MumSeridi(ogeler, sembolik, durum, etkilesimli = etkilesimli, pariltiSurumu = pariltiSurumu)
        Spacer(Modifier.size(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Lejant("Parti", Renk.MumParti)
                Lejant("Lider", Renk.MumLider)
                if (sembolik) Text("Sembolik · eşit", style = AciklamaYazisi, color = Renk.MetinEnSoluk)
            }
            Text(ipucu, style = AciklamaYazisi, color = if (ipucuVurgulu) Renk.VurguAcik else Renk.MetinEnSoluk)
        }
    }
}

private val AciklamaYazisi = Yazi.EnKucuk.copy(fontWeight = FontWeight.Normal)

/** Üç çubuklu grafik simgesi (bildirim başlığı). */
private const val GRAFIK_GLIFI = "M5 20V10M12 20V4M19 20v-7"

@Composable
private fun Lejant(ad: String, renk: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(width = 6.dp, height = 9.dp).clip(RoundedCornerShape(2.dp)).background(renk))
        Text(ad, style = AciklamaYazisi, color = Renk.MetinEnSoluk)
    }
}

/** "● CANLI": nokta 2,4 sn'de bir yavaşça solup büyür. */
@Composable
fun CanliEtiketi() {
    val azalt = LocalHareketAzalt.current
    val gecisi = rememberInfiniteTransition(label = "canli")
    val nabiz by gecisi.animateFloat(
        initialValue = 1f,
        targetValue = if (azalt) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nabiz",
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            Modifier
                .size(5.dp)
                .graphicsLayer {
                    alpha = 0.3f + 0.7f * nabiz
                    val o = 0.6f + 0.4f * nabiz
                    scaleX = o
                    scaleY = o
                }
                .clip(CircleShape)
                .background(Renk.VurguAcik),
        )
        Text("CANLI", style = Yazi.EnKucukMono, color = Renk.VurguAcik)
    }
}
