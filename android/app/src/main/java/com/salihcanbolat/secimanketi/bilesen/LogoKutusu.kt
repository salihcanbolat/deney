package com.salihcanbolat.secimanketi.bilesen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.Parti

/**
 * Parti logosu kutusu (34×34). Logo varsa açık renkli kutuda, düzenlenmeden gösterilir.
 * Logo yoksa nötr bir glif; sütun açıkken kutu fildişine döner.
 * Tüm logolar aynı boy ve çerçevede (tarafsızlık kuralı).
 */
@Composable
fun LogoKutusu(parti: Parti, acik: Boolean, modifier: Modifier = Modifier, boyut: Dp = Olcu.LogoKutusu) {
    val sekil = RoundedCornerShape(Kose.LogoKutusu)
    val zemin by animateColorAsState(
        targetValue = when {
            parti.logo != null -> Renk.LogoKutusu
            acik -> Renk.Metin
            else -> Renk.Yuzey2
        },
        animationSpec = gecis(400, Hareket.Yumusak),
        label = "logoZemin",
    )
    val glifRengi by animateColorAsState(
        targetValue = if (acik) Renk.Zemin else Renk.Glif,
        animationSpec = gecis(400, Hareket.Yumusak),
        label = "glifRengi",
    )
    Box(
        modifier = modifier
            .size(boyut)
            .clip(sekil)
            .background(zemin)
            .border(1.dp, Color.White.copy(alpha = 0.1f), sekil),
        contentAlignment = Alignment.Center,
    ) {
        if (parti.logo != null) {
            Image(
                painter = painterResource(parti.logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(2.dp),
            )
        } else {
            Glif(parti.glif, glifRengi, Modifier.size(boyut * 15f / 34f))
        }
    }
}

/** 24×24 görünüm kutusundaki SVG yolunu çizer (kalınlık görünüm kutusu biriminde). */
@Composable
fun Glif(yol: String, renk: Color, modifier: Modifier = Modifier, kalinlik: Float = 2f) {
    val path = remember(yol) { PathParser().parsePathString(yol).toPath() }
    Canvas(modifier) {
        val olcek = size.minDimension / 24f
        scale(olcek, olcek, pivot = Offset.Zero) {
            drawPath(path, renk, style = Stroke(width = kalinlik, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
