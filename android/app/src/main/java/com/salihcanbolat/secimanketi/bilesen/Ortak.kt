package com.salihcanbolat.secimanketi.bilesen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.gecis
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Ekran HTML'lerindeki çizgi simgeleri (24×24 görünüm kutusu; daire ve dikdörtgenler yola çevrildi). */
object Simgeler {
    const val GRAFIK = "M5 20V10M12 20V4M19 20v-7"
    const val TELEFON = "M9 2.5h6a3 3 0 0 1 3 3v13a3 3 0 0 1 -3 3h-6a3 3 0 0 1 -3 -3v-13a3 3 0 0 1 3 -3zM10 6h4"
    const val TELEFON_SERIT = "M9 2.5h6a3 3 0 0 1 3 3v13a3 3 0 0 1 -3 3h-6a3 3 0 0 1 -3 -3v-13a3 3 0 0 1 3 -3zM9.5 15h5M9.5 12h5"
    const val SAAT = "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0M12 7.5V12l3 2"
    const val HESAP = "M8 8a4 4 0 1 0 8 0a4 4 0 1 0 -8 0M4 20c1.5-3.6 4.5-5.5 8-5.5s6.5 1.9 8 5.5"
    const val KILIT = "M7.5 11h9a2.5 2.5 0 0 1 2.5 2.5v5a2.5 2.5 0 0 1 -2.5 2.5h-9a2.5 2.5 0 0 1 -2.5 -2.5v-5a2.5 2.5 0 0 1 2.5 -2.5zM8 11V8a4 4 0 0 1 8 0v3"
    const val ZIL = "M6 16V11a6 6 0 0 1 12 0v5l1.5 2h-15zM10 20.5a2 2 0 0 0 4 0"
    const val ZIL_KAPALI = "M6 16V11a6 6 0 0 1 12 0v5l1.5 2h-15zM10 20.5a2 2 0 0 0 4 0M4 4l16 16"
    const val GERI = "M15 5l-7 7 7 7"
    const val ONAY = "M5 12.5l4.5 4.5L19 7.5"
    const val KALKAN = "M12 3l7 3v5c0 4.5-3 8-7 10-4-2-7-5.5-7-10V6z"
    const val GONDER = "M8.5 9a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0M5.5 19c1.3-3 3.8-4.5 6.5-4.5s5.2 1.5 6.5 4.5M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0"
}

/** Çizgi simgesi; [kalinlik] görünüm kutusu biriminde (tasarımdaki stroke-width). */
@Composable
fun Simge(yol: String, renk: Color, boyut: Dp, modifier: Modifier = Modifier, kalinlik: Float = 1.8f) {
    Glif(yol, renk, modifier.size(boyut), kalinlik)
}

/** Onay işareti; [ilerleme] 0→1 iken çizilerek belirir (stroke-dashoffset karşılığı). */
@Composable
fun CizilenOnay(renk: Color, kalinlik: Float, ilerleme: () -> Float, modifier: Modifier = Modifier) {
    val yol = remember { PathParser().parsePathString(Simgeler.ONAY).toPath() }
    val olcum = remember { PathMeasure().apply { setPath(yol, false) } }
    val parca = remember { Path() }
    Canvas(modifier) {
        val o = size.minDimension / 24f
        parca.reset()
        olcum.getSegment(0f, olcum.length * ilerleme().coerceIn(0f, 1f), parca, true)
        scale(o, o, pivot = Offset.Zero) {
            drawPath(parca, renk, style = Stroke(width = kalinlik, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Seçimde beliren yuvarlak onay (tasarımda "pop": 0,4 → 1,15 → 1, 450 ms). */
@Composable
fun SecimIsareti(secili: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.size(26.dp), contentAlignment = Alignment.Center) {
        if (secili) {
            Box(
                Modifier
                    .size(26.dp)
                    .pop()
                    .clip(CircleShape)
                    .background(Renk.Vurgu),
                contentAlignment = Alignment.Center,
            ) {
                Simge(Simgeler.ONAY, Renk.VurguUstuMetin, 14.dp, kalinlik = 3f)
            }
        } else {
            Box(Modifier.size(24.dp).border(1.5.dp, Color.White.copy(alpha = 0.22f), CircleShape))
        }
    }
}

/** İlk göründüğünde 0,4 → 1,15 → 1 ölçekle "pop" yapar (450 ms, basma eğrisi). */
fun Modifier.pop(): Modifier = composed {
    val azalt = LocalHareketAzalt.current
    val olcek = remember { Animatable(if (azalt) 1f else 0.4f) }
    LaunchedEffect(Unit) {
        if (!azalt) {
            olcek.animateTo(1f, keyframes {
                durationMillis = 450
                0.4f at 0 using Hareket.Basma
                1.15f at 270 using Hareket.Basma
            })
        }
    }
    graphicsLayer {
        scaleX = olcek.value
        scaleY = olcek.value
        alpha = ((olcek.value - 0.4f) / 0.6f).coerceIn(0f, 1f)
    }
}

/**
 * Basınca hafifçe küçülür (tasarımdaki `.press:active`), bırakınca yayla geri döner.
 * Dalgalanma (ripple) yok; geri bildirim ölçek ile verilir.
 */
fun Modifier.basmaOlcegi(kaynak: MutableInteractionSource, olcek: Float = Hareket.BasmaOlcek): Modifier = composed {
    val basili by kaynak.collectIsPressedAsState()
    val deger by animateFloatAsState(
        if (basili) olcek else 1f,
        gecis(Hareket.BasmaMs, Hareket.Basma),
        label = "basma",
    )
    graphicsLayer {
        scaleX = deger
        scaleY = deger
    }
}

/**
 * Ekrana girişte aşağıdan yükselerek belirir (tasarımdaki `.rise`): 800 ms, 16 dp, kademeli gecikme.
 * "Animasyonları kaldır" açıkken doğrudan görünür.
 */
fun Modifier.belirme(gecikmeMs: Int = 0, kayma: Dp = Hareket.GirisKaymaDp.dp, olcek: Float = 1f, sureMs: Int = Hareket.GirisMs): Modifier = composed {
    belirme(rememberGiris(gecikmeMs, sureMs), kayma, olcek)
}

/**
 * Ekran düzeyinde giriş ilerlemesi (0→1). Kaydırılan listelerde öğeler yeniden göründüğünde
 * animasyon tekrar oynamasın diye ekranda bir kez oluşturulup öğelere verilir.
 */
@Composable
fun rememberGiris(gecikmeMs: Int, sureMs: Int = Hareket.GirisMs): State<Float> {
    val azalt = LocalHareketAzalt.current
    val ilerleme = remember { Animatable(if (azalt) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!azalt) ilerleme.animateTo(1f, tween(sureMs, gecikmeMs, Hareket.Giris))
    }
    return ilerleme.asState()
}

fun Modifier.belirme(ilerleme: State<Float>, kayma: Dp = Hareket.GirisKaymaDp.dp, olcek: Float = 1f): Modifier = graphicsLayer {
    val p = ilerleme.value
    alpha = p.coerceIn(0f, 1f)
    translationY = (1f - p) * kayma.toPx()
    val o = olcek + (1f - olcek) * p
    scaleX = o
    scaleY = o
}

/**
 * CSS `linear-gradient(<derece>deg, …)` karşılığı: 0° yukarı, 90° sağa; gradyan çizgisi kutunun
 * köşelerine değecek uzunluktadır.
 */
fun Modifier.aciliGradyan(derece: Float, renkler: List<Color>, sekil: Shape): Modifier = drawBehind {
    val a = Math.toRadians(derece.toDouble())
    val yon = Offset(sin(a).toFloat(), -cos(a).toFloat())
    val uzunluk = abs(size.width * yon.x) + abs(size.height * yon.y)
    val merkez = center
    val firca = Brush.linearGradient(renkler, start = merkez - yon * (uzunluk / 2), end = merkez + yon * (uzunluk / 2))
    drawOutline(sekil.createOutline(size, layoutDirection, this), firca)
}

/** repeating-linear-gradient(135deg, [acik] 0 3px, [koyu] 3px 6px): mumların çizgili dokusu. */
fun DrawScope.cizgiliDolgu(acik: Color, koyu: Color) {
    drawRect(koyu)
    val serit = 3.dp.toPx()
    val adim = 6.dp.toPx() * sqrt(2f)
    var x = -size.height
    while (x < size.width + size.height) {
        drawLine(acik, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = serit)
        x += adim
    }
}

/** Kesik çizgili kenar (pasif gönder düğmesi). */
fun Modifier.kesikKenar(renk: Color, kose: Dp, kalinlik: Dp = 1.dp): Modifier = drawWithContent {
    drawContent()
    val k = kalinlik.toPx()
    drawRoundRect(
        color = renk,
        topLeft = Offset(k / 2, k / 2),
        size = size.copy(width = size.width - k, height = size.height - k),
        cornerRadius = CornerRadius(kose.toPx()),
        style = Stroke(width = k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
    )
}

/**
 * Tasarımdaki üst boşluk (durum çubuğu dahil ölçülmüş). Durum çubuğu daha uzunsa altında kalınır.
 */
@Composable
fun ustBosluk(tasarim: Dp): Dp =
    max(tasarim, WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)

/** Tasarımdaki alt boşluk. Üç düğmeli gezinme çubuğu gibi daha yüksek bir sistem çubuğu varsa üstünde kalınır. */
@Composable
fun altBosluk(tasarim: Dp): Dp =
    max(tasarim, WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 10.dp)
