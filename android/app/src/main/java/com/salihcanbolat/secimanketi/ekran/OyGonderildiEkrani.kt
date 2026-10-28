package com.salihcanbolat.secimanketi.ekran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.bilesen.CizilenOnay
import com.salihcanbolat.secimanketi.bilesen.Simge
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.altBosluk
import com.salihcanbolat.secimanketi.bilesen.basmaOlcegi
import com.salihcanbolat.secimanketi.bilesen.belirme
import com.salihcanbolat.secimanketi.bilesen.ustBosluk
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.Oy
import com.salihcanbolat.secimanketi.veri.Partiler

private const val HALKA_DONGU_MS = 3000L
private val HalkaEgrisi = CubicBezierEasing(0.2f, 0.7f, 0.3f, 1f)
private val CizimEgrisi = CubicBezierEasing(0.6f, 0f, 0.2f, 1f)

/**
 * Ekran 3 · Oy gönderildi. Yayılan halkalar, çizilerek beliren onay, seçimlerin özeti (yalnızca bu
 * cihazda), "Kilit ekranında takip et" anahtarı ve kilit ekranı görünümüne geçiş.
 *
 * @param halkaZamani Ekran görüntüsü testleri için halkaların sabit zamanı (ms); null ise canlı.
 */
@Composable
fun OyGonderildiEkrani(
    anket: Anket,
    oy: Oy,
    kilitteTakip: Boolean,
    onTakipDegistir: (Boolean) -> Unit,
    onKilitGorunumu: () -> Unit,
    onAnketlereDon: () -> Unit,
    modifier: Modifier = Modifier,
    halkaZamani: Long? = null,
) {
    BoxWithConstraints(modifier.fillMaxSize().background(Renk.Zemin)) {
        val ekranYuksekligi = maxHeight
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                Modifier
                    .heightIn(min = ekranYuksekligi)
                    .padding(start = 20.dp, end = 20.dp, top = ustBosluk(72.dp), bottom = altBosluk(34.dp)),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                // Kısa ekranlarda (tasarım 844 dp) alt düğmeler görünür kalsın diye halka alanı küçülür.
                OnayAnimasyonu(halkaZamani, (ekranYuksekligi - 690.dp).coerceIn(150.dp, 200.dp))
                Column(
                    Modifier.fillMaxWidth().belirme(500, kayma = 18.dp, sureMs = 900),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Oyun sayıldı",
                        style = Yazi.Baslik1.copy(fontSize = 30.sp),
                        color = Renk.Metin,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        "${anket.ad} · anonim olarak kaydedildi.\nBu ankette tekrar oy veremezsin.",
                        style = Yazi.Govde.copy(lineHeight = 1.45.em, textAlign = TextAlign.Center),
                        color = Renk.MetinSoluk,
                    )
                }
                Column(Modifier.belirme(620, kayma = 18.dp, sureMs = 900)) {
                    Ozet(anket, oy)
                    Row(
                        Modifier.padding(start = 4.dp, end = 4.dp, top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Simge(Simgeler.KALKAN, Renk.MetinEnSoluk, 14.dp, Modifier.padding(top = 1.dp))
                        Text(
                            "Bu özet yalnızca bu cihazda saklanır. Kilit ekranında senin tercihin hiçbir zaman gösterilmez.",
                            style = Yazi.Etiket12.copy(lineHeight = 1.45.em),
                            color = Renk.MetinEnSoluk,
                        )
                    }
                }
                TakipAnahtari(kilitteTakip, onTakipDegistir, Modifier.belirme(740, kayma = 18.dp, sureMs = 900))
                Box(Modifier.weight(1f))
                Column(Modifier.belirme(860, kayma = 18.dp, sureMs = 900), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnaDugme("Kilit ekranı görünümüne geç", onKilitGorunumu)
                    val kaynak = remember { MutableInteractionSource() }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .basmaOlcegi(kaynak, 0.97f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(kaynak, null, role = Role.Button, onClick = onAnketlereDon),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Anketlere dön", style = Yazi.Govde, color = Renk.MetinIkincil)
                    }
                }
            }
        }
    }
}

/** 104 dp mavi çekirdek, içinde çizilen onay; çevresinde 3 sn'de bir yayılan üç halka. */
@Composable
private fun OnayAnimasyonu(halkaZamani: Long?, yukseklik: Dp) {
    val azalt = LocalHareketAzalt.current
    var zaman by remember { mutableLongStateOf(halkaZamani ?: 0L) }
    if (halkaZamani == null && !azalt) {
        LaunchedEffect(Unit) {
            val baslangic = withInfiniteAnimationFrameMillis { it }
            while (true) withInfiniteAnimationFrameMillis { zaman = it - baslangic }
        }
    }
    val cekirdek = remember { Animatable(if (azalt) 1f else 0.3f) }
    val cizim = remember { Animatable(if (azalt) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!azalt) {
            cekirdek.animateTo(1f, keyframes {
                durationMillis = 800
                0.3f at 0 using Hareket.Basma
                1.08f at 440 using Hareket.Basma
            })
        }
    }
    LaunchedEffect(Unit) {
        if (!azalt) cizim.animateTo(1f, tween(600, 350, CizimEgrisi))
    }

    Box(Modifier.fillMaxWidth().height(yukseklik).semantics { contentDescription = "Oy gönderildi" }, contentAlignment = Alignment.Center) {
        if (!azalt) {
            repeat(3) { k ->
                Box(
                    Modifier
                        .size(120.dp)
                        .drawBehind {
                            val t = zaman - k * 1000L
                            if (t < 0) return@drawBehind
                            val p = (t % HALKA_DONGU_MS).toFloat() / HALKA_DONGU_MS
                            // ölçek 0,6 → 1,9 (tüm döngü); saydamlık 0 → 0,6 (%25) → 0
                            val olcek = 0.6f + 1.3f * HalkaEgrisi.transform(p)
                            val saydamlik = if (p < 0.25f) 0.6f * HalkaEgrisi.transform(p / 0.25f)
                            else 0.6f * (1f - HalkaEgrisi.transform((p - 0.25f) / 0.75f))
                            drawCircle(
                                Renk.Vurgu.copy(alpha = 0.6f * saydamlik),
                                radius = size.minDimension / 2 * olcek - 0.5.dp.toPx(),
                                style = Stroke(1.dp.toPx()),
                            )
                        },
                )
            }
        }
        val sekil = CircleShape
        Box(
            Modifier
                .size(104.dp)
                .graphicsLayer {
                    scaleX = cekirdek.value
                    scaleY = cekirdek.value
                    alpha = ((cekirdek.value - 0.3f) / 0.78f).coerceIn(0f, 1f)
                }
                .shadow(30.dp, sekil, ambientColor = Color.Transparent, spotColor = Renk.Vurgu)
                .clip(sekil)
                .drawBehind {
                    // radial-gradient(circle at 35% 30%, #A9C1FF, #5B87FF 55%, #2E5BFF)
                    val merkez = Offset(size.width * 0.35f, size.height * 0.30f)
                    val yaricap = Offset(size.width, size.height).minus(merkez).getDistance()
                    drawRect(
                        Brush.radialGradient(
                            0f to Color(0xFFA9C1FF),
                            0.55f to Renk.Vurgu,
                            1f to Renk.VurguKoyu,
                            center = merkez,
                            radius = yaricap,
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            CizilenOnay(Renk.VurguUstuMetin, 2.6f, { cizim.value }, Modifier.size(46.dp))
        }
    }
}

@Composable
private fun Ozet(anket: Anket, oy: Oy) {
    val sekil = RoundedCornerShape(Kose.ListeOgesi)
    data class OzetSatiri(val etiket: String, val deger: String?)
    val satirlar = buildList {
        if (anket.tur.destekVar) {
            add(OzetSatiri("Parti", oy.partiId?.let { Partiler.bul(it).kisaAd }))
            add(OzetSatiri("Lider", oy.liderPartiId?.let { Partiler.bul(it).liderAdlari }))
        }
        if (anket.tur.aslaVar) add(OzetSatiri("Asla", oy.aslaPartiId?.let { Partiler.bul(it).kisaAd }))
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Renk.Yuzey)
            .border(1.dp, Color.White.copy(alpha = 0.07f), sekil)
            .semantics(mergeDescendants = false) { contentDescription = "Seçimlerinin özeti" }
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        satirlar.forEachIndexed { i, s ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .drawBehind {
                        if (i < satirlar.lastIndex) {
                            drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                        }
                    }
                    .semantics(mergeDescendants = true) {}
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(s.etiket, style = Yazi.Govde.copy(fontSize = 14.sp), color = Renk.MetinEnSoluk)
                Text(
                    s.deger ?: "Seçilmedi",
                    style = Yazi.Govde.copy(
                        fontWeight = if (s.deger != null) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.End,
                    ),
                    color = if (s.deger != null) Renk.Metin else Renk.MetinEnSoluk,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TakipAnahtari(acik: Boolean, onDegistir: (Boolean) -> Unit, modifier: Modifier) {
    val sekil = RoundedCornerShape(Kose.BuyukDugme)
    val kaynak = remember { MutableInteractionSource() }
    val iz by animateColorAsState(if (acik) Renk.Vurgu else Color.White.copy(alpha = 0.16f), gecis(Hareket.BasmaMs, Hareket.Yumusak), label = "iz")
    val topuz by animateDpAsState(if (acik) 20.dp else 0.dp, gecis(450, Hareket.Basma), label = "topuz")
    Row(
        modifier
            .fillMaxWidth()
            .basmaOlcegi(kaynak, 0.97f)
            .heightIn(min = 64.dp)
            .clip(sekil)
            .background(Renk.Yuzey)
            .border(1.dp, Color.White.copy(alpha = 0.08f), sekil)
            .toggleable(acik, kaynak, null, role = Role.Switch, onValueChange = onDegistir)
            .testTag("takip-anahtari")
            .padding(start = 16.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Simge(Simgeler.TELEFON_SERIT, Renk.Vurgu, 22.dp, kalinlik = 1.7f)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Kilit ekranında takip et", style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Metin)
            Text("Önceki anket kilit ekranından kalkar", style = Yazi.Etiket12, color = Renk.MetinEnSoluk)
        }
        Box(
            Modifier
                .size(width = 52.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iz),
        ) {
            Box(
                Modifier
                    .padding(3.dp)
                    .offset(x = topuz)
                    .size(26.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Renk.Metin),
            )
        }
    }
}

/** 56 dp fildişi ana düğme. */
@Composable
fun AnaDugme(metin: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val sekil = RoundedCornerShape(Kose.BuyukDugme)
    val kaynak = remember { MutableInteractionSource() }
    Box(
        modifier
            .fillMaxWidth()
            .basmaOlcegi(kaynak, 0.97f)
            .heightIn(min = 56.dp)
            .clip(sekil)
            .background(Renk.Metin)
            .clickable(kaynak, null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(metin, style = Yazi.Govde.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = Renk.Zemin)
    }
}
