package com.salihcanbolat.secimanketi.bilesen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis

/**
 * "Bu anketi kilit ekranında takip et" düğmesi. Kısa hâli "Takip et" / "Takipte"; simgesi kilit
 * ekranındaki şeridiyle küçük bir telefon (asma kilit değil: kilit "kapalı" ya da "güvenli" gibi
 * okunuyordu). Takibe alınınca telefonun şeridi dolar, köşesinde onay belirir.
 *
 * Aynı anda yalnızca bir anket kilit ekranında olabilir; takipteki ankete yeniden dokunmak bir şey
 * değiştirmez (başka bir anketi takibe almak öncekini bırakır).
 *
 * @param uzun Ayrıntı ekranındaki geniş düğme için tam metin.
 */
@Composable
fun KilitTakipDugmesi(
    takipte: Boolean,
    onTakipEt: () -> Unit,
    modifier: Modifier = Modifier,
    uzun: Boolean = false,
    etiket: String? = null,
) {
    val sekil = RoundedCornerShape(Kose.Dugme)
    val kaynak = remember { MutableInteractionSource() }
    val dokunus = LocalHapticFeedback.current
    val azalt = LocalHareketAzalt.current
    val spec = gecis<Color>(Hareket.BasmaMs, Hareket.Yumusak)
    val zemin by animateColorAsState(if (takipte) Renk.Vurgu.copy(alpha = 0.14f) else Color.Transparent, spec, label = "takipZemin")
    val kenar by animateColorAsState(if (takipte) Renk.Vurgu.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f), spec, label = "takipKenar")
    val renk by animateColorAsState(if (takipte) Renk.VurguAcik else Renk.MetinIkincil, spec, label = "takipRenk")
    Row(
        modifier
            .basmaOlcegi(kaynak)
            .heightIn(min = Olcu.MinDokunmaAlani)
            .background(zemin, sekil)
            .border(1.dp, kenar, sekil)
            .toggleable(takipte, kaynak, null, role = Role.Switch) {
                if (!takipte) {
                    dokunus.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTakipEt()
                }
            }
            .semantics {
                contentDescription = "Bu anketi kilit ekranında takip et"
                stateDescription = if (takipte) "Kilit ekranında takip ediliyor" else "Takip edilmiyor"
            }
            .then(if (etiket != null) Modifier.testTag(etiket) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KilitEkraniSimgesi(takipte, renk, 18.dp)
        AnimatedContent(
            targetState = takipte,
            transitionSpec = {
                if (azalt) {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                } else {
                    // Yeni metin aşağıdan, eskisi yukarı: durum değişti hissi.
                    (slideInVertically(tween(Hareket.BasmaMs, easing = Hareket.Basma)) { it / 2 } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(tween(200)) { -it / 2 } + fadeOut(tween(150)))
                }
            },
            label = "takipMetni",
        ) { acik ->
            Text(
                when {
                    uzun && acik -> "Kilit ekranında takip ediliyor"
                    uzun -> "Kilit ekranında takip et"
                    acik -> "Takipte"
                    else -> "Takip et"
                },
                style = Yazi.Govde.copy(fontSize = 14.sp, fontWeight = if (acik) FontWeight.SemiBold else FontWeight.Medium),
                color = renk,
                maxLines = 1,
            )
        }
    }
}

/**
 * Kilit ekranı simgesi: telefon, üstte saat çizgisi ve ortada şerit. [dolu] iken şerit dolar ve
 * sağ altta küçük bir onay rozeti belirir (yayla).
 */
@Composable
fun KilitEkraniSimgesi(dolu: Boolean, renk: Color, boyut: Dp, modifier: Modifier = Modifier) {
    val doluluk by animateFloatAsState(if (dolu) 1f else 0f, gecis(Hareket.BasmaMs, Hareket.Basma), label = "simgeDoluluk")
    Canvas(modifier.size(boyut)) {
        val o = size.minDimension / 24f
        scale(o, o, pivot = Offset.Zero) {
            val cizgi = Stroke(width = 1.7f, cap = StrokeCap.Round)
            // Telefon gövdesi
            drawRoundRect(renk, topLeft = Offset(6f, 2.5f), size = Size(12f, 19f), cornerRadius = CornerRadius(3f), style = cizgi)
            // Saat
            drawLine(renk, Offset(10.5f, 5.6f), Offset(13.5f, 5.6f), strokeWidth = 1.7f, cap = StrokeCap.Round)
            // Şerit: boşken çerçeve, takipteyken dolu
            drawRoundRect(renk, topLeft = Offset(8.6f, 9f), size = Size(6.8f, 4.4f), cornerRadius = CornerRadius(1.2f), style = Stroke(1.4f))
            if (doluluk > 0f) {
                drawRoundRect(renk.copy(alpha = renk.alpha * doluluk), topLeft = Offset(8.6f, 9f), size = Size(6.8f, 4.4f), cornerRadius = CornerRadius(1.2f))
            }
            // Onay rozeti
            if (doluluk > 0f) {
                val r = 4.6f * doluluk
                val merkez = Offset(18.2f, 18.6f)
                drawCircle(Renk.Vurgu, radius = r + 1.2f * doluluk, center = merkez)
                if (doluluk > 0.6f) {
                    val a = ((doluluk - 0.6f) / 0.4f).coerceIn(0f, 1f)
                    val p1 = Offset(merkez.x - 2.2f, merkez.y + 0.1f)
                    val p2 = Offset(merkez.x - 0.6f, merkez.y + 1.7f)
                    val p3 = Offset(merkez.x + 2.4f, merkez.y - 1.5f)
                    drawLine(Renk.VurguUstuMetin.copy(alpha = a), p1, p2, strokeWidth = 1.6f, cap = StrokeCap.Round)
                    drawLine(Renk.VurguUstuMetin.copy(alpha = a), p2, p3, strokeWidth = 1.6f, cap = StrokeCap.Round)
                }
            }
        }
    }
}
