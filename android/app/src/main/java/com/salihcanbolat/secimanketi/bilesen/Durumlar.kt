package com.salihcanbolat.secimanketi.bilesen

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.SonucGorunumu
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Turkce = Locale.forLanguageTag("tr")

/** Ekran 6'daki durum simgeleri (24×24). */
object DurumSimgeleri {
    const val SEMBOLIK = "M5 20V12M12 20V12M19 20V12M3 8h18"
    const val HENUZ_OY_YOK = "M7 9h10a3 3 0 0 1 3 3v5a3 3 0 0 1 -3 3h-10a3 3 0 0 1 -3 -3v-5a3 3 0 0 1 3 -3zM9 9V6h6v3M12 13v3"
    const val YAYIN_DURDURULDU = "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0M9 9v6M15 9v6"
    const val BAGLANTI_YOK = "M2.5 9a14 14 0 0 1 19 0M6 12.5a9 9 0 0 1 12 0M9.5 16a4 4 0 0 1 5 0M3 3l18 18"
    const val GIRIS = "M12 3l7 3v5c0 4.5-3 8-7 10-4-2-7-5.5-7-10V6zM9 12l2 2 4-4"
    const val YENILE = "M20 12a8 8 0 1 1 -2.3 -5.6M20 4v4h-4"
}

/** "21:40" */
fun saatMetni(ms: Long): String =
    DateTimeFormatter.ofPattern("HH:mm", Turkce).format(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()))

/** "Yeniden yayın: 3 Kasım, 21:00"; tarih bilinmiyorsa açıklanacağı söylenir. */
fun yenidenYayinMetni(bitis: Long?): String = if (bitis == null) {
    "Yeniden yayın tarihi açıklanacak"
} else {
    "Yeniden yayın: " + DateTimeFormatter.ofPattern("d MMMM, HH:mm", Turkce).format(Instant.ofEpochMilli(bitis).atZone(ZoneId.systemDefault()))
}

/** "ÇEVRİMDIŞI · 21:40" */
fun cevrimdisiEtiketi(durum: AnketDurumu): String =
    "ÇEVRİMDIŞI" + (durum.sonGuncelleme?.let { " · ${saatMetni(it)}" } ?: "")

/**
 * Kartın sağ üstündeki durum rozeti. Bağlantı yoksa çevrimdışı rozeti önce gelir; canlı veride
 * rozet yok.
 */
@Composable
fun DurumRozeti(durum: AnketDurumu, modifier: Modifier = Modifier) {
    val (metin, yazi, zemin) = when {
        durum.gorunum == SonucGorunumu.YAYIN_DURDURULDU -> Triple("YAYIN DURDURULDU", Renk.Asla, Renk.Asla.copy(alpha = 0.16f))
        durum.cevrimdisi -> Triple(cevrimdisiEtiketi(durum), Renk.MetinIkincil, Color.White.copy(alpha = 0.08f))
        durum.gorunum == SonucGorunumu.SEMBOLIK -> Triple("SEMBOLİK", Renk.Zemin, Renk.Metin)
        durum.gorunum == SonucGorunumu.HENUZ_OY_YOK -> Triple("HENÜZ OY YOK", Renk.VurguAcik, Renk.Vurgu.copy(alpha = 0.14f))
        else -> return
    }
    Text(
        metin,
        style = Yazi.EnKucukMono.copy(letterSpacing = 0.06.em),
        color = yazi,
        maxLines = 1,
        modifier = modifier.clip(CircleShape).background(zemin).padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/**
 * Yayın durduruldu: bulanık, solup parlayan çubuklar ve ortada turuncu kilit. Çubuklar sonuç
 * değildir, hepsi aynı boyda; logolar da gösterilmez (sıraları sonucu ele verir).
 */
@Composable
fun YasakPaneli(modifier: Modifier = Modifier, yukseklik: Dp = 110.dp) {
    val azalt = LocalHareketAzalt.current
    val parilti by rememberInfiniteTransition(label = "yasak").animateFloat(
        initialValue = if (azalt) 0.55f else 0.35f,
        targetValue = if (azalt) 0.55f else 0.8f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "yasakParilti",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(yukseklik)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x80040714))
            .testTag("yasak-paneli"),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(14.dp)
                // Android 12 öncesinde bulanıklık yok; çubuklar yalnızca soluk kalır.
                .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(6.dp) else Modifier)
                .graphicsLayer { alpha = parilti },
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            repeat(8) {
                Box(Modifier.weight(1f).fillMaxHeight(0.7f).clip(RoundedCornerShape(5.dp)).background(Color(0xFF2A3560)))
            }
        }
        Simge(Simgeler.KILIT, Renk.Asla, 28.dp, Modifier.align(Alignment.Center))
    }
}

/** Henüz oy yok: kesik çizgili kutuda büyük "0" ve "katılımcı". */
@Composable
fun HenuzOyYokPaneli(modifier: Modifier = Modifier, yukseklik: Dp = 110.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(yukseklik)
            .kesikKenar(Color.White.copy(alpha = 0.16f), 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("0", style = Yazi.BuyukDeger.copy(fontSize = 36.sp, letterSpacing = (-0.03).em), color = Renk.Metin)
            Text("katılımcı", style = Yazi.Etiket12, color = Renk.MetinEnSoluk)
        }
    }
}

/** Bağlantı yok: son güncelleme saati ve oyun bekletildiği bilgisi. */
@Composable
fun CevrimdisiPaneli(sonGuncelleme: Long?, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x80040714))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Son güncelleme", style = Yazi.Etiket12, color = Renk.MetinEnSoluk)
        Text(sonGuncelleme?.let(::saatMetni) ?: "—", style = Yazi.BuyukDeger.copy(fontSize = 30.sp, letterSpacing = (-0.03).em), color = Renk.Metin)
        Text("Oyun gönderilmeye hazır bekletilir", style = Yazi.Etiket12, color = Renk.MetinEnSoluk)
    }
}

/** Kenarlıklı "Yeniden dene"; simgesi 2,8 sn'de bir döner. */
@Composable
fun YenidenDeneDugmesi(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val sekil = RoundedCornerShape(Kose.Dugme)
    val kaynak = remember { MutableInteractionSource() }
    val azalt = LocalHareketAzalt.current
    val aci by rememberInfiniteTransition(label = "yenile").animateFloat(
        initialValue = 0f,
        targetValue = if (azalt) 0f else 360f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "yenileAci",
    )
    Row(
        modifier
            .fillMaxWidth()
            .basmaOlcegi(kaynak, 0.97f)
            .heightIn(min = Olcu.MinDokunmaAlani)
            .clip(sekil)
            .border(1.dp, Color.White.copy(alpha = 0.14f), sekil)
            .clickable(kaynak, null, role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Simge(DurumSimgeleri.YENILE, Renk.Metin, 16.dp, Modifier.graphicsLayer { rotationZ = aci }, kalinlik = 2f)
        Text("Yeniden dene", style = Yazi.Govde.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = Renk.Metin)
    }
}

/**
 * Ekran 6'daki durum kartı: simge kutusu, başlık, açıklama ve içerik. [asla] true ise (yayın
 * durduruldu) turuncu kenar ve üstten turuncu bir ışık.
 */
@Composable
fun DurumKarti(
    simge: String,
    simgeRengi: Color,
    simgeZemini: Color,
    baslik: String,
    aciklama: String,
    modifier: Modifier = Modifier,
    asla: Boolean = false,
    icerik: @Composable ColumnScope.() -> Unit = {},
) {
    val sekil = RoundedCornerShape(Kose.Kart)
    Column(
        modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Renk.Yuzey)
            .then(if (asla) Modifier.background(Brush.verticalGradient(0f to Renk.Asla.copy(alpha = 0.10f), 0.45f to Color.Transparent)) else Modifier)
            .border(1.dp, if (asla) Renk.Asla.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.07f), sekil)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(simgeZemini), contentAlignment = Alignment.Center) {
            Simge(simge, simgeRengi, 22.dp)
        }
        Text(baslik, style = Yazi.Baslik3.copy(fontSize = 20.sp, letterSpacing = (-0.01).em), color = Renk.Metin, modifier = Modifier.semantics { heading() })
        Text(aciklama, style = Yazi.Govde.copy(fontSize = 14.sp, lineHeight = 1.5.em), color = Renk.MetinSoluk)
        icerik()
    }
}

/** Bağlantı yok kartı (Anketler ekranının üstünde). */
@Composable
fun BaglantiYokKarti(durum: AnketDurumu, onYenidenDene: () -> Unit, modifier: Modifier = Modifier) {
    DurumKarti(
        simge = DurumSimgeleri.BAGLANTI_YOK,
        simgeRengi = Renk.MetinIkincil,
        simgeZemini = Color.White.copy(alpha = 0.08f),
        baslik = "Bağlantı yok",
        aciklama = "Son bilinen sonuçlar gösteriliyor. Bağlantı gelince kendiliğinden güncellenir.",
        modifier = modifier.testTag("baglanti-yok"),
    ) {
        CevrimdisiPaneli(durum.sonGuncelleme)
        YenidenDeneDugmesi(onYenidenDene)
    }
}

/**
 * Giriş gerekli: oy vermek için Google ile giriş. Göz atmak için giriş gerekmez.
 * @param bilgi Giriş düğmesinin altında gösterilecek kısa not (ör. girişin henüz bağlanmadığı).
 */
@Composable
fun GirisGerekliIcerigi(onGiris: () -> Unit, onGozAt: () -> Unit, modifier: Modifier = Modifier, bilgi: String? = null) {
    DurumKarti(
        simge = DurumSimgeleri.GIRIS,
        simgeRengi = Renk.MetinIkincil,
        simgeZemini = Color.White.copy(alpha = 0.08f),
        baslik = "Giriş gerekli",
        aciklama = "Her kişi bir kez oy verebilsin diye giriş istenir. Kime oy verdiğin cihazından çıkmaz.",
        modifier = modifier.testTag("giris-gerekli"),
    ) {
        val sekil = RoundedCornerShape(16.dp)
        val kaynak = remember { MutableInteractionSource() }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .basmaOlcegi(kaynak, 0.97f)
                .heightIn(min = 52.dp)
                .clip(sekil)
                .background(Renk.Metin)
                .clickable(kaynak, null, role = Role.Button, onClick = onGiris),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Simge(Simgeler.GONDER, Renk.Zemin, 16.dp, kalinlik = 2f)
            Text("Google ile giriş yap", style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Zemin)
        }
        if (bilgi != null) {
            Text(bilgi, style = Yazi.Etiket12, color = Renk.MetinEnSoluk, modifier = Modifier.fillMaxWidth().pop())
        }
        val kaynak2 = remember { MutableInteractionSource() }
        Box(
            Modifier
                .fillMaxWidth()
                .basmaOlcegi(kaynak2, 0.97f)
                .heightIn(min = Olcu.MinDokunmaAlani)
                .clip(RoundedCornerShape(Kose.Dugme))
                .clickable(kaynak2, null, role = Role.Button, onClick = onGozAt),
            contentAlignment = Alignment.Center,
        ) {
            Text("Göz atmaya devam et", style = Yazi.Govde.copy(fontSize = 14.sp), color = Renk.MetinIkincil)
        }
    }
}
