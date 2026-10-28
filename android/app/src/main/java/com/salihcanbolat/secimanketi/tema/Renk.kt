package com.salihcanbolat.secimanketi.tema

import androidx.compose.ui.graphics.Color

/**
 * Renkler: tasarim/tokens.json → "renk". Ekranlarda sabit renk kullanılmaz, buradan alınır.
 * Parti renkleri arayüzde hiçbir yerde kullanılmaz; partiler yalnızca logolarıyla görünür.
 */
object Renk {
    val Zemin = Color(0xFF070B1C)
    val ZeminKilitGradyan = listOf(Color(0xFF16224D), Color(0xFF0A1030), Color(0xFF040714))
    val Yuzey = Color(0xFF10162E)
    val Yuzey2 = Color(0xFF18203D)
    /** rgba(20,28,58,0.78) */
    val CamKart = Color(0xC7141C3A)
    /** rgba(255,255,255,0.08) */
    val Cizgi = Color(0x14FFFFFF)

    val Metin = Color(0xFFF5F1E8)
    val MetinIkincil = Color(0xFFD9DEEC)
    val MetinSoluk = Color(0xFFB8C1DD)
    val MetinEnSoluk = Color(0xFF98A2C3)

    val Vurgu = Color(0xFF5B87FF)
    val VurguAcik = Color(0xFF8FAEFF)
    val VurguKoyu = Color(0xFF2E5BFF)
    val VurguUstuMetin = Color(0xFF050A1F)

    /** Yalnızca "asla" değerleri için. */
    val Asla = Color(0xFFFFA65C)

    val LogoKutusu = Color(0xFFF7F5EF)
    val MumParti = Color(0xFFF5F1E8)
    val MumLider = Color(0xFF5B87FF)

    // Ekran HTML'lerinde geçen ek tonlar
    /** Bildirim başlığı ("Canlı Anket · …") */
    val BaslikMetni = Color(0xFFC7CEE3)
    /** Logosu olmayan partinin glifi (kapalı sütunda) */
    val Glif = Color(0xFFE6E9F3)
    /** Bilgi kartı zemini: rgba(10,16,48,0.85) */
    val BilgiKarti = Color(0xD90A1030)
}
