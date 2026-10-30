package com.salihcanbolat.secimanketi.ekran

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.bilesen.Glif
import com.salihcanbolat.secimanketi.bilesen.SecimIsareti
import com.salihcanbolat.secimanketi.bilesen.Simge
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.altBosluk
import com.salihcanbolat.secimanketi.bilesen.basmaOlcegi
import com.salihcanbolat.secimanketi.bilesen.belirme
import com.salihcanbolat.secimanketi.bilesen.kesikKenar
import com.salihcanbolat.secimanketi.bilesen.pop
import com.salihcanbolat.secimanketi.bilesen.rememberGiris
import com.salihcanbolat.secimanketi.bilesen.ustBosluk
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.Lider
import com.salihcanbolat.secimanketi.veri.Oy
import com.salihcanbolat.secimanketi.veri.Parti
import com.salihcanbolat.secimanketi.veri.Partiler
import kotlinx.coroutines.launch

/** Bölümler: anket türüne göre görünür. */
private enum class Bolum(val baslik: String, val sekmeAdi: String) {
    PARTI("Desteklediğin parti", "Parti"),
    LIDER("Desteklediğin lider", "Lider"),
    ASLA("Asla oy vermem", "Asla"),
}

/** Listedeki satırlar; bölüm sekmeleri bu sıraya göre kaydırır. */
private sealed interface Satir {
    val anahtar: String
    data object Ust : Satir { override val anahtar = "ust" }
    data object Sekmeler : Satir { override val anahtar = "sekmeler" }
    data class Baslik(val bolum: Bolum) : Satir { override val anahtar = "baslik-$bolum" }
    data class Aciklama(val bolum: Bolum, val metin: String) : Satir { override val anahtar = "aciklama-$bolum" }
    data class PartiSatiri(val parti: Parti, val ilk: Boolean) : Satir { override val anahtar = "parti-${parti.id}" }
    data class LiderSatiri(val parti: Parti, val ilk: Boolean) : Satir { override val anahtar = "lider-${parti.id}" }
    data class AslaSatiri(val partiler: List<Parti>, val sira: Int) : Satir { override val anahtar = "asla-$sira" }
}

/**
 * Ekran 2 · Oy verme. Parti zorunlu; lider ve "asla" isteğe bağlı. Her bölümde tek seçim; seçime
 * tekrar dokunmak ya da "Geri al" seçimi kaldırır. Lider seçimi partiden bağımsızdır.
 */
@Composable
fun OyVermeEkrani(
    anket: Anket,
    onGeri: () -> Unit,
    onGonder: (Oy) -> Unit,
    modifier: Modifier = Modifier,
    baslangicParti: String? = null,
    baslangicLider: String? = null,
    baslangicAsla: String? = null,
) {
    var parti by rememberSaveable { mutableStateOf(baslangicParti) }
    var lider by rememberSaveable { mutableStateOf(baslangicLider) }
    var asla by rememberSaveable { mutableStateOf(baslangicAsla) }
    val dokunus = LocalHapticFeedback.current
    fun tik() = dokunus.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    val destek = anket.tur.destekVar
    val aslaVar = anket.tur.aslaVar
    val gonderilebilir = if (destek) parti != null else asla != null

    val satirlar = remember(anket, parti) { satirlariKur(anket, parti) }
    val liste = rememberLazyListState()
    val kapsam = rememberCoroutineScope()
    fun bolumeGit(bolum: Bolum) {
        val sira = satirlar.indexOfFirst { it is Satir.Baslik && it.bolum == bolum }
        if (sira >= 0) kapsam.launch { liste.animateScrollToItem(sira) }
    }

    val giris0 = rememberGiris(0)
    val giris1 = rememberGiris(60)
    val giris2 = rememberGiris(140)
    fun girisi(satir: Satir): State<Float>? = when (satir) {
        Satir.Ust -> giris0
        Satir.Sekmeler -> giris1
        is Satir.Baslik -> if (satir.bolum == ilkBolum(anket)) giris2 else null
        is Satir.PartiSatiri -> giris2
        is Satir.AslaSatiri -> if (!destek) giris2 else null
        is Satir.Aciklama -> if (satir.bolum == ilkBolum(anket)) giris2 else null
        else -> null
    }

    val altCubukYuksekligi = 202.dp + (altBosluk(34.dp) - 34.dp)
    Box(modifier.fillMaxSize().background(Renk.Zemin)) {
        LazyColumn(
            state = liste,
            modifier = Modifier.fillMaxSize().testTag("oy-listesi"),
            contentPadding = PaddingValues(start = Olcu.EkranKenarBoslugu, end = Olcu.EkranKenarBoslugu, top = ustBosluk(56.dp), bottom = altCubukYuksekligi),
        ) {
            items(satirlar, key = { it.anahtar }) { satir ->
                val g = girisi(satir)
                val m = if (g != null) Modifier.belirme(g) else Modifier
                when (satir) {
                    Satir.Ust -> UstSatir(anket, onGeri, m)
                    Satir.Sekmeler -> Sekmeler(anket, parti, lider, asla, ::bolumeGit, m.padding(top = 26.dp))
                    is Satir.Baslik -> {
                        val (secimVar, temizle) = when (satir.bolum) {
                            Bolum.PARTI -> (parti != null) to { parti = null }
                            Bolum.LIDER -> (lider != null) to { lider = null }
                            Bolum.ASLA -> (asla != null) to { asla = null }
                        }
                        BolumBasligiSatiri(satir.bolum, secimVar, temizle, m.padding(top = 13.dp))
                    }
                    is Satir.Aciklama -> Text(
                        satir.metin,
                        style = Yazi.Kucuk.copy(lineHeight = 1.4.em),
                        color = Renk.MetinEnSoluk,
                        modifier = m.padding(bottom = 12.dp),
                    )
                    is Satir.PartiSatiri -> PartiSecimi(
                        parti = satir.parti,
                        secili = parti == satir.parti.id,
                        onSec = {
                            val yeni = if (parti == satir.parti.id) null else satir.parti.id
                            if (yeni != null) tik()
                            parti = yeni
                            if (asla == yeni) asla = null
                        },
                        modifier = m.padding(top = if (satir.ilk) 0.dp else 8.dp),
                    )
                    is Satir.LiderSatiri -> LiderSecimi(
                        parti = satir.parti,
                        secili = lider == satir.parti.id,
                        partininLideri = parti == satir.parti.id,
                        onSec = {
                            val yeni = if (lider == satir.parti.id) null else satir.parti.id
                            if (yeni != null) tik()
                            lider = yeni
                        },
                        modifier = Modifier.animateItem().padding(top = if (satir.ilk) 0.dp else 8.dp),
                    )
                    is Satir.AslaSatiri -> Row(
                        m.padding(top = if (satir.sira == 0) 0.dp else 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        satir.partiler.forEach { p ->
                            AslaSecimi(
                                parti = p,
                                secili = asla == p.id,
                                devreDisi = destek && parti == p.id,
                                onSec = {
                                    val yeni = if (asla == p.id) null else p.id
                                    if (yeni != null) tik()
                                    asla = yeni
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - satir.partiler.size) { Box(Modifier.weight(1f)) }
                    }
                }
            }
        }

        AltCubuk(
            anket = anket,
            parti = parti,
            lider = lider,
            asla = asla,
            gonderilebilir = gonderilebilir,
            onGonder = {
                dokunus.performHapticFeedback(HapticFeedbackType.LongPress)
                onGonder(Oy(anket.id, parti.takeIf { destek }, lider.takeIf { destek }, asla.takeIf { aslaVar }, System.currentTimeMillis()))
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun ilkBolum(anket: Anket) = if (anket.tur.destekVar) Bolum.PARTI else Bolum.ASLA

private fun satirlariKur(anket: Anket, parti: String?): List<Satir> = buildList {
    add(Satir.Ust)
    add(Satir.Sekmeler)
    val partiler = Partiler.tumu
    if (anket.tur.destekVar) {
        add(Satir.Baslik(Bolum.PARTI))
        partiler.forEachIndexed { i, p -> add(Satir.PartiSatiri(p, i == 0)) }
        add(Satir.Baslik(Bolum.LIDER))
        add(
            Satir.Aciklama(
                Bolum.LIDER,
                "İsteğe bağlı ve partinden bağımsız: başka bir partinin liderini de seçebilirsin. " +
                    "Eş genel başkanlı partilerde ikisi birlikte seçilir.",
            ),
        )
        // Seçilen partinin lideri listenin başına gelir; başka lider seçmek serbest.
        val sirali = partiler.sortedBy { if (it.id == parti) 0 else 1 }
        sirali.forEachIndexed { i, p -> add(Satir.LiderSatiri(p, i == 0)) }
    }
    if (anket.tur.aslaVar) {
        add(Satir.Baslik(Bolum.ASLA))
        add(
            Satir.Aciklama(
                Bolum.ASLA,
                if (anket.tur.destekVar) "İsteğe bağlı. Desteklediğin parti burada seçilemez."
                else "Asla oy vermeyeceğin partiyi seç. Tek seçim yapılır.",
            ),
        )
        partiler.chunked(3).forEachIndexed { i, grup -> add(Satir.AslaSatiri(grup, i)) }
    }
}

@Composable
internal fun UstSatir(anket: Anket, onGeri: () -> Unit, modifier: Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val kaynak = remember { MutableInteractionSource() }
        Box(
            Modifier
                .basmaOlcegi(kaynak)
                .size(Olcu.MinDokunmaAlani)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .clickable(kaynak, null, role = Role.Button, onClick = onGeri)
                .semantics { contentDescription = "Anketlere dön" },
            contentAlignment = Alignment.Center,
        ) {
            Simge(Simgeler.GERI, Renk.Metin, 18.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${anket.tur.etiket} · ANONİM", style = Yazi.EnKucukMono.copy(letterSpacing = 0.08.em), color = Renk.MetinEnSoluk)
            Text(anket.ad, style = Yazi.Baslik2, color = Renk.Metin, modifier = Modifier.semantics { heading() })
        }
    }
}

@Composable
private fun Sekmeler(
    anket: Anket,
    parti: String?,
    lider: String?,
    asla: String?,
    onGit: (Bolum) -> Unit,
    modifier: Modifier,
) {
    data class BolumSekmesi(val ust: String, val bolum: Bolum, val tamam: Boolean, val etkin: Boolean)
    val sekmeler = if (anket.tur.destekVar) {
        buildList {
            add(BolumSekmesi("1 · Zorunlu", Bolum.PARTI, parti != null, true))
            add(BolumSekmesi("2 · İsteğe bağlı", Bolum.LIDER, lider != null, parti != null))
            if (anket.tur.aslaVar) add(BolumSekmesi("3 · İsteğe bağlı", Bolum.ASLA, asla != null, parti != null))
        }
    } else {
        listOf(BolumSekmesi("1 · Zorunlu", Bolum.ASLA, asla != null, true))
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        sekmeler.forEach { s ->
            val spec = gecis<Color>(Hareket.BasmaMs, Hareket.Yumusak)
            val zemin by animateColorAsState(
                when {
                    s.tamam -> Renk.Vurgu.copy(alpha = 0.14f)
                    s.etkin -> Color.White.copy(alpha = 0.08f)
                    else -> Color.White.copy(alpha = 0.03f)
                },
                spec, label = "sekmeZemin",
            )
            val renk by animateColorAsState(
                when {
                    s.tamam -> Renk.Vurgu
                    s.etkin -> Renk.Metin
                    else -> Renk.MetinEnSoluk
                },
                spec, label = "sekmeRenk",
            )
            val sekil = RoundedCornerShape(Kose.Dugme)
            val kaynak = remember { MutableInteractionSource() }
            Column(
                Modifier
                    .weight(1f)
                    .basmaOlcegi(kaynak, 0.97f)
                    .heightIn(min = Olcu.MinDokunmaAlani)
                    .clip(sekil)
                    .background(zemin)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), sekil)
                    .clickable(kaynak, null, role = Role.Button, onClickLabel = "${s.bolum.sekmeAdi} bölümüne git") { onGit(s.bolum) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(s.ust, style = Yazi.EnKucuk.copy(fontWeight = FontWeight.Normal), color = renk.copy(alpha = renk.alpha * 0.75f), maxLines = 1)
                Text(s.bolum.sekmeAdi, style = Yazi.Kucuk.copy(fontWeight = FontWeight.SemiBold), color = renk)
            }
        }
    }
}

/**
 * Bölüm başlığı ve "Geri al". Düğme görünmezken de yeri ayrılır (48 dp); seçim yapılınca liste
 * zıplamaz.
 */
@Composable
private fun BolumBasligiSatiri(bolum: Bolum, secimVar: Boolean, onTemizle: () -> Unit, modifier: Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = Olcu.MinDokunmaAlani), verticalAlignment = Alignment.CenterVertically) {
        Text(
            bolum.baslik,
            style = Yazi.Baslik3.copy(letterSpacing = (-0.01).em),
            color = Renk.Metin,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        val gorunurluk by animateFloatAsState(if (secimVar) 1f else 0f, gecis(250, Hareket.Yumusak), label = "geriAl")
        if (gorunurluk > 0f) {
            Box(
                Modifier
                    .heightIn(min = Olcu.MinDokunmaAlani)
                    .alpha(gorunurluk)
                    .clickable(enabled = secimVar, role = Role.Button, onClick = onTemizle)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Geri al",
                    style = Yazi.Govde.copy(fontSize = 14.sp),
                    color = if (bolum == Bolum.ASLA) Renk.Asla else Renk.Vurgu,
                    modifier = Modifier.semantics { contentDescription = "${bolum.sekmeAdi} seçimini geri al" },
                )
            }
        }
    }
}

/** Parti ve lider satırlarının ortak kabı: seçilince mavi kenar, hafif zemin ve dış parıltı. */
@Composable
private fun SecimSatiri(
    secili: Boolean,
    onSec: () -> Unit,
    modifier: Modifier,
    icerik: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val sekil = RoundedCornerShape(18.dp)
    val kaynak = remember { MutableInteractionSource() }
    val spec = gecis<Color>(Hareket.BasmaMs, Hareket.Yumusak)
    val kenar by animateColorAsState(if (secili) Renk.Vurgu.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.07f), spec, label = "satirKenar")
    val zemin by animateColorAsState(
        if (secili) Renk.Vurgu.copy(alpha = 0.08f).compositeOver(Renk.Zemin) else Renk.Yuzey,
        spec, label = "satirZemin",
    )
    val parilti by animateFloatAsState(if (secili) 1f else 0f, gecis(Hareket.BasmaMs, Hareket.Yumusak), label = "satirParilti")
    val golge by animateDpAsState(if (secili) 12.dp else 0.dp, gecis(Hareket.BasmaMs, Hareket.Yumusak), label = "satirGolge")
    Row(
        modifier
            .fillMaxWidth()
            .basmaOlcegi(kaynak, 0.97f)
            .drawBehind {
                // 0 0 0 4px rgba(91,135,255,0.10)
                if (parilti > 0f) {
                    val h = 4.dp.toPx()
                    drawRoundRect(
                        Renk.Vurgu.copy(alpha = 0.10f * parilti),
                        topLeft = Offset(-h / 2, -h / 2),
                        size = size.copy(width = size.width + h, height = size.height + h),
                        cornerRadius = CornerRadius(18.dp.toPx() + h / 2),
                        style = Stroke(h),
                    )
                }
            }
            .shadow(golge, sekil, ambientColor = Color.Transparent, spotColor = Renk.Vurgu.copy(alpha = 0.5f))
            .clip(sekil)
            .background(zemin)
            .border(1.dp, kenar, sekil)
            .selectable(secili, kaynak, null, role = Role.RadioButton, onClick = onSec)
            .heightIn(min = 60.dp)
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icerik()
        SecimIsareti(secili)
    }
}

@Composable
private fun PartiSecimi(parti: Parti, secili: Boolean, onSec: () -> Unit, modifier: Modifier) {
    SecimSatiri(secili, onSec, modifier.testTag("parti-${parti.id}")) {
        val sekil = RoundedCornerShape(13.dp)
        Box(
            Modifier
                .size(44.dp)
                .clip(sekil)
                .background(if (parti.logo != null) Renk.LogoKutusu else Renk.Yuzey2)
                .border(1.dp, Color.White.copy(alpha = 0.08f), sekil),
            contentAlignment = Alignment.Center,
        ) {
            if (parti.logo != null) {
                Image(painterResource(parti.logo), null, contentScale = ContentScale.Fit, modifier = Modifier.padding(2.dp))
            } else {
                Glif(parti.glif, Renk.Glif, Modifier.size(20.dp), kalinlik = 1.8f)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(parti.kisaAd, style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Metin)
            Text(parti.uzunAd, style = Yazi.Etiket12, color = Renk.MetinEnSoluk, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun LiderSecimi(parti: Parti, secili: Boolean, partininLideri: Boolean, onSec: () -> Unit, modifier: Modifier) {
    SecimSatiri(secili, onSec, modifier.testTag("lider-${parti.id}")) {
        val ikili = parti.liderler.size > 1
        Box(Modifier.size(width = 56.dp, height = 44.dp)) {
            LiderFotografi(parti.liderler[0], ikinci = false)
            parti.liderler.getOrNull(1)?.let { LiderFotografi(it, ikinci = true, modifier = Modifier.offset(x = 18.dp)) }
        }
        Column(
            Modifier.weight(1f).padding(start = if (ikili) 8.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(parti.liderAdlari, style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.25.em), color = Renk.Metin)
            Text(parti.kisaAd, style = Yazi.Etiket12, color = Renk.MetinEnSoluk)
            if (partininLideri) {
                Text(
                    "Partinin lideri",
                    style = Yazi.EnKucuk.copy(fontWeight = FontWeight.SemiBold),
                    color = Renk.VurguAcik,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(CircleShape)
                        .background(Renk.Vurgu.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

/** 44 dp yuvarlak lider fotoğrafı; fotoğraf yoksa baş harfler. İkinci eş başkan koyu kenarla biner. */
@Composable
private fun LiderFotografi(lider: Lider, ikinci: Boolean, modifier: Modifier = Modifier) {
    val zemin = if (ikinci) {
        Brush.linearGradient(listOf(Color(0xFF2C3968), Color(0xFF182042)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF24305C), Color(0xFF141B38)))
    }
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(zemin)
            .border(if (ikinci) 2.dp else 1.dp, if (ikinci) Renk.Zemin else Color.White.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (lider.foto != null) {
            Image(painterResource(lider.foto), null, contentScale = ContentScale.Crop, modifier = Modifier.size(44.dp))
        } else {
            Text(lider.basHarfler, style = Yazi.Kucuk.copy(fontWeight = FontWeight.SemiBold), color = Renk.Glif)
        }
    }
}

@Composable
private fun AslaSecimi(parti: Parti, secili: Boolean, devreDisi: Boolean, onSec: () -> Unit, modifier: Modifier) {
    val sekil = RoundedCornerShape(16.dp)
    val kaynak = remember { MutableInteractionSource() }
    val spec = gecis<Color>(Hareket.BasmaMs, Hareket.Yumusak)
    val kenar by animateColorAsState(if (secili) Renk.Asla.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.07f), spec, label = "aslaKenar")
    val zemin by animateColorAsState(if (secili) Renk.Asla.copy(alpha = 0.12f).compositeOver(Renk.Zemin) else Renk.Yuzey, spec, label = "aslaZemin")
    val renk by animateColorAsState(if (secili) Renk.Asla else Renk.Glif, spec, label = "aslaRenk")
    val saydamlik by animateFloatAsState(if (devreDisi) 0.3f else 1f, gecis(Hareket.BasmaMs, Hareket.Yumusak), label = "aslaSaydam")
    Column(
        modifier
            .basmaOlcegi(kaynak, if (devreDisi) 1f else 0.97f)
            .alpha(saydamlik)
            .heightIn(min = 56.dp)
            .clip(sekil)
            .background(zemin)
            .border(1.dp, kenar, sekil)
            .selectable(secili, kaynak, null, enabled = !devreDisi, role = Role.RadioButton, onClick = onSec)
            .testTag("asla-${parti.id}")
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(parti.kisaAd, style = Yazi.Kucuk.copy(fontWeight = FontWeight.SemiBold), color = renk, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (secili) {
            Text("ASLA", style = Yazi.EnKucuk.copy(letterSpacing = 0.06.em), color = renk, modifier = Modifier.pop())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AltCubuk(
    anket: Anket,
    parti: String?,
    lider: String?,
    asla: String?,
    gonderilebilir: Boolean,
    onGonder: () -> Unit,
    modifier: Modifier,
) {
    fun ad(id: String?) = id?.let { Partiler.bul(it).kisaAd } ?: "—"
    Column(
        modifier
            .fillMaxWidth()
            .background(
                // Tasarımda solma özet çiplerinin arkasında başlıyor; altından geçen satırlar
                // çiplerle karışmasın diye solma çiplerin üstünde biter.
                Brush.verticalGradient(
                    0f to Color(0x000A0B0F),
                    0.16f to Color(0xEB0A0B0F),
                    0.3f to Renk.Zemin,
                ),
            )
            .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = altBosluk(34.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (anket.tur.destekVar) {
                OzetCipi("Parti: ${ad(parti)}")
                OzetCipi("Lider: " + (lider?.let { "${ad(it)} lideri" } ?: "—"))
            }
            if (anket.tur.aslaVar) OzetCipi("Asla: ${ad(asla)}")
        }
        val azalt = LocalHareketAzalt.current
        AnimatedContent(
            targetState = gonderilebilir,
            transitionSpec = {
                if (azalt) {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                } else {
                    (fadeIn(tween(250)) + scaleIn(tween(Hareket.BasmaMs, easing = Hareket.Basma), initialScale = 0.96f)) togetherWith fadeOut(tween(150))
                }
            },
            label = "gonder",
        ) { etkin ->
            if (etkin) GonderDugmesi(onGonder) else PasifGonderDugmesi()
        }
    }
}

@Composable
private fun OzetCipi(metin: String) {
    Text(
        metin,
        style = Yazi.Etiket12,
        color = Renk.MetinIkincil,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun GonderDugmesi(onClick: () -> Unit) {
    val sekil = RoundedCornerShape(Kose.BuyukDugme)
    val kaynak = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .basmaOlcegi(kaynak, 0.97f)
            .shadow(18.dp, sekil, ambientColor = Color.Transparent, spotColor = Renk.Vurgu.copy(alpha = 0.6f))
            .clip(sekil)
            .background(Renk.Vurgu)
            .clickable(kaynak, null, role = Role.Button, onClick = onClick)
            .heightIn(min = 56.dp)
            .testTag("gonder"),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Simge(Simgeler.GONDER, Renk.VurguUstuMetin, 18.dp, kalinlik = 2f)
        Text("Google hesabınla onayla ve gönder", style = Yazi.Govde.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = Renk.VurguUstuMetin)
    }
}

@Composable
private fun PasifGonderDugmesi() {
    val sekil = RoundedCornerShape(Kose.BuyukDugme)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(sekil)
            .background(Color.White.copy(alpha = 0.03f))
            .kesikKenar(Color.White.copy(alpha = 0.18f), Kose.BuyukDugme)
            .semantics { disabled() }
            .testTag("gonder-pasif"),
        contentAlignment = Alignment.Center,
    ) {
        Text("Göndermek için bir parti seç", style = Yazi.Govde.copy(fontWeight = FontWeight.Medium), color = Renk.MetinEnSoluk)
    }
}
