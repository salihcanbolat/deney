package com.salihcanbolat.secimanketi

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performSemanticsAction
import com.github.takahirom.roborazzi.captureRoboImage
import com.salihcanbolat.secimanketi.ekran.AnketAyrintiEkrani
import com.salihcanbolat.secimanketi.ekran.Hedef
import com.salihcanbolat.secimanketi.ekran.HesapEkrani
import com.salihcanbolat.secimanketi.ekran.Uygulama
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.OrnekDurumlar
import com.salihcanbolat.secimanketi.veri.Oy
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

/** Anket ayrıntısı ve Hesap / ayarlar ekranı. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-port-xxhdpi")
class HesapVeAyrintiTest {
    @get:Rule
    val compose = createComposeRule()

    private val gun = LocalDate.of(2026, 10, 25)
    private val oy = Oy(Anketler.genelSecim.id, "chp", "dem", "zafer", 0L)

    private fun kaydet(ad: String) = compose.onRoot().captureRoboImage("../../build/screenshots/android/$ad.png")
    private fun SemanticsNodeInteraction.tikla() = performSemanticsAction(SemanticsActions.OnClick)

    private fun ayrinti(anketDurumu: AnketDurumu, oyu: Oy? = oy, hareketAzalt: Boolean = true) {
        compose.setContent {
            SecimTemasi(hareketAzalt = hareketAzalt) {
                AnketAyrintiEkrani(
                    anket = Anketler.genelSecim,
                    anketDurumu = anketDurumu,
                    oy = oyu,
                    zilAcik = true,
                    kilitte = true,
                    onGeri = {}, onZil = {}, onKilit = {}, onKatil = {}, onKilitGorunumu = {},
                )
            }
        }
    }

    @Test
    fun ayrinti_sembolik_destekVeAsla() {
        compose.mainClock.autoAdvance = false
        ayrinti(OrnekDurumlar.sembolik(), hareketAzalt = false)
        compose.mainClock.advanceTimeBy(2500)
        kaydet("61-ayrinti-sembolik")
    }

    @Test
    fun ayrinti_aslaSekmesi() {
        ayrinti(OrnekDurumlar.sembolik())
        compose.onNodeWithTag("tur-asla").tikla()
        compose.waitForIdle()
        compose.onNodeWithText("Partiye asla").assertIsDisplayed()
        kaydet("62-ayrinti-asla")
    }

    @Test
    fun ayrinti_seninOyun() {
        ayrinti(OrnekDurumlar.sembolik())
        compose.onNodeWithTag("ayrinti-listesi").performScrollToKey("oyun-not")
        compose.onNodeWithContentDescription("Seçimlerinin özeti").assertExists()
        kaydet("63-ayrinti-senin-oyun")
    }

    @Test
    fun ayrinti_yasak_katilmamis() {
        ayrinti(OrnekDurumlar.yayinDurduruldu(), oyu = null)
        compose.onNodeWithText("Yayın durduruldu").assertIsDisplayed()
        compose.onNodeWithTag("ayrinti-katil").assertIsDisplayed()
        kaydet("64-ayrinti-yasak")
    }

    @Test
    fun hesap_ekrani() {
        val durum = UygulamaDurumu().apply { oyKaydet(oy) }
        compose.setContent { SecimTemasi(hareketAzalt = true) { HesapEkrani(durum, onGeri = {}) } }
        kaydet("65-hesap")
        compose.onNodeWithTag("ozet-sil").performScrollTo()
        kaydet("66-hesap-asagi")
    }

    @Test
    fun hesap_bildirimKapatilir_ozetSilinir_katilimKalir() {
        val durum = UygulamaDurumu().apply { oyKaydet(oy) }
        var kapandi = false
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                Uygulama(durum, bugun = gun, onBildirimKapat = { kapandi = true }, baslangic = listOf(Hedef.AnketListesi, Hedef.Hesap))
            }
        }
        compose.onNodeWithTag("kilit-bildirimi").assertIsOn().tikla()
        compose.onNodeWithTag("kilit-bildirimi").assertIsOff()
        compose.waitForIdle()
        assertFalse(durum.bildirimAcik)
        assertTrue("bildirim kaldırılmalı", kapandi)

        compose.onNodeWithTag("ozet-sil").performScrollTo().tikla()
        compose.onNodeWithTag("ozet-sil-onay").tikla()
        compose.waitForIdle()
        assertFalse(durum.ozetVarMi)
        assertTrue("katılım kalmalı: tek oy kuralı", durum.katildiMi(oy.anketId))
        assertFalse("ikinci oy kabul edilmemeli", durum.oyKaydet(oy.copy(partiId = "ak")))
        compose.onNodeWithTag("ozet-yok").assertExists()
    }

    @Test
    fun gezinme_kartAyrintiyiAcar_ayrintidanKatilinir() {
        val durum = UygulamaDurumu()
        compose.setContent { SecimTemasi(hareketAzalt = true) { Uygulama(durum, bugun = gun) } }
        compose.onNodeWithContentDescription("Hesap ve Google ile giriş").tikla()
        compose.onNodeWithTag("hesap-giris").assertExists()
        compose.onNodeWithContentDescription("Geri").tikla()
        compose.waitForIdle()

        compose.onNodeWithTag("anket-${Anketler.gencSecmen.id}").performScrollTo().tikla()
        compose.waitForIdle()
        compose.onNodeWithTag("ayrinti-listesi").assertExists()
        compose.onNodeWithTag("ayrinti-katil").tikla()
        compose.waitForIdle()
        compose.onNodeWithTag("oy-listesi").assertExists()
        assertEquals(Anketler.genelSecim.id, durum.kilitAnketiId)
    }
}
