package com.salihcanbolat.secimanketi.ekran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.salihcanbolat.secimanketi.bilesen.KilitEkraniSimgesi
import com.salihcanbolat.secimanketi.bilesen.KilitTakipDugmesi
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.salihcanbolat.secimanketi.bilesen.BaglantiYokKarti
import com.salihcanbolat.secimanketi.bilesen.DurumRozeti
import com.salihcanbolat.secimanketi.bilesen.YasakPaneli
import com.salihcanbolat.secimanketi.bilesen.yenidenYayinMetni
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.SonucGorunumu
import com.salihcanbolat.secimanketi.bilesen.LogoKutusu
import com.salihcanbolat.secimanketi.bilesen.Simge
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.aciliGradyan
import com.salihcanbolat.secimanketi.bilesen.basmaOlcegi
import com.salihcanbolat.secimanketi.bilesen.belirme
import com.salihcanbolat.secimanketi.bilesen.cizgiliDolgu
import com.salihcanbolat.secimanketi.bilesen.ustBosluk
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import com.salihcanbolat.secimanketi.veri.SeritOgesi
import com.salihcanbolat.secimanketi.veri.UygulamaDurumu
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Turkce = Locale.forLanguageTag("tr")

/**
 * Ekran 1 · Anketler. Üstte kilit ekranındaki anketin kartı, altında katılınan ve katılınabilecek
 * anketler. Alt gezinme bu ekranın dışında, [Uygulama] içinde çizilir.
 */
@Composable
fun AnketlerEkrani(
    durum: UygulamaDurumu,
    onIncele: (Anket) -> Unit,
    onKatil: (Anket) -> Unit,
    onHesap: () -> Unit,
    modifier: Modifier = Modifier,
    onAyrinti: (Anket) -> Unit = onIncele,
    bugun: LocalDate = LocalDate.now(),
    altBoslukEk: Dp = 0.dp,
    onYenidenDene: () -> Unit = {},
    /** Yalnızca hata ayıklama sürümünde: "CANLI · tarih" etiketine uzun basınca durum önizlemesi. */
    onDurumOnizleme: (() -> Unit)? = null,
) {
    val kilitAnketi = durum.kilitAnketi
    val kilitDurumu = durum.anketDurumu(kilitAnketi.id)
    val katildiklarim = Anketler.tumu.filter { durum.katildiMi(it.id) }
    val katilabileceklerim = Anketler.tumu.filterNot { durum.katildiMi(it.id) }

    Box(modifier.fillMaxSize().background(Renk.Zemin)) {
        NefesAlanIsik()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = Olcu.EkranKenarBoslugu, end = Olcu.EkranKenarBoslugu, top = ustBosluk(64.dp), bottom = 120.dp + altBoslukEk),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Baslik(bugun, onHesap, Modifier.belirme(0, olcek = 0.985f), onDurumOnizleme)
            if (kilitDurumu.cevrimdisi) {
                BaglantiYokKarti(kilitDurumu, onYenidenDene, Modifier.belirme(30, olcek = 0.985f))
            }
            KilitEkraniKarti(
                anket = kilitAnketi,
                ogeler = durum.serit(kilitAnketi),
                anketDurumu = kilitDurumu,
                onClick = { onIncele(kilitAnketi) },
                modifier = Modifier.belirme(50, olcek = 0.985f),
            )
            if (katildiklarim.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BolumBasligi("KATILDIKLARIM", Modifier.belirme(120, olcek = 0.985f))
                    katildiklarim.forEach { anket ->
                        AnketKarti(
                            anket = anket,
                            aciklama = katildiAciklamasi(durum.oy(anket.id)?.zaman),
                            katildi = true,
                            zilAcik = durum.zilAcikMi(anket.id),
                            kilitte = kilitAnketi.id == anket.id,
                            onZil = { durum.zilDegistir(anket.id) },
                            onKilit = { durum.kilideAl(anket.id) },
                            onEylem = { onAyrinti(anket) },
                            onAc = { onAyrinti(anket) },
                            modifier = Modifier.belirme(190, olcek = 0.985f),
                        )
                    }
                }
            }
            if (katilabileceklerim.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BolumBasligi("KATILABİLECEKLERİM", Modifier.belirme(260, olcek = 0.985f))
                    katilabileceklerim.forEach { anket ->
                        AnketKarti(
                            anket = anket,
                            aciklama = anket.tur.aciklama,
                            katildi = false,
                            zilAcik = durum.zilAcikMi(anket.id),
                            kilitte = kilitAnketi.id == anket.id,
                            onZil = { durum.zilDegistir(anket.id) },
                            onKilit = { durum.kilideAl(anket.id) },
                            onEylem = { onKatil(anket) },
                            onAc = { onAyrinti(anket) },
                            eylemMetni = if (durum.anketDurumu(anket.id).gorunum == SonucGorunumu.HENUZ_OY_YOK) "İlk oyu ver" else "Katıl",
                            modifier = Modifier.belirme(330, olcek = 0.985f),
                        )
                    }
                }
            }
        }
    }
}

/** "Oyun sayıldı · — katılımcı · 21:40". Katılımcı sayısı gerçek veri gelene kadar "—". */
private fun katildiAciklamasi(zaman: Long?): String {
    val saat = zaman?.takeIf { it > 0 }?.let {
        DateTimeFormatter.ofPattern("HH:mm", Turkce).format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))
    }
    return listOfNotNull("Oyun sayıldı", "— katılımcı", saat).joinToString(" · ")
}

/** Üstte, ekranın ortasında yavaşça nefes alan mavi ışık (3,2 sn). */
@Composable
private fun BoxScope.NefesAlanIsik() {
    val azalt = LocalHareketAzalt.current
    val gecis = rememberInfiniteTransition(label = "nefes")
    val alfa by gecis.animateFloat(
        initialValue = if (azalt) 1f else 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nefesAlfa",
    )
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .offset(y = (-160).dp)
            .requiredSize(520.dp, 320.dp)
            .graphicsLayer { this.alpha = alfa }
            .drawBehind {
                // radial-gradient(closest-side, Vurgu .16 → 0): daireyi dikeyde basıp elips yapar.
                val r = size.width / 2
                scale(1f, size.height / size.width, pivot = center) {
                    drawCircle(Brush.radialGradient(listOf(Renk.Vurgu.copy(alpha = 0.16f), Renk.Vurgu.copy(alpha = 0f)), center, r), r, center)
                }
            },
    )
}

@Composable
private fun Baslik(bugun: LocalDate, onHesap: () -> Unit, modifier: Modifier = Modifier, onUzunBas: (() -> Unit)? = null) {
    val tarih = DateTimeFormatter.ofPattern("d MMMM yyyy", Turkce).format(bugun).uppercase(Turkce)
    val dokunus = LocalHapticFeedback.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = if (onUzunBas != null) {
                    Modifier.pointerInput(onUzunBas) {
                        detectTapGestures(onLongPress = {
                            dokunus.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUzunBas()
                        })
                    }
                } else {
                    Modifier
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CanliNokta()
                Text("CANLI · $tarih", style = UstEtiket, color = Renk.MetinEnSoluk)
            }
            Text("Anketler", style = Yazi.Baslik1.copy(lineHeight = 1.05.em), color = Renk.Metin, modifier = Modifier.semantics { heading() })
            Text("Her oy eşit sayılır. Her ses anonim kalır.", style = Yazi.Govde.copy(fontSize = 14.sp), color = Renk.MetinSoluk)
        }
        val kaynak = remember { MutableInteractionSource() }
        Box(
            Modifier
                .basmaOlcegi(kaynak)
                .size(Olcu.MinDokunmaAlani)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .clickable(kaynak, null, role = Role.Button, onClick = onHesap)
                .semantics { contentDescription = "Hesap ve Google ile giriş" },
            contentAlignment = Alignment.Center,
        ) {
            Simge(Simgeler.HESAP, Renk.Metin, 20.dp, kalinlik = 1.6f)
        }
    }
}

/** Mavi nokta: 2,4 sn'de bir solup küçülür (tasarımdaki `.live`). */
@Composable
fun CanliNokta() {
    val azalt = LocalHareketAzalt.current
    val gecis = rememberInfiniteTransition(label = "canliNokta")
    val nabiz by gecis.animateFloat(
        initialValue = 1f,
        targetValue = if (azalt) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nabiz",
    )
    Box(
        Modifier
            .size(7.dp)
            .graphicsLayer {
                alpha = 0.35f + 0.65f * nabiz
                val o = 0.6f + 0.4f * nabiz
                scaleX = o
                scaleY = o
            }
            .drawBehind {
                drawCircle(Brush.radialGradient(listOf(Renk.Vurgu.copy(alpha = 0.6f), Color.Transparent), radius = size.minDimension * 1.7f), radius = size.minDimension * 1.7f)
                drawCircle(Renk.Vurgu)
            },
    )
}

/** Geist Mono 12, harf aralığı 0,08 em (üst etiketler). */
val UstEtiket = Yazi.EnKucukMono.copy(fontSize = 12.sp, letterSpacing = 0.08.em)

@Composable
fun BolumBasligi(metin: String, modifier: Modifier = Modifier) {
    Text(
        metin,
        style = Yazi.Kucuk.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.08.em),
        color = Renk.MetinEnSoluk,
        modifier = modifier.semantics { heading() },
    )
}

/**
 * Kilit ekranındaki anketin kartı: adı ve mum şeridinin küçük hali. Dokununca Ekran 4 açılır.
 * Duruma göre (Ekran 6): yayın yasağında şerit yerine kilitli panel, henüz oy yokken mumsuz logolar.
 */
@Composable
fun KilitEkraniKarti(
    anket: Anket,
    ogeler: List<SeritOgesi>,
    anketDurumu: AnketDurumu,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sekil = RoundedCornerShape(Kose.Kart)
    val kaynak = remember { MutableInteractionSource() }
    val gorunum = anketDurumu.gorunum
    val yasak = gorunum == SonucGorunumu.YAYIN_DURDURULDU
    Column(
        modifier
            .basmaOlcegi(kaynak)
            .fillMaxWidth()
            .shadow(28.dp, sekil, ambientColor = Color.Transparent, spotColor = (if (yasak) Renk.Asla else Renk.Vurgu).copy(alpha = 0.5f))
            .clip(sekil)
            .background(Renk.Zemin)
            .aciliGradyan(
                160f,
                if (yasak) listOf(Renk.Asla.copy(alpha = 0.10f), Color.White.copy(alpha = 0.025f)) else listOf(Color.White.copy(alpha = 0.09f), Color.White.copy(alpha = 0.025f)),
                sekil,
            )
            .border(1.dp, if (yasak) Renk.Asla.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.12f), sekil)
            .clickable(kaynak, null, role = Role.Button, onClickLabel = "İncele", onClick = onClick)
            .testTag("kilit-karti")
            .padding(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KilitEkraniSimgesi(dolu = !yasak, renk = if (yasak) Renk.Asla else Renk.Vurgu, boyut = 18.dp)
                Text("Kilit ekranında", style = Yazi.Kucuk, color = Renk.MetinSoluk)
            }
            DurumRozeti(anketDurumu)
        }
        Spacer(Modifier.height(10.dp))
        Text(anket.ad, style = Yazi.Baslik2, color = Renk.Metin)
        Spacer(Modifier.height(16.dp))
        if (yasak) {
            YasakPaneli(yukseklik = 86.dp)
        } else {
            MiniSerit(ogeler, anketDurumu.sembolikGosterim)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(
                when (gorunum) {
                    SonucGorunumu.YAYIN_DURDURULDU -> yenidenYayinMetni(anketDurumu.yayinYasagiBitis)
                    SonucGorunumu.SEMBOLIK -> "Gerçek oy yok · tüm partiler eşit"
                    SonucGorunumu.HENUZ_OY_YOK -> "Henüz oy yok · ilk oyla canlanır"
                    SonucGorunumu.CANLI -> "${anketDurumu.katilimciSayisi} katılımcı"
                },
                style = Yazi.Kucuk,
                color = if (yasak) Renk.Asla else Renk.MetinEnSoluk,
                modifier = Modifier.weight(1f),
            )
            Text("İncele →", style = Yazi.Kucuk, color = Renk.Vurgu)
        }
    }
}

/** Kilit ekranı şeridinin küçük, etkileşimsiz hali (yalnızca artı mumlar ve logolar). */
@Composable
fun MiniSerit(ogeler: List<SeritOgesi>, sembolik: Boolean, modifier: Modifier = Modifier) {
    val enYuksek = ogeler.maxOfOrNull { maxOf(it.sonuc.partiDestek ?: 0f, it.sonuc.liderDestek ?: 0f) }?.takeIf { it > 0f } ?: 1f
    // Henüz oy yokken (değer yok) mum çizilmez, yalnızca logolar.
    fun oran(deger: Float?): Float? = when {
        sembolik -> 0.7f
        deger == null -> null
        else -> (deger / enYuksek * 0.92f).coerceIn(0.08f, 1f)
    }
    Row(modifier.fillMaxWidth().semantics(mergeDescendants = true) {}, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ogeler.forEach { oge ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth().height(46.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                    oran(oge.sonuc.partiDestek)?.let { MiniMum(it, sol = true, gecikmeMs = 0) }
                    oran(oge.sonuc.liderDestek)?.let { MiniMum(it, sol = false, gecikmeMs = 50) }
                }
                LogoKutusu(oge.parti, acik = false)
            }
        }
    }
}

/** 8 dp genişlikte çizgili mum; ekrana girişte aşağıdan büyür (1,1 sn). */
@Composable
private fun MiniMum(oran: Float, sol: Boolean, gecikmeMs: Int) {
    val azalt = LocalHareketAzalt.current
    val buyume = remember { Animatable(if (azalt) 1f else 0.08f) }
    LaunchedEffect(Unit) {
        if (!azalt) buyume.animateTo(1f, tween(1100, gecikmeMs, Hareket.Giris))
    }
    val sekil = if (sol) {
        RoundedCornerShape(topStart = 4.dp, bottomStart = 2.dp)
    } else {
        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 2.dp)
    }
    val (acik, koyu) = if (sol) {
        Renk.MumParti.copy(alpha = 0.95f) to Renk.MumParti.copy(alpha = 0.6f)
    } else {
        Renk.MumLider.copy(alpha = 0.95f) to Renk.MumLider.copy(alpha = 0.5f)
    }
    Box(
        Modifier
            .width(8.dp)
            .fillMaxHeight(oran)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 1f)
                scaleY = buyume.value
            }
            .clip(sekil)
            .drawBehind { cizgiliDolgu(acik, koyu) },
    )
}

/** Bir anketin kartı: tür etiketi, ad, açıklama, zil, "kilit ekranına al" ve Katıl/Sonuçlar. */
@Composable
fun AnketKarti(
    anket: Anket,
    aciklama: String,
    katildi: Boolean,
    zilAcik: Boolean,
    kilitte: Boolean,
    onZil: () -> Unit,
    onKilit: () -> Unit,
    onEylem: () -> Unit,
    modifier: Modifier = Modifier,
    eylemMetni: String = if (katildi) "Sonuçlar" else "Katıl",
    /** Kartın kendisine dokununca: anket ayrıntısı. */
    onAc: (() -> Unit)? = null,
) {
    val sekil = RoundedCornerShape(Kose.ListeOgesi)
    val kaynak = remember { MutableInteractionSource() }
    val azalt = LocalHareketAzalt.current
    var yeniTakip by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxWidth()
            .basmaOlcegi(kaynak, 0.985f)
            .clip(sekil)
            .background(Renk.Yuzey)
            .border(1.dp, Color.White.copy(alpha = 0.07f), sekil)
            .then(if (onAc != null) Modifier.clickable(kaynak, null, onClickLabel = "Ayrıntılar", onClick = onAc) else Modifier)
            .testTag("anket-${anket.id}")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TurEtiketi(anket.tur.etiket)
                Text(anket.ad, style = Yazi.Baslik3.copy(letterSpacing = (-0.015).em), color = Renk.Metin)
                Text(aciklama, style = Yazi.Kucuk, color = Renk.MetinEnSoluk, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            ZilDugmesi(anket.ad, zilAcik, onZil)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KilitTakipDugmesi(
                takipte = kilitte,
                onTakipEt = {
                    onKilit()
                    yeniTakip = true
                },
                modifier = Modifier.weight(1f),
                etiket = "takip-${anket.id}",
            )
            EylemDugmesi(
                metin = eylemMetni,
                zemin = if (katildi) Renk.Metin else Renk.Vurgu,
                yazi = if (katildi) Renk.Zemin else Renk.VurguUstuMetin,
                onClick = onEylem,
                modifier = Modifier.weight(1f),
            )
        }
        // Takibe alınınca kısa bir doğrulama; 2,5 sn sonra kendiliğinden kapanır.
        AnimatedVisibility(
            visible = yeniTakip && kilitte,
            enter = if (azalt) EnterTransition.None else expandVertically(tween(Hareket.BasmaMs, easing = Hareket.Giris)) + fadeIn(tween(250)),
            exit = if (azalt) ExitTransition.None else shrinkVertically(tween(300, easing = Hareket.Giris)) + fadeOut(tween(200)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KilitEkraniSimgesi(true, Renk.VurguAcik, 16.dp)
                Text("Kilit ekranında artık bu anket görünecek", style = Yazi.Kucuk, color = Renk.VurguAcik)
            }
        }
        LaunchedEffect(yeniTakip) {
            if (yeniTakip) {
                delay(2500)
                yeniTakip = false
            }
        }
    }
}

@Composable
fun TurEtiketi(metin: String) {
    Text(
        metin,
        style = Yazi.EnKucukMono.copy(letterSpacing = 0.05.em),
        color = Renk.MetinSoluk,
        modifier = Modifier
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
internal fun ZilDugmesi(anketAdi: String, acik: Boolean, onClick: () -> Unit) {
    val sekil = RoundedCornerShape(15.dp)
    val kaynak = remember { MutableInteractionSource() }
    val zemin by animateColorAsState(
        if (acik) Renk.Vurgu.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f),
        gecis(Hareket.BasmaMs, Hareket.Yumusak), label = "zilZemin",
    )
    val renk by animateColorAsState(if (acik) Renk.Vurgu else Renk.MetinEnSoluk, gecis(Hareket.BasmaMs, Hareket.Yumusak), label = "zilRenk")
    Box(
        Modifier
            .basmaOlcegi(kaynak)
            .size(Olcu.MinDokunmaAlani)
            .clip(sekil)
            .background(zemin)
            .border(1.dp, Color.White.copy(alpha = 0.1f), sekil)
            .toggleable(acik, kaynak, null, role = Role.Switch, onValueChange = { onClick() })
            .semantics { contentDescription = if (acik) "$anketAdi bildirimleri açık, kapat" else "$anketAdi bildirimlerini aç" },
        contentAlignment = Alignment.Center,
    ) {
        Simge(if (acik) Simgeler.ZIL else Simgeler.ZIL_KAPALI, renk, 20.dp, kalinlik = 1.7f)
    }
}


@Composable
fun EylemDugmesi(metin: String, zemin: Color, yazi: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val sekil = RoundedCornerShape(Kose.Dugme)
    val kaynak = remember { MutableInteractionSource() }
    Box(
        modifier
            .basmaOlcegi(kaynak)
            .heightIn(min = Olcu.MinDokunmaAlani)
            .clip(sekil)
            .background(zemin)
            .clickable(kaynak, null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(metin, style = Yazi.Govde.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = yazi)
    }
}
