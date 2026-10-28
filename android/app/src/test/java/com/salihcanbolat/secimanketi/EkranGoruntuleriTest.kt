package com.salihcanbolat.secimanketi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.salihcanbolat.secimanketi.bilesen.SeritDurumu
import com.salihcanbolat.secimanketi.bilesen.SeritKarti
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeEkrani
import com.salihcanbolat.secimanketi.kilit.kilitZemini
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDateTime

/**
 * Ekran görüntüleri (Roborazzi, emülatörsüz). Tasarımdaki 390 × 844 çerçeve.
 * PNG'ler depo kökünde build/screenshots/android/ altına yazılır; CI bunları docs/screenshots/android/ altına kopyalar.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-port-xxhdpi")
class EkranGoruntuleriTest {
    @get:Rule
    val compose = createComposeRule()

    private val zaman = LocalDateTime.of(2026, 10, 25, 21, 40)

    private fun kaydet(ad: String) {
        compose.onRoot().captureRoboImage("../../build/screenshots/android/$ad.png")
    }

    @Test
    fun seritKompakt_bildirim() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                Box(Modifier.fillMaxSize().kilitZemini()) {
                    SeritKarti(
                        anket = Anketler.genelSecim,
                        ogeler = SembolikVeri.serit(),
                        sembolik = true,
                        durum = SeritDurumu(),
                        ipucu = "Dokun, incele →",
                        etkilesimli = false,
                        modifier = Modifier.padding(top = 214.dp, start = 12.dp, end = 12.dp).fillMaxWidth(),
                    )
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        kaydet("01-serit-kompakt")
    }

    @Test
    fun inceleme_dinlenme() {
        icerik(SeritDurumu())
        kaydet("10-inceleme-dinlenme")
    }

    @Test
    fun inceleme_akAcik() {
        icerik(SeritDurumu(acik = 0, tutuluyor = true))
        kaydet("11-inceleme-ak-acik")
    }

    @Test
    fun inceleme_demAcik_esGenelBaskanlar() {
        icerik(SeritDurumu(acik = 2, tutuluyor = true))
        kaydet("12-inceleme-dem-acik")
    }

    @Test
    fun inceleme_yeniYolAcik_kartSolda() {
        icerik(SeritDurumu(acik = 6, tutuluyor = true))
        kaydet("13-inceleme-yeniyol-acik")
    }

    /** Gerçek jest: basılı tut (240 ms eşiği), kaydır, bırak. */
    @Test
    fun jest_basiliTutKaydirBirak() {
        val durum = SeritDurumu()
        icerik(durum)
        val serit = compose.onNodeWithTag("serit")
        var genislik = 0f
        serit.performTouchInput { genislik = width.toFloat(); down(Offset(width / 16f, height / 2f)) }
        compose.mainClock.advanceTimeBy(150)
        assertNull("eşikten önce açılmamalı", durum.acik)
        compose.mainClock.advanceTimeBy(200)
        assertEquals(0, durum.acik)
        serit.performTouchInput { moveTo(Offset(genislik * 11f / 16f, height / 2f)) }
        compose.mainClock.advanceTimeBy(900)
        assertEquals(5, durum.acik)
        kaydet("14-jest-kaydirildi-iyi")
        serit.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(900)
        assertNull("bırakınca kompakt hâle dönmeli", durum.acik)
    }

    /** Basılı tutmadan yapılan kaydırma şeridi açmaz. */
    @Test
    fun jest_basiliTutmadanKaydirmaAcmaz() {
        val durum = SeritDurumu()
        icerik(durum)
        val serit = compose.onNodeWithTag("serit")
        serit.performTouchInput {
            down(Offset(width / 16f, height / 2f))
            moveTo(Offset(width * 9f / 16f, height / 2f))
        }
        compose.mainClock.advanceTimeBy(600)
        assertNull(durum.acik)
        serit.performTouchInput { up() }
    }

    private fun icerik(durum: SeritDurumu) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            SecimTemasi(hareketAzalt = false) {
                KilitIncelemeEkrani(durum = durum, sabitZaman = zaman, uyanis = false)
            }
        }
        compose.mainClock.advanceTimeBy(1500)
    }
}
