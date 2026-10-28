package com.salihcanbolat.secimanketi.bilesen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.gecis
import kotlin.math.sqrt

/**
 * Yapışık iki mum: solda parti oyu (fildişi), sağda lider oyu (mavi). Aralarında boşluk yok;
 * dış köşeler yuvarlak, birleşen kenar düz. Her mumun üstünde ince bir fitil var.
 *
 * @param parti Parti mumunun yüksekliği, alanın oranı olarak (0–1).
 * @param lider Lider mumunun yüksekliği, alanın oranı olarak (0–1).
 * @param parilti 0–1: canlı veri geldiğinde mum uçlarının kısa parıltısı.
 */
@Composable
fun MumCifti(
    parti: Float,
    lider: Float,
    genislik: Dp,
    fitil: Dp,
    modifier: Modifier = Modifier,
    parilti: Float = 0f,
) {
    val partiOran by animateFloatAsState(parti, gecis(700, Hareket.YayBuyume), label = "partiMumu")
    val liderOran by animateFloatAsState(lider, gecis(700, Hareket.YayBuyume, gecikmeMs = 60), label = "liderMumu")
    val gen by animateDpAsState(genislik, gecis(500, Hareket.Yumusak), label = "mumGenisligi")
    val fitilBoyu by animateDpAsState(fitil, gecis(Hareket.YayBuyumeMs, Hareket.YayBuyume), label = "fitil")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        Mum(partiOran, gen, fitilBoyu, Renk.MumParti, fitilAlfa = 0.7f, golgeAlfa = 0.6f, sol = true, parilti = parilti)
        Mum(liderOran, gen, fitilBoyu, Renk.MumLider, fitilAlfa = 0.9f, golgeAlfa = 0.5f, sol = false, parilti = parilti)
    }
}

@Composable
private fun Mum(
    oran: Float,
    genislik: Dp,
    fitil: Dp,
    renk: Color,
    fitilAlfa: Float,
    golgeAlfa: Float,
    sol: Boolean,
    parilti: Float,
) {
    // Parti mumu: üst-sol 4, alt-sol 2; lider mumu: üst-sağ 4, alt-sağ 2. Birleşen kenar düz.
    val sekil = if (sol) {
        RoundedCornerShape(topStart = 4.dp, topEnd = 0.dp, bottomEnd = 0.dp, bottomStart = 2.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 4.dp, bottomEnd = 2.dp, bottomStart = 0.dp)
    }
    val parlak = renk.copy(alpha = 0.95f)
    Column(
        modifier = Modifier
            .width(genislik)
            .fillMaxHeight(oran.coerceIn(0f, 1f)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .width(1.5.dp)
                .height(fitil)
                .background(lerpParlak(renk.copy(alpha = fitilAlfa), parilti)),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(sekil)
                .drawBehind {
                    // repeating-linear-gradient(135deg, renk .95 0 3px, renk .6 3px 6px)
                    drawRect(lerpParlak(renk.copy(alpha = golgeAlfa), parilti))
                    val serit = 3.dp.toPx()
                    val adim = 6.dp.toPx() * sqrt(2f)
                    var x = -size.height
                    while (x < size.width + size.height) {
                        drawLine(
                            color = lerpParlak(parlak, parilti),
                            start = Offset(x, size.height),
                            end = Offset(x + size.height, 0f),
                            strokeWidth = serit,
                        )
                        x += adim
                    }
                },
        )
    }
}

/** Parıltı sırasında rengi beyaza doğru açar (CSS brightness(1.7) karşılığı). */
private fun lerpParlak(renk: Color, parilti: Float): Color {
    if (parilti <= 0f) return renk
    val t = (parilti * 0.5f).coerceIn(0f, 1f)
    return Color(
        red = renk.red + (1f - renk.red) * t,
        green = renk.green + (1f - renk.green) * t,
        blue = renk.blue + (1f - renk.blue) * t,
        alpha = renk.alpha,
    )
}
