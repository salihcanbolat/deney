package com.salihcanbolat.secimanketi.bilesen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.veri.Lider

/** Lider fotoğrafı (32 dp daire). Eş genel başkanlarda iki fotoğraf üst üste biner. */
@Composable
fun LiderAvatarlari(liderler: List<Lider>, modifier: Modifier = Modifier, boyut: Dp = Olcu.LiderAvatari) {
    val ikiLider = liderler.size > 1
    // İkinci fotoğraf birincinin altına çeyrek boy biner (tasarımda margin-top: -8px).
    Box(modifier.height(if (ikiLider) boyut * 1.75f else boyut), contentAlignment = Alignment.TopCenter) {
        liderler.getOrNull(0)?.let { LiderAvatari(it, boyut) }
        liderler.getOrNull(1)?.let { LiderAvatari(it, boyut, ikinci = true, modifier = Modifier.offset(y = boyut * 0.75f)) }
    }
}

@Composable
fun LiderAvatari(lider: Lider, boyut: Dp = Olcu.LiderAvatari, ikinci: Boolean = false, modifier: Modifier = Modifier) {
    val zemin = if (ikinci) {
        Brush.linearGradient(listOf(Color(0xFF36457A), Color(0xFF1A2347)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF2C3A6B), Color(0xFF151D3D)))
    }
    Box(
        modifier = modifier
            .size(boyut)
            .clip(CircleShape)
            .background(zemin)
            .border(1.5.dp, Renk.Vurgu.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (lider.foto != null) {
            Image(
                painter = painterResource(lider.foto),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(boyut),
            )
        } else {
            Text(lider.basHarfler, style = Yazi.EnKucuk.copy(fontWeight = FontWeight.SemiBold), color = Renk.Metin)
        }
    }
}
