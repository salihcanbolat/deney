package com.salihcanbolat.secimanketi.ekran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.bilesen.SeritKarti
import com.salihcanbolat.secimanketi.bilesen.belirme
import com.salihcanbolat.secimanketi.bilesen.rememberSeritDurumu
import com.salihcanbolat.secimanketi.bilesen.ustBosluk
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * "Canlı" sekmesi. Tasarım paketinde bu sekmenin ekranı yok; şimdilik kilit ekranındaki anketin
 * şeridini uygulama içinde, aynı kartla ve aynı basılı tut + kaydır etkileşimiyle gösterir.
 */
@Composable
fun CanliEkrani(
    anket: Anket,
    modifier: Modifier = Modifier,
    bugun: LocalDate = LocalDate.now(),
    altBoslukEk: Dp = 0.dp,
) {
    val turkce = Locale.forLanguageTag("tr")
    val tarih = DateTimeFormatter.ofPattern("d MMMM yyyy", turkce).format(bugun).uppercase(turkce)
    Box(modifier.fillMaxSize().background(Renk.Zemin)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = ustBosluk(64.dp), bottom = 120.dp + altBoslukEk),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(
                Modifier.padding(horizontal = Olcu.EkranKenarBoslugu).belirme(0, olcek = 0.985f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CanliNokta()
                    Text("CANLI · $tarih", style = UstEtiket, color = Renk.MetinEnSoluk)
                }
                Text("Canlı", style = Yazi.Baslik1.copy(lineHeight = 1.05.em), color = Renk.Metin, modifier = Modifier.semantics { heading() })
                Text(anket.ad, style = Yazi.Govde.copy(fontSize = 14.sp), color = Renk.MetinSoluk)
            }
            SeritKarti(
                anket = anket,
                ogeler = SembolikVeri.serit(anket),
                sembolik = true,
                durum = rememberSeritDurumu(),
                ipucu = "Basılı tut, kaydır",
                ipucuVurgulu = true,
                modifier = Modifier
                    .padding(horizontal = Olcu.KilitSeridiKartYanBosluk)
                    .fillMaxWidth()
                    .belirme(60, olcek = 0.985f),
            )
        }
    }
}
