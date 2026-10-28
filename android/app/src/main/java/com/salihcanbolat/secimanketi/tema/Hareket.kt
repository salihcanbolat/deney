package com.salihcanbolat.secimanketi.tema

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import android.content.Context

/** Hareket: tasarim/tokens.json → "hareket". */
object Hareket {
    /** Sütunun büyümesi, mum yüksekliği. */
    val YayBuyume = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1.05f)
    const val YayBuyumeMs = 600

    val Basma = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1.2f)
    const val BasmaMs = 350
    const val BasmaOlcek = 0.96f

    val Giris = CubicBezierEasing(0.16f, 0.84f, 0.24f, 1f)
    const val GirisMs = 800
    const val GirisKademeMs = 70
    const val GirisKaymaDp = 16

    val BilgiKarti = CubicBezierEasing(0.16f, 0.84f, 0.24f, 1f)
    const val BilgiKartiMs = 450
    const val BilgiKartiGecikmeMs = 100

    const val BasiliTutmaEsigiMs = 240L

    const val CanliPariltiDonguMs = 7000
    const val CanliPariltiSutunKademeMs = 350

    /** CSS "ease" */
    val Yumusak = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

    /** Sistemde "Animasyonları kaldır" açık mı (animatör süre ölçeği 0). */
    fun hareketAzaltildiMi(context: Context): Boolean =
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}

/** "Animasyonları kaldır" açıkken tüm animasyonlar kapanır, geçişler anında olur. */
val LocalHareketAzalt = staticCompositionLocalOf { false }

/** Hareketi azalt açıksa animasyonu anlık geçişe çevirir. */
@Composable
@ReadOnlyComposable
fun <T> hareket(spec: AnimationSpec<T>): AnimationSpec<T> = if (LocalHareketAzalt.current) snap() else spec

/** Tasarımdaki süre ve eğriyle tween; hareketi azalt açıksa anlık. */
@Composable
@ReadOnlyComposable
fun <T> gecis(ms: Int, easing: androidx.compose.animation.core.Easing, gecikmeMs: Int = 0): AnimationSpec<T> =
    hareket(tween(durationMillis = ms, delayMillis = gecikmeMs, easing = easing))
