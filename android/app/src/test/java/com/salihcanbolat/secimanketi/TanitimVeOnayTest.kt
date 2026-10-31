package com.salihcanbolat.secimanketi

import android.content.Context
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.salihcanbolat.secimanketi.ekran.OyVermeEkrani
import com.salihcanbolat.secimanketi.ekran.TanitimEkrani
import com.salihcanbolat.secimanketi.ekran.Uygulama
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.UygulamaDurumu
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** İlk açılış tanıtımı ve gönderimden önce gözden geçirme paneli. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-port-xxhdpi")
class TanitimVeOnayTest {
    @get:Rule
    val compose = createComposeRule()

    private fun kaydet(ad: String) = compose.onRoot().captureRoboImage("../../build/screenshots/android/$ad.png")
    private fun SemanticsNodeInteraction.tikla() = performSemanticsAction(SemanticsActions.OnClick)

    private fun tanitim(sayfa: Int) {
        compose.setContent { SecimTemasi(hareketAzalt = true) { TanitimEkrani(onBitti = {}, onBildirimIzniIste = {}, baslangicSayfasi = sayfa) } }
    }

    @Test
    fun tanitim_1_esitOy() {
        tanitim(0)
        kaydet("70-tanitim-esit-oy")
    }

    @Test
    fun tanitim_2_kilitEkrani() {
        tanitim(1)
        kaydet("71-tanitim-kilit-ekrani")
    }

    @Test
    fun tanitim_3_basiliTut() {
        tanitim(2)
        kaydet("72-tanitim-basili-tut")
    }

    @Test
    fun ilkAcilis_tanitimBirKezGosterilir_izinIstenir() {
        val baglam = ApplicationProvider.getApplicationContext<Context>()
        val durum = UygulamaDurumu.yukle(baglam)
        var izinIstendi = 0
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                Uygulama(durum, bugun = LocalDate.of(2026, 10, 25), onBildirimIzniIste = { izinIstendi++ })
            }
        }
        compose.onNodeWithTag("tanitim").assertIsDisplayed()
        repeat(3) {
            compose.onNodeWithTag("tanitim-ileri").tikla()
            compose.waitForIdle()
        }
        assertEquals("ikinci sayfada bildirim izni istenmeli", 1, izinIstendi)
        assertTrue(durum.tanitimGoruldu)
        compose.onNodeWithTag("tanitim").assertDoesNotExist()
        assertTrue("bir daha gösterilmez", UygulamaDurumu.yukle(baglam).tanitimGoruldu)
    }

    @Test
    fun oyOnay_paneli() {
        var gonderildi = false
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                OyVermeEkrani(
                    Anketler.gencSecmen,
                    onGeri = {},
                    onGonder = { gonderildi = true },
                    baslangicParti = "chp",
                    baslangicLider = "dem",
                    baslangicAsla = "zafer",
                )
            }
        }
        compose.onNodeWithTag("gonder").performClick()
        compose.onNodeWithTag("oy-onay").assertIsDisplayed()
        assertFalse(gonderildi)
        kaydet("73-oy-onay")
        compose.onNodeWithTag("basili-tut-gonder").tikla()
        assertTrue(gonderildi)
    }
}
