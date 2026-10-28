package com.salihcanbolat.secimanketi.tema

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.salihcanbolat.secimanketi.R

/** Geist (metin) ve Geist Mono (etiketler). SIL Open Font License; android/GEIST_LICENSE.txt. */
val Geist = FontFamily(
    Font(R.font.geist_light, FontWeight.Light),
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_bold, FontWeight.Bold),
)

val GeistMono = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
    Font(R.font.geist_mono_medium, FontWeight.Medium),
    Font(R.font.geist_mono_semibold, FontWeight.SemiBold),
    Font(R.font.geist_mono_bold, FontWeight.Bold),
)

/** Satır yüksekliğini tasarımdaki gibi tam uygular (yazı tipinin iç boşluğu eklenmez). */
private val siki = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
    fontFeatureSettings = "tnum",
)

/**
 * Yazı ölçeği: tasarim/tokens.json → "yazi". Boyutlar sp; sistem yazı boyutuyla büyür.
 * Rakamlar her yerde sabit genişlikli (tnum). En küçük boyut 11 sp.
 */
object Yazi {
    val Saat = siki.copy(fontFamily = Geist, fontSize = 96.sp, fontWeight = FontWeight.Light, letterSpacing = (-0.03).em, lineHeight = 0.95.em)
    val Baslik1 = siki.copy(fontFamily = Geist, fontSize = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.03).em)
    val Baslik2 = siki.copy(fontFamily = Geist, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).em)
    val Baslik3 = siki.copy(fontFamily = Geist, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    val Govde = siki.copy(fontFamily = Geist, fontSize = 15.sp, fontWeight = FontWeight.Normal)
    val Kucuk = siki.copy(fontFamily = Geist, fontSize = 13.sp, fontWeight = FontWeight.Normal)
    val EnKucuk = siki.copy(fontFamily = Geist, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    val EnKucukMono = siki.copy(fontFamily = GeistMono, fontSize = 11.sp, fontWeight = FontWeight.Normal)

    // Ekran HTML'lerindeki ara boyutlar
    val Tarih = siki.copy(fontFamily = Geist, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    val Etiket12 = siki.copy(fontFamily = Geist, fontSize = 12.sp, fontWeight = FontWeight.Normal)
    val KartBasligi = siki.copy(fontFamily = Geist, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.015).em, lineHeight = 1.15.em)
    val BuyukDeger = siki.copy(fontFamily = Geist, fontSize = 22.sp, fontWeight = FontWeight.Light)
}
