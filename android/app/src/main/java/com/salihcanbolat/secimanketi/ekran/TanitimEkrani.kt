package com.salihcanbolat.secimanketi.ekran

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.bildirim.CanliAnketBildirimi
import com.salihcanbolat.secimanketi.bilesen.KilitEkraniSimgesi
import com.salihcanbolat.secimanketi.bilesen.LogoKutusu
import com.salihcanbolat.secimanketi.bilesen.MumCifti
import com.salihcanbolat.secimanketi.bilesen.SeritDurumu
import com.salihcanbolat.secimanketi.bilesen.SeritKarti
import com.salihcanbolat.secimanketi.bilesen.Simge
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.altBosluk
import com.salihcanbolat.secimanketi.bilesen.basmaOlcegi
import com.salihcanbolat.secimanketi.bilesen.rememberSeritDurumu
import com.salihcanbolat.secimanketi.bilesen.ustBosluk
import com.salihcanbolat.secimanketi.kilit.kilitZemini
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.Partiler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private const val SAYFA_SAYISI = 3

/**
 * İlk açılış tanıtımı (üç sayfa): anonimlik, kilit ekranında takip ve basılı tut + kaydır.
 * İkinci sayfada bildirim izni istenir; izin kilit ekranı şeridi için gerekli.
 * Tasarım paketinde bu ekran yok; uygulamanın diliyle çizildi.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TanitimEkrani(
    onBitti: () -> Unit,
    onBildirimIzniIste: () -> Unit,
    modifier: Modifier = Modifier,
    baslangicSayfasi: Int = 0,
) {
    val sayfa = rememberPagerState(initialPage = baslangicSayfasi) { SAYFA_SAYISI }
    val kapsam = rememberCoroutineScope()
    val context = LocalContext.current
    val izinGerekli = remember {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !CanliAnketBildirimi.izinVarMi(context)
    }
    fun ileri() {
        if (sayfa.currentPage == SAYFA_SAYISI - 1) onBitti() else kapsam.launch { sayfa.animateScrollToPage(sayfa.currentPage + 1) }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Renk.Zemin)
            .drawBehind {
                // Üstten yumuşak mavi ışık
                drawCircle(
                    Brush.radialGradient(listOf(Renk.Vurgu.copy(alpha = 0.18f), Color.Transparent), center = Offset(size.width / 2, size.height * 0.22f), radius = size.width * 0.8f),
                    radius = size.width * 0.8f,
                    center = Offset(size.width / 2, size.height * 0.22f),
                )
            }
            .testTag("tanitim"),
    ) {
        Column(Modifier.fillMaxSize().padding(top = ustBosluk(16.dp), bottom = altBosluk(28.dp))) {
            // Üst: "Atla" (son sayfada yok)
            Box(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp)) {
                if (sayfa.currentPage < SAYFA_SAYISI - 1) {
                    Text(
                        "Atla",
                        style = Yazi.Govde.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        color = Renk.MetinEnSoluk,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button, onClick = onBitti)
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                    )
                }
            }

            HorizontalPager(state = sayfa, modifier = Modifier.weight(1f)) { i ->
                val uzaklik = ((sayfa.currentPage - i) + sayfa.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Çizim: sayfa kayarken hafifçe küçülüp solar, metinden biraz daha yavaş kayar (derinlik).
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = 1f - uzaklik * 0.6f
                                val o = 1f - uzaklik * 0.08f
                                scaleX = o
                                scaleY = o
                                translationX = ((sayfa.currentPage - i) + sayfa.currentPageOffsetFraction) * size.width * 0.25f
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        when (i) {
                            0 -> EsitOyCizimi(gorunur = sayfa.settledPage == 0)
                            1 -> KilitEkraniCizimi()
                            else -> SeritDenemesi()
                        }
                    }
                    val (baslik, metin) = SAYFALAR[i]
                    Text(
                        baslik,
                        style = Yazi.Baslik1.copy(fontSize = 30.sp, lineHeight = 1.1.em, textAlign = TextAlign.Center),
                        color = Renk.Metin,
                        modifier = Modifier.padding(top = 24.dp).semantics { heading() },
                    )
                    Text(
                        metin,
                        style = Yazi.Govde.copy(lineHeight = 1.5.em, textAlign = TextAlign.Center),
                        color = Renk.MetinSoluk,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp).heightIn(min = 90.dp),
                    )
                }
            }

            SayfaNoktalari(sayfa, Modifier.align(Alignment.CenterHorizontally).padding(vertical = 18.dp))

            val izinSayfasi = sayfa.currentPage == 1 && izinGerekli
            AnimatedContent(
                targetState = when {
                    sayfa.currentPage == SAYFA_SAYISI - 1 -> "Başla"
                    izinSayfasi -> "Bildirimlere izin ver"
                    else -> "Devam"
                },
                transitionSpec = {
                    (slideInVertically(tween(Hareket.BasmaMs, easing = Hareket.Basma)) { it / 3 } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(tween(200)) { -it / 3 } + fadeOut(tween(150)))
                },
                modifier = Modifier.padding(horizontal = 20.dp),
                label = "tanitimDugmesi",
            ) { metin ->
                AnaDugme(
                    metin,
                    onClick = {
                        if (izinSayfasi) onBildirimIzniIste()
                        ileri()
                    },
                    modifier = Modifier.testTag("tanitim-ileri"),
                )
            }
            // İzin sayfasında ikinci seçenek; yer her zaman ayrılır, düğme zıplamaz.
            Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                if (izinSayfasi) {
                    Text(
                        "Şimdi değil",
                        style = Yazi.Govde.copy(fontSize = 14.sp),
                        color = Renk.MetinIkincil,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button) { ileri() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

private val SAYFALAR = listOf(
    "Her oy eşit sayılır" to
        "Kime oy verdiğin hesabınla ilişkilendirilmez. Gerçek oylar gelene kadar tüm partiler eşit gösterilir; hiçbir parti önde ya da geride değildir.",
    "Kilit ekranında takip et" to
        "Seçtiğin anketin şeridi kilit ekranında, saatin hemen altında durur. Kilidi açmadan dokunup incele. Senin tercihin orada asla görünmez.",
    "Basılı tut, kaydır" to
        "Bir logoya basılı tut, parmağını kaldırmadan kaydır. Her sütun bir parti: solda partinin, sağda liderinin mumu.",
)

/** Etkin sayfa uzun mavi hap, diğerleri küçük nokta. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SayfaNoktalari(sayfa: PagerState, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(SAYFA_SAYISI) { i ->
            val etkin = sayfa.currentPage == i
            val genislik by animateDpAsState(if (etkin) 22.dp else 6.dp, gecis(Hareket.BasmaMs, Hareket.Basma), label = "nokta")
            Box(
                Modifier
                    .size(width = genislik, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (etkin) Renk.Vurgu else Color.White.copy(alpha = 0.2f)),
            )
        }
    }
}

/** 1 · Büyük bir mum çifti ve altında onaylı logo kutusu (uygulama simgesiyle aynı); "ANONİM" rozeti. */
@Composable
private fun EsitOyCizimi(gorunur: Boolean) {
    val azalt = LocalHareketAzalt.current
    val oran = if (gorunur || azalt) 1f else 0.12f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width = 120.dp, height = 170.dp)
                .drawBehind {
                    drawCircle(Brush.radialGradient(listOf(Renk.Vurgu.copy(alpha = 0.28f), Color.Transparent)), radius = size.width * 1.1f)
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            MumCifti(parti = oran * 0.82f, lider = oran, genislik = 30.dp, fitil = 10.dp, modifier = Modifier.height(170.dp))
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.size(width = 72.dp, height = 58.dp).clip(RoundedCornerShape(16.dp)).background(Renk.LogoKutusu),
            contentAlignment = Alignment.Center,
        ) {
            Simge(Simgeler.ONAY, Renk.ZeminKilitGradyan[1], 32.dp, kalinlik = 2.6f)
        }
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Simge(Simgeler.KALKAN, Renk.VurguAcik, 14.dp)
            Text("ANONİM · SEMBOLİK", style = Yazi.EnKucukMono.copy(letterSpacing = 0.08.em), color = Renk.VurguAcik)
        }
    }
}

/** 2 · Küçük bir telefon: kilit ekranı, saat ve altında gerçek şerit kartı (küçültülmüş). */
@Composable
private fun KilitEkraniCizimi() {
    val sekil = RoundedCornerShape(38.dp)
    Box(contentAlignment = Alignment.BottomCenter) {
        Box(
            Modifier
                .padding(bottom = 22.dp)
                .size(width = 206.dp, height = 360.dp)
                .clip(sekil)
                .kilitZemini()
                .border(1.5.dp, Color.White.copy(alpha = 0.14f), sekil),
        ) {
            Column(Modifier.padding(top = 34.dp, start = 16.dp, end = 16.dp)) {
                Text("21:40", style = Yazi.Saat.copy(fontSize = 54.sp), color = Renk.Metin)
                Text("Pazar, 25 Ekim", style = Yazi.Tarih.copy(fontSize = 10.sp), color = Renk.MetinIkincil, modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.height(14.dp))
                Box(Modifier.olcekliGenislik(0.5f, 348.dp)) {
                    SeritKarti(
                        anket = Anketler.genelSecim,
                        ogeler = SembolikVeri.serit(),
                        anketDurumu = SembolikVeri.durum(),
                        durum = SeritDurumu(),
                        ipucu = "Dokun, incele →",
                        etkilesimli = false,
                    )
                }
            }
            // Telefonun alt çentiği
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .size(width = 64.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f)),
            )
        }
        // Telefonun altına binen "Takipte" rozeti
        Row(
            Modifier
                .clip(RoundedCornerShape(Kose.Dugme))
                .background(Renk.Yuzey2)
                .border(1.dp, Renk.Vurgu.copy(alpha = 0.6f), RoundedCornerShape(Kose.Dugme))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KilitEkraniSimgesi(true, Renk.VurguAcik, 18.dp)
            Text("Takipte", style = Yazi.Govde.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = Renk.VurguAcik)
        }
    }
}

/**
 * 3 · Gerçek, etkileşimli şerit: burada denenebilir. Basılı tutunca kart aşağı doğru büyür (metnin
 * üstüne biner, bırakınca geri döner).
 */
@Composable
private fun SeritDenemesi() {
    val durum = rememberSeritDurumu()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(Kose.Kart + 6.dp))
                .kilitZemini()
                .padding(8.dp),
        ) {
            SeritKarti(
                anket = Anketler.genelSecim,
                ogeler = SembolikVeri.serit(),
                anketDurumu = SembolikVeri.durum(),
                durum = durum,
                ipucu = "Dene: basılı tut",
                ipucuVurgulu = true,
                uyanis = true,
                modifier = Modifier.fillMaxWidth().testTag("tanitim-serit"),
            )
        }
    }
}

/**
 * İçeriği [genislik] genişliğinde ölçer, [olcek] kadar küçültüp yerleştirir; yerleşimde de küçük
 * yer kaplar (graphicsLayer'ın tersine).
 */
private fun Modifier.olcekliGenislik(olcek: Float, genislik: Dp): Modifier = layout { olculebilir, _ ->
    val p = olculebilir.measure(Constraints.fixedWidth(genislik.roundToPx()))
    layout((p.width * olcek).roundToInt(), (p.height * olcek).roundToInt()) {
        p.placeWithLayer(0, 0) {
            scaleX = olcek
            scaleY = olcek
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}
