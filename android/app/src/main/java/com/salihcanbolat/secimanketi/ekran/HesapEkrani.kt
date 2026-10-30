package com.salihcanbolat.secimanketi.ekran

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.salihcanbolat.secimanketi.bildirim.CanliAnketBildirimi
import com.salihcanbolat.secimanketi.bilesen.Simge
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.altBosluk
import com.salihcanbolat.secimanketi.bilesen.basmaOlcegi
import com.salihcanbolat.secimanketi.bilesen.belirme
import com.salihcanbolat.secimanketi.bilesen.pop
import com.salihcanbolat.secimanketi.bilesen.ustBosluk
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.Kose
import com.salihcanbolat.secimanketi.tema.LocalHareketAzalt
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.tema.Yazi
import com.salihcanbolat.secimanketi.tema.gecis
import com.salihcanbolat.secimanketi.veri.UygulamaDurumu

/**
 * Hesap ve ayarlar: giriş durumu, kilit ekranı bildirimi, gizlilik ve cihazdaki oy özetlerini
 * silme, hakkında. Tasarım paketinde bu ekran yok; uygulamanın diğer ekranlarının diliyle çizildi.
 */
@Composable
fun HesapEkrani(
    durum: UygulamaDurumu,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var girisBilgisi by remember { mutableStateOf<String?>(null) }
    val izinVar = bildirimIzniDurumu(context)

    Box(modifier.fillMaxSize().background(Renk.Zemin)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = Olcu.EkranKenarBoslugu, end = Olcu.EkranKenarBoslugu, top = ustBosluk(56.dp), bottom = altBosluk(34.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GeriBasligi("HESAP VE AYARLAR", "Hesap", onGeri, Modifier.belirme(0))

            HesapKarti(
                bilgi = girisBilgisi,
                onGiris = { girisBilgisi = "Google ile giriş bu sürümde henüz yok; sunucu bağlanınca açılacak." },
                modifier = Modifier.padding(top = 14.dp).belirme(60),
            )

            BolumBasligi("KİLİT EKRANI", Modifier.padding(top = 14.dp).belirme(120))
            AyarKarti(Modifier.belirme(120)) {
                AyarAnahtari(
                    baslik = "Kilit ekranında canlı şerit",
                    aciklama = "Seçili anket kilit ekranında sessiz, kalıcı bir bildirimle görünür.",
                    acik = durum.bildirimAcik,
                    onDegistir = durum::bildirimiAc,
                    etiket = "kilit-bildirimi",
                )
                AyarAyirici()
                AyarSatiri("Kilit ekranındaki anket", durum.kilitAnketi.ad)
                if (!izinVar) {
                    AyarAyirici()
                    IzinSatiri { bildirimAyarlariniAc(context) }
                }
            }

            BolumBasligi("GİZLİLİK", Modifier.padding(top = 14.dp).belirme(180))
            AyarKarti(Modifier.belirme(180)) {
                BilgiSatiri(Simgeler.KALKAN, "Oyun anonim", "Kime oy verdiğin hesabınla ilişkilendirilmez; yalnızca bu ankette oy kullandığın bilinir.")
                AyarAyirici()
                BilgiSatiri(Simgeler.TELEFON, "Tercihin bu cihazda", "Oy özetin yalnızca bu telefonda saklanır, hiçbir yere gönderilmez.")
                AyarAyirici()
                BilgiSatiri(Simgeler.KILIT, "Kilit ekranı herkese açık", "Kilit ekranında ve bildirimde senin tercihin hiçbir zaman gösterilmez.")
            }
            OzetSilme(
                ozetVar = durum.ozetVarMi,
                onSil = durum::oyOzetleriniSil,
                modifier = Modifier.belirme(180),
            )

            BolumBasligi("HAKKINDA", Modifier.padding(top = 14.dp).belirme(240))
            AyarKarti(Modifier.belirme(240)) {
                AyarSatiri("Sürüm", surum(context))
                AyarAyirici()
                AyarSatiri("Rakamlar", "Sembolik · tüm partiler eşit")
                AyarAyirici()
                AyarSatiri("Partiler", "Meclis sandalye sırasıyla")
            }
            Text(
                "Gerçek oylar gelene kadar hiçbir parti önde ya da geride gösterilmez.",
                style = Yazi.Etiket12.copy(lineHeight = 1.45.em),
                color = Renk.MetinEnSoluk,
                modifier = Modifier.padding(horizontal = 4.dp).belirme(240),
            )
        }
    }
}

/** Geri düğmesi, küçük üst etiket ve başlık (Oy verme ekranındaki üst satırla aynı). */
@Composable
fun GeriBasligi(ust: String, baslik: String, onGeri: () -> Unit, modifier: Modifier = Modifier) {
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
                .semantics { contentDescription = "Geri" },
            contentAlignment = Alignment.Center,
        ) {
            Simge(Simgeler.GERI, Renk.Metin, 18.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(ust, style = Yazi.EnKucukMono.copy(letterSpacing = 0.08.em), color = Renk.MetinEnSoluk)
            Text(baslik, style = Yazi.Baslik2, color = Renk.Metin, modifier = Modifier.semantics { heading() })
        }
    }
}

/** Misafir hesabı ve Google ile giriş. */
@Composable
private fun HesapKarti(bilgi: String?, onGiris: () -> Unit, modifier: Modifier) {
    val sekil = RoundedCornerShape(Kose.Kart)
    Column(
        modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Renk.Yuzey)
            .border(1.dp, Color.White.copy(alpha = 0.07f), sekil)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Simge(Simgeler.HESAP, Renk.MetinIkincil, 24.dp, kalinlik = 1.6f)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Misafir", style = Yazi.Baslik3, color = Renk.Metin)
                Text("Göz atmak için giriş gerekmez.", style = Yazi.Kucuk, color = Renk.MetinEnSoluk)
            }
        }
        Text(
            "Oy vermek için Google ile giriş istenir; böylece her kişi bir ankette bir kez oy verir. Kime oy verdiğin cihazından çıkmaz.",
            style = Yazi.Govde.copy(fontSize = 14.sp, lineHeight = 1.5.em),
            color = Renk.MetinSoluk,
        )
        val kaynak = remember { MutableInteractionSource() }
        Row(
            Modifier
                .fillMaxWidth()
                .basmaOlcegi(kaynak, 0.97f)
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Renk.Metin)
                .clickable(kaynak, null, role = Role.Button, onClick = onGiris)
                .testTag("hesap-giris"),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Simge(Simgeler.GONDER, Renk.Zemin, 16.dp, kalinlik = 2f)
            Text("Google ile giriş yap", style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Zemin)
        }
        if (bilgi != null) {
            Text(bilgi, style = Yazi.Etiket12, color = Renk.MetinEnSoluk, modifier = Modifier.pop())
        }
    }
}

@Composable
private fun AyarKarti(modifier: Modifier = Modifier, icerik: @Composable ColumnScope.() -> Unit) {
    val sekil = RoundedCornerShape(Kose.ListeOgesi)
    Column(
        modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(Renk.Yuzey)
            .border(1.dp, Color.White.copy(alpha = 0.07f), sekil)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        content = icerik,
    )
}

@Composable
private fun AyarAyirici() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
}

@Composable
private fun AyarSatiri(baslik: String, deger: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(baslik, style = Yazi.Govde.copy(fontSize = 14.sp), color = Renk.MetinEnSoluk)
        Text(deger, style = Yazi.Govde.copy(textAlign = TextAlign.End), color = Renk.Metin, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BilgiSatiri(simge: String, baslik: String, aciklama: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 14.dp).semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Renk.Vurgu.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Simge(simge, Renk.Vurgu, 18.dp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(baslik, style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Metin)
            Text(aciklama, style = Yazi.Kucuk.copy(lineHeight = 1.45.em), color = Renk.MetinEnSoluk)
        }
    }
}

/** Ayar anahtarı (Ekran 3'teki "Kilit ekranında takip et" ile aynı anahtar). */
@Composable
private fun AyarAnahtari(baslik: String, aciklama: String, acik: Boolean, onDegistir: (Boolean) -> Unit, etiket: String) {
    val kaynak = remember { MutableInteractionSource() }
    val iz by animateColorAsState(if (acik) Renk.Vurgu else Color.White.copy(alpha = 0.16f), gecis(Hareket.BasmaMs, Hareket.Yumusak), label = "ayarIz")
    val topuz by animateDpAsState(if (acik) 20.dp else 0.dp, gecis(450, Hareket.Basma), label = "ayarTopuz")
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(acik, kaynak, null, role = Role.Switch, onValueChange = onDegistir)
            .testTag(etiket)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(baslik, style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Metin)
            Text(aciklama, style = Yazi.Kucuk.copy(lineHeight = 1.4.em), color = Renk.MetinEnSoluk)
        }
        Box(Modifier.size(width = 52.dp, height = 32.dp).clip(RoundedCornerShape(16.dp)).background(iz)) {
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

/** Bildirim izni kapalıysa uyarı ve sistem ayarlarına giden bağlantı. */
@Composable
private fun IzinSatiri(onAc: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClickLabel = "Bildirim ayarlarını aç", onClick = onAc)
            .testTag("izin-satiri"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Bildirim izni kapalı", style = Yazi.Govde.copy(fontWeight = FontWeight.SemiBold), color = Renk.Asla)
            Text("İzin olmadan şerit kilit ekranında görünmez.", style = Yazi.Kucuk, color = Renk.MetinEnSoluk)
        }
        Text("Aç →", style = Yazi.Govde.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = Renk.Asla)
    }
}

/**
 * "Oy özetlerini bu cihazdan sil": iki adımlı (önce onay). Katılım bilgisi kalır; bir ankette tek
 * oy kuralı sürer.
 */
@Composable
private fun OzetSilme(ozetVar: Boolean, onSil: () -> Unit, modifier: Modifier) {
    var onay by remember { mutableStateOf(false) }
    val azalt = LocalHareketAzalt.current
    AnimatedContent(
        targetState = when {
            !ozetVar -> 2
            onay -> 1
            else -> 0
        },
        transitionSpec = { if (azalt) fadeIn(tween(0)) togetherWith fadeOut(tween(0)) else fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
        modifier = modifier.fillMaxWidth(),
        label = "ozetSilme",
    ) { adim ->
        val sekil = RoundedCornerShape(Kose.Dugme)
        when (adim) {
            0 -> {
                val kaynak = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .basmaOlcegi(kaynak, 0.97f)
                        .heightIn(min = Olcu.MinDokunmaAlani)
                        .clip(sekil)
                        .border(1.dp, Renk.Asla.copy(alpha = 0.4f), sekil)
                        .clickable(kaynak, null, role = Role.Button) { onay = true }
                        .testTag("ozet-sil"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Oy özetlerini bu cihazdan sil", style = Yazi.Govde.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = Renk.Asla)
                }
            }
            1 -> Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Kose.ListeOgesi))
                    .background(Renk.Asla.copy(alpha = 0.08f))
                    .border(1.dp, Renk.Asla.copy(alpha = 0.3f), RoundedCornerShape(Kose.ListeOgesi))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Kime oy verdiğinin özeti bu cihazdan silinir. Oy kullandığın anketlerde tekrar oy veremezsin.",
                    style = Yazi.Kucuk.copy(lineHeight = 1.45.em),
                    color = Renk.MetinSoluk,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EylemDugmesi("Vazgeç", Color.White.copy(alpha = 0.06f), Renk.Metin, { onay = false }, Modifier.weight(1f))
                    EylemDugmesi(
                        "Sil",
                        Renk.Asla,
                        Renk.VurguUstuMetin,
                        {
                            onSil()
                            onay = false
                        },
                        Modifier.weight(1f).testTag("ozet-sil-onay"),
                    )
                }
            }
            else -> Text(
                "Bu cihazda oy özeti yok.",
                style = Yazi.Etiket12,
                color = Renk.MetinEnSoluk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("ozet-yok"),
            )
        }
    }
}

/** Bildirim izni; sistem ayarlarından dönünce yeniden okunur. */
@Composable
private fun bildirimIzniDurumu(context: Context): Boolean {
    var izin by remember { mutableStateOf(CanliAnketBildirimi.izinVarMi(context)) }
    val yasamDongusu = (context as? LifecycleOwner)?.lifecycle
    DisposableEffect(yasamDongusu) {
        val gozlemci = LifecycleEventObserver { _, olay ->
            if (olay == Lifecycle.Event.ON_RESUME) izin = CanliAnketBildirimi.izinVarMi(context)
        }
        yasamDongusu?.addObserver(gozlemci)
        onDispose { yasamDongusu?.removeObserver(gozlemci) }
    }
    return izin
}

private fun bildirimAyarlariniAc(context: Context) {
    val niyet = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(niyet) }
}

private fun surum(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "—"
