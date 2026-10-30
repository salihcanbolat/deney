package com.salihcanbolat.secimanketi.ekran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.bilesen.DurumKarti
import com.salihcanbolat.secimanketi.bilesen.DurumRozeti
import com.salihcanbolat.secimanketi.bilesen.DurumSimgeleri
import com.salihcanbolat.secimanketi.bilesen.HenuzOyYokPaneli
import com.salihcanbolat.secimanketi.bilesen.LogoKutusu
import com.salihcanbolat.secimanketi.bilesen.Simge
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.YasakPaneli
import com.salihcanbolat.secimanketi.bilesen.altBosluk
import com.salihcanbolat.secimanketi.bilesen.basmaOlcegi
import com.salihcanbolat.secimanketi.bilesen.belirme
import com.salihcanbolat.secimanketi.bilesen.cizgiliDolgu
import com.salihcanbolat.secimanketi.bilesen.rememberGiris
import com.salihcanbolat.secimanketi.bilesen.saatMetni
import com.salihcanbolat.secimanketi.bilesen.ustBosluk
import com.salihcanbolat.secimanketi.bilesen.yenidenYayinMetni
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.Oy
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import com.salihcanbolat.secimanketi.veri.SeritOgesi
import com.salihcanbolat.secimanketi.veri.SonucGorunumu
import com.salihcanbolat.secimanketi.veri.yuzde

/** Sonuç listesinde destek ya da "asla" değerleri. */
private enum class SonucTuru(val ad: String) { DESTEK("Destek"), ASLA("Asla") }

/**
 * Anket ayrıntısı: durum, katılım, tüm partilerin sonuç listesi ve (katıldıysan) bu cihazdaki oy
 * özetin. Tasarım paketinde bu ekran yok; Anketler ve Oy verme ekranlarının diliyle çizildi.
 */
@Composable
fun AnketAyrintiEkrani(
    anket: Anket,
    anketDurumu: AnketDurumu,
    oy: Oy?,
    zilAcik: Boolean,
    kilitte: Boolean,
    onGeri: () -> Unit,
    onZil: () -> Unit,
    onKilit: () -> Unit,
    onKatil: () -> Unit,
    onKilitGorunumu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gorunum = anketDurumu.gorunum
    val sembolik = anketDurumu.sembolikGosterim
    val turler = buildList {
        if (anket.tur.destekVar) add(SonucTuru.DESTEK)
        if (anket.tur.aslaVar) add(SonucTuru.ASLA)
    }
    var tur by rememberSaveable { mutableStateOf(turler.first()) }
    val siralama = remember(anket, anketDurumu) { SembolikVeri.siralama(anket, anketDurumu) }
    val sonucVar = gorunum == SonucGorunumu.SEMBOLIK || gorunum == SonucGorunumu.CANLI

    val giris0 = rememberGiris(0)
    val giris1 = rememberGiris(60)
    val giris2 = rememberGiris(140)
    // Çubuklar ekrana girişte bir kez dolar; listede kaydırınca tekrar oynamaz.
    val cubukGirisi = rememberGiris(220, 900)

    Box(modifier.fillMaxSize().background(Renk.Zemin)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("ayrinti-listesi"),
            contentPadding = PaddingValues(
                start = Olcu.EkranKenarBoslugu,
                end = Olcu.EkranKenarBoslugu,
                top = ustBosluk(56.dp),
                bottom = 132.dp + (altBosluk(34.dp) - 34.dp),
            ),
        ) {
            item(key = "ust") { UstSatir(anket, onGeri, Modifier.belirme(giris0)) }
            item(key = "bilgi") {
                BilgiKarti(anket, anketDurumu, zilAcik, kilitte, onZil, onKilit, Modifier.padding(top = 22.dp).belirme(giris1))
            }
            item(key = "sonuclar-baslik") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 12.dp).belirme(giris2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BolumBasligi("SONUÇLAR", Modifier.weight(1f))
                    if (turler.size > 1 && sonucVar) {
                        TurSecici(turler, tur, onSec = { tur = it })
                    }
                }
            }
            when (gorunum) {
                SonucGorunumu.YAYIN_DURDURULDU -> item(key = "yasak") {
                    DurumKarti(
                        simge = DurumSimgeleri.YAYIN_DURDURULDU,
                        simgeRengi = Renk.Asla,
                        simgeZemini = Renk.Asla.copy(alpha = 0.16f),
                        baslik = "Yayın durduruldu",
                        aciklama = "Seçim öncesi yayın yasağı nedeniyle sonuçlar gizlendi. Oy vermeye devam edebilirsin.",
                        asla = true,
                        modifier = Modifier.belirme(giris2),
                    ) {
                        YasakPaneli()
                        Text(yenidenYayinMetni(anketDurumu.yayinYasagiBitis), style = Yazi.Kucuk, color = Renk.Asla)
                    }
                }
                SonucGorunumu.HENUZ_OY_YOK -> item(key = "henuz") {
                    DurumKarti(
                        simge = DurumSimgeleri.HENUZ_OY_YOK,
                        simgeRengi = Renk.Vurgu,
                        simgeZemini = Renk.Vurgu.copy(alpha = 0.12f),
                        baslik = "Henüz oy yok",
                        aciklama = "Anket açıldı ama ilk oy henüz gelmedi. Sonuçlar ilk oyla birlikte canlanır.",
                        modifier = Modifier.belirme(giris2),
                    ) {
                        HenuzOyYokPaneli()
                    }
                }
                else -> {
                    item(key = "lejant") { Lejant(tur, sembolik, Modifier.padding(bottom = 10.dp).belirme(giris2)) }
                    itemsIndexed(siralama, key = { _, oge -> "sonuc-${oge.parti.id}" }) { i, oge ->
                        SonucSatiri(
                            sira = i + 1,
                            oge = oge,
                            tur = tur,
                            sembolik = sembolik,
                            enYuksek = enYuksekDeger(siralama, tur),
                            ilk = i == 0,
                            son = i == siralama.lastIndex,
                            cubukGirisi = cubukGirisi,
                            modifier = Modifier.belirme(giris2),
                        )
                    }
                }
            }
            if (oy != null) {
                item(key = "oyun-baslik") { BolumBasligi("SENİN OYUN", Modifier.padding(top = 26.dp, bottom = 12.dp)) }
                item(key = "oyun") {
                    if (oy.ozetSilindi) {
                        Text(
                            "Bu ankette oy kullandın. Kime oy verdiğinin özeti bu cihazdan silindi.",
                            style = Yazi.Kucuk.copy(lineHeight = 1.45.em),
                            color = Renk.MetinEnSoluk,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Kose.ListeOgesi))
                                .background(Renk.Yuzey)
                                .padding(16.dp),
                        )
                    } else {
                        OyOzeti(anket, oy)
                    }
                }
                item(key = "oyun-not") {
                    Row(Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Simge(Simgeler.KALKAN, Renk.MetinEnSoluk, 14.dp, Modifier.padding(top = 1.dp))
                        Text(
                            "Bu özet yalnızca bu cihazda saklanır. Kilit ekranında senin tercihin hiçbir zaman gösterilmez.",
                            style = Yazi.Etiket12.copy(lineHeight = 1.45.em),
                            color = Renk.MetinEnSoluk,
                        )
                    }
                }
            }
        }

        // Alt düğme: katılmadıysan "Katıl", katıldıysan kilit ekranı görünümü.
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(0f to Color(0x00070B1C), 0.3f to Renk.Zemin))
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = altBosluk(34.dp)),
        ) {
            if (oy == null) {
                val ilkOy = gorunum == SonucGorunumu.HENUZ_OY_YOK
                EylemDugmesi(
                    metin = if (ilkOy) "İlk oyu ver" else "Katıl",
                    zemin = Renk.Vurgu,
                    yazi = Renk.VurguUstuMetin,
                    onClick = onKatil,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("ayrinti-katil"),
                )
            } else {
                AnaDugme("Kilit ekranı görünümünde aç", onKilitGorunumu)
            }
        }
    }
}

/** Durum rozeti, katılım ve son güncelleme; altında "kilit ekranına al" ve zil. */
@Composable
private fun BilgiKarti(
    anket: Anket,
    durum: AnketDurumu,
    zilAcik: Boolean,
    kilitte: Boolean,
    onZil: () -> Unit,
    onKilit: () -> Unit,
    modifier: Modifier,
) {
    val sekil = RoundedCornerShape(Kose.ListeOgesi)
    val katilim = when (durum.gorunum) {
        SonucGorunumu.CANLI -> "${durum.katilimciSayisi} katılımcı"
        SonucGorunumu.HENUZ_OY_YOK -> "0 katılımcı"
        // Sembolik ve yasakta gerçek sayı gösterilmez.
        else -> "— katılımcı"
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Renk.Yuzey)
            .border(1.dp, Color.White.copy(alpha = 0.07f), sekil)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TurEtiketi(anket.tur.etiket)
            DurumRozeti(durum)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(katilim, style = Yazi.BuyukDeger.copy(fontSize = 26.sp, letterSpacing = (-0.02).em), color = Renk.Metin)
                Text(
                    durum.sonGuncelleme?.let { "Son güncelleme ${saatMetni(it)}" } ?: anket.tur.aciklama,
                    style = Yazi.Kucuk,
                    color = Renk.MetinEnSoluk,
                )
            }
            ZilDugmesi(anket.ad, zilAcik, onZil)
        }
        KilitDugmesi(kilitte, onKilit, Modifier.fillMaxWidth())
    }
}

/** Destek / Asla seçici (yalnızca "Destek + Asla" anketlerinde). */
@Composable
private fun TurSecici(turler: List<SonucTuru>, secili: SonucTuru, onSec: (SonucTuru) -> Unit) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .padding(3.dp)
            .selectableGroup(),
    ) {
        turler.forEach { t ->
            val acik = t == secili
            val spec = gecis<Color>(Hareket.BasmaMs, Hareket.Yumusak)
            val zemin by animateColorAsState(
                when {
                    !acik -> Color.Transparent
                    t == SonucTuru.ASLA -> Renk.Asla.copy(alpha = 0.16f)
                    else -> Color.White.copy(alpha = 0.1f)
                },
                spec, label = "turZemin",
            )
            val renk by animateColorAsState(
                when {
                    !acik -> Renk.MetinEnSoluk
                    t == SonucTuru.ASLA -> Renk.Asla
                    else -> Renk.Metin
                },
                spec, label = "turRenk",
            )
            val kaynak = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .basmaOlcegi(kaynak)
                    .heightIn(min = 40.dp)
                    .clip(CircleShape)
                    .background(zemin)
                    .selectable(acik, kaynak, null, role = Role.Tab, onClick = { onSec(t) })
                    .testTag("tur-${t.name.lowercase()}")
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(t.ad, style = Yazi.Kucuk.copy(fontWeight = FontWeight.SemiBold), color = renk)
            }
        }
    }
}

@Composable
private fun Lejant(tur: SonucTuru, sembolik: Boolean, modifier: Modifier) {
    val (parti, lider) = renkler(tur)
    Row(modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LejantOgesi(if (tur == SonucTuru.ASLA) "Partiye asla" else "Parti", parti)
        LejantOgesi(if (tur == SonucTuru.ASLA) "Lidere asla" else "Lider", lider)
        if (sembolik) Text("Sembolik · eşit", style = Yazi.EnKucuk.copy(fontWeight = FontWeight.Normal), color = Renk.MetinEnSoluk)
    }
}

@Composable
private fun LejantOgesi(ad: String, renk: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(width = 6.dp, height = 9.dp).clip(RoundedCornerShape(2.dp)).background(renk))
        Text(ad, style = Yazi.EnKucuk.copy(fontWeight = FontWeight.Normal), color = Renk.MetinEnSoluk)
    }
}

private fun renkler(tur: SonucTuru): Pair<Color, Color> = when (tur) {
    SonucTuru.DESTEK -> Renk.MumParti to Renk.MumLider
    SonucTuru.ASLA -> Renk.Asla to Renk.Asla.copy(alpha = 0.6f)
}

private fun degerler(oge: SeritOgesi, tur: SonucTuru): Pair<Float?, Float?> = when (tur) {
    SonucTuru.DESTEK -> oge.sonuc.partiDestek to oge.sonuc.liderDestek
    SonucTuru.ASLA -> oge.sonuc.partiyeAsla to oge.sonuc.lidereAsla
}

/** Ortak ölçek: partiler birbiriyle karşılaştırılabilsin diye en yüksek değere göre. */
private fun enYuksekDeger(siralama: List<SeritOgesi>, tur: SonucTuru): Float =
    siralama.maxOfOrNull { oge -> degerler(oge, tur).let { maxOf(it.first ?: 0f, it.second ?: 0f) } }?.takeIf { it > 0f } ?: 1f

/**
 * Bir partinin satırı: sıra, logo, ad ve lider; sağda parti ve lider için iki ince çubuk ve
 * yüzdeleri. Sembolik veride çubuklar eşit, yüzdeler "%—".
 */
@Composable
private fun SonucSatiri(
    sira: Int,
    oge: SeritOgesi,
    tur: SonucTuru,
    sembolik: Boolean,
    enYuksek: Float,
    ilk: Boolean,
    son: Boolean,
    cubukGirisi: State<Float>,
    modifier: Modifier,
) {
    val r = Kose.ListeOgesi
    val sekil = RoundedCornerShape(topStart = if (ilk) r else 0.dp, topEnd = if (ilk) r else 0.dp, bottomStart = if (son) r else 0.dp, bottomEnd = if (son) r else 0.dp)
    val (partiDeger, liderDeger) = degerler(oge, tur)
    val (partiRengi, liderRengi) = renkler(tur)
    fun oran(d: Float?) = when {
        sembolik -> 0.5f
        d == null -> 0f
        else -> (d / enYuksek).coerceIn(0f, 1f)
    }
    val etiket = buildString {
        append("$sira. sıra, ${oge.parti.kisaAd}, lider ${oge.parti.liderler.joinToString(" ve ") { it.ad }}. ")
        if (sembolik) {
            append("Sembolik veri.")
        } else {
            val ad = if (tur == SonucTuru.ASLA) "asla" else "destek"
            append("Parti $ad ${yuzde(partiDeger, false)}, lider $ad ${yuzde(liderDeger, false)}.")
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Renk.Yuzey)
            .drawBehind {
                if (!son) drawLine(Color.White.copy(alpha = 0.06f), Offset(16.dp.toPx(), size.height), Offset(size.width - 16.dp.toPx(), size.height), 1.dp.toPx())
            }
            .semantics(mergeDescendants = true) { contentDescription = etiket }
            .heightIn(min = 64.dp)
            .padding(start = 12.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "$sira",
            style = Yazi.EnKucukMono.copy(fontSize = 12.sp, textAlign = TextAlign.End),
            color = Renk.MetinEnSoluk,
            modifier = Modifier.width(18.dp),
        )
        LogoKutusu(oge.parti, acik = false)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(oge.parti.kisaAd, style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Metin, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(oge.parti.liderAdlari, style = Yazi.Etiket12, color = Renk.MetinEnSoluk, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(Modifier.width(104.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Cubuk(oran(partiDeger), partiRengi, yuzde(partiDeger, sembolik), cubukGirisi)
            Cubuk(oran(liderDeger), liderRengi, yuzde(liderDeger, sembolik), cubukGirisi)
        }
    }
}

/** İnce, çizgili çubuk ve sağında yüzde. Doluluk değişince yayla akar. */
@Composable
private fun Cubuk(oran: Float, renk: Color, metin: String, giris: State<Float>) {
    val hedef by animateFloatAsState(oran, gecis(700, Hareket.YayBuyume), label = "cubuk")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.06f)),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((hedef * giris.value).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .drawBehind { cizgiliDolgu(renk.copy(alpha = renk.alpha * 0.95f), renk.copy(alpha = renk.alpha * 0.55f)) },
            )
        }
        Text(metin, style = Yazi.EnKucukMono.copy(textAlign = TextAlign.End), color = Renk.MetinIkincil, modifier = Modifier.width(40.dp))
    }
}
