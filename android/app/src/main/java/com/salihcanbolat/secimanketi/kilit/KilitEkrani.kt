package com.salihcanbolat.secimanketi.kilit

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.bilesen.Glif
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Kilit ekranı zemini: radial-gradient(120% 80% at 20% 10%, #16224D, #0A1030 %55, #040714).
 * Ekran 4 ve 5'te birebir aynı çizilir; geçişte hiçbir öğe yerinden oynamaz.
 */
fun Modifier.kilitZemini(): Modifier = drawBehind {
    val merkez = Offset(size.width * 0.2f, size.height * 0.1f)
    val yatay = size.width * 1.2f
    val dikey = size.height * 0.8f
    val (r0, r1, r2) = Renk.ZeminKilitGradyan
    scale(scaleX = 1f, scaleY = dikey / yatay, pivot = merkez) {
        drawRect(
            brush = Brush.radialGradient(0f to r0, 0.55f to r1, 1f to r2, center = merkez, radius = yatay),
            topLeft = Offset(-size.width * 4, -size.height * 4),
            size = androidx.compose.ui.geometry.Size(size.width * 9, size.height * 9),
        )
    }
}

/** Duvar kâğıdında yavaşça süzülen mavi ışık (16 sn). */
@Composable
fun BoxScope.SuzulenIsik() {
    val azalt = LocalHareketAzalt.current
    val gecis = rememberInfiniteTransition(label = "suzulme")
    val t by gecis.animateFloat(
        initialValue = 0f,
        targetValue = if (azalt) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "t",
    )
    val yogunluk = LocalDensity.current
    Box(
        Modifier
            .offset(x = 50.dp, y = 340.dp)
            .graphicsLayer {
                with(yogunluk) {
                    translationX = 24.dp.toPx() * t
                    translationY = 30.dp.toPx() * t
                }
            }
            .size(300.dp)
            .background(Brush.radialGradient(listOf(Renk.Vurgu.copy(alpha = 0.12f), Renk.Vurgu.copy(alpha = 0f)))),
    )
}

private val Turkce = Locale.forLanguageTag("tr")
private val SaatBicimi = DateTimeFormatter.ofPattern("HH:mm", Turkce)
private val TarihBicimi = DateTimeFormatter.ofPattern("EEEE, d MMMM", Turkce)

/**
 * Saat (96 sp / 300) ve tarih. Gerçek saati gösterir; ekran görüntüleri için sabitlenebilir.
 */
@Composable
fun KilitSaati(modifier: Modifier = Modifier, sabitZaman: LocalDateTime? = null) {
    var simdi by remember { mutableStateOf(sabitZaman ?: LocalDateTime.now()) }
    LaunchedEffect(sabitZaman) {
        if (sabitZaman != null) return@LaunchedEffect
        while (true) {
            val su = LocalDateTime.now()
            simdi = su
            delay((60 - su.second) * 1000L)
        }
    }
    Column(modifier) {
        Text(SaatBicimi.format(simdi), style = Yazi.Saat, color = Renk.Metin, modifier = Modifier.cssSatirYuksekligi(0.95f))
        Text(
            TarihBicimi.format(simdi),
            style = Yazi.Tarih,
            color = Renk.MetinIkincil,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

/**
 * CSS'deki gibi satır yüksekliği: kutu `oran × yazı boyutu` yüksekliğindedir ve yazı dikeyde ortalanır
 * (Compose, yazı tipinin kendi yüksekliğinden küçük satır yüksekliğinde kutuyu küçültmez).
 * Saat: 96 sp × 0,95; rakamlar tasarımdaki konuma oturur.
 */
private fun Modifier.cssSatirYuksekligi(oran: Float): Modifier = layout { olculebilir, kisitlar ->
    val yerlesik = olculebilir.measure(kisitlar)
    val hedef = (Yazi.Saat.fontSize.toPx() * oran).roundToInt()
    layout(yerlesik.width, hedef) {
        yerlesik.place(0, (hedef - yerlesik.height) / 2)
    }
}

/**
 * Kilit ekranının alt kısmı: parmak izi ve kısayollar (tasarımdaki gibi; dokunulmaz, ekran okuyucuda gizli).
 */
@Composable
fun BoxScope.KilitAltKismi() {
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 130.dp)
            .size(56.dp)
            .clip(CircleShape)
            .border(1.5.dp, Renk.Metin.copy(alpha = 0.35f), CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Glif(PARMAK_IZI, Renk.Metin.copy(alpha = 0.8f), Modifier.size(26.dp), kalinlik = 1.5f)
    }
    Row(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, bottom = 40.dp)
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Kisayol(EL_FENERI)
        Kisayol(KAMERA)
    }
}

@Composable
private fun Kisayol(yol: String) {
    Box(
        Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Glif(yol, Renk.Metin, Modifier.size(20.dp), kalinlik = 1.7f)
    }
}

private const val PARMAK_IZI = "M12 4a7 7 0 0 0-7 7v2M19 13v-2a7 7 0 0 0-3-5.7M12 8a3 3 0 0 0-3 3v3a8 8 0 0 0 1 4M15 11v3a10 10 0 0 1-1 4.5M12 12v3"
private const val EL_FENERI = "M8 3h8l-1 6H9zM9 9l1 12h4l1-12"
private const val KAMERA = "M4 8h3l2-3h6l2 3h3v11H4zM8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0"
