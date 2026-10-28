package com.salihcanbolat.secimanketi.ekran

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.bilesen.Simge
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.basmaOlcegi
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis

enum class Sekme(val ad: String, val simge: String) {
    ANKETLER("Anketler", Simgeler.GRAFIK),
    KILIT("Kilit ekranı", Simgeler.TELEFON),
    CANLI("Canlı", Simgeler.SAAT),
}

/** Yüzen alt gezinme hapı: 64 dp, köşe 32, yarı saydam lacivert. */
@Composable
fun AltGezinme(secili: Sekme, onSec: (Sekme) -> Unit, modifier: Modifier = Modifier) {
    val sekil = RoundedCornerShape(32.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(20.dp, sekil, ambientColor = Color.Black.copy(alpha = 0.6f), spotColor = Color.Black.copy(alpha = 0.6f))
            .clip(sekil)
            // rgba(20,28,58,0.72); bulanıklaştırma Android 12 altında yok, bu yüzden yalnızca saydamlık.
            .background(Color(0xB8141C3A))
            .border(1.dp, Color.White.copy(alpha = 0.1f), sekil)
            .padding(6.dp)
            .selectableGroup()
            .testTag("alt-gezinme"),
    ) {
        Sekme.entries.forEach { sekme ->
            SekmeOgesi(sekme, sekme == secili, { onSec(sekme) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SekmeOgesi(sekme: Sekme, secili: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val sekil = RoundedCornerShape(26.dp)
    val kaynak = remember { MutableInteractionSource() }
    val spec = gecis<Color>(Hareket.BasmaMs, Hareket.Yumusak)
    val zemin by animateColorAsState(if (secili) Color.White.copy(alpha = 0.1f) else Color.Transparent, spec, label = "sekmeZemin")
    val renk by animateColorAsState(if (secili) Renk.Metin else Renk.MetinEnSoluk, spec, label = "sekmeRenk")
    Row(
        modifier
            .basmaOlcegi(kaynak)
            .fillMaxHeight()
            .clip(sekil)
            .background(zemin)
            .selectable(secili, kaynak, null, role = Role.Tab, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Simge(sekme.simge, renk, 18.dp)
        Text(
            sekme.ad,
            style = Yazi.Kucuk.copy(fontWeight = if (secili) FontWeight.SemiBold else FontWeight.Medium),
            color = renk,
            maxLines = 1,
        )
    }
}
