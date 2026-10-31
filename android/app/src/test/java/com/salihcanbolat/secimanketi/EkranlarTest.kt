package com.salihcanbolat.secimanketi

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.salihcanbolat.secimanketi.ekran.CanliEkrani
import com.salihcanbolat.secimanketi.ekran.OyGonderildiEkrani
import com.salihcanbolat.secimanketi.ekran.OyVermeEkrani
import com.salihcanbolat.secimanketi.ekran.Uygulama
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.Anketler
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

/** Ekran 1, 2, 3 ve Canlı sekmesi: ekran görüntüleri ve oy akışı. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-port-xxhdpi")
class EkranlarTest {
    @get:Rule
    val compose = createComposeRule()

    private val gun = LocalDate.of(2026, 10, 25)

    private fun kaydet(ad: String) {
        compose.onRoot().captureRoboImage("../../build/screenshots/android/$ad.png")
    }

    /** Giriş animasyonları gerçek eğrileriyle oynar, sonra görüntü alınır (sonsuz animasyonlar beklenmez). */
    private fun animasyonlarBitsin() {
        compose.mainClock.advanceTimeBy(2500)
    }

    private fun genelSecimeKatilmis() = UygulamaDurumu().apply {
        oyKaydet(Oy(Anketler.genelSecim.id, "chp", "chp", "zafer", 0L))
    }

    // ---- Ekran görüntüleri ----

    @Test
    fun anketler_ilkAcilis() {
        compose.mainClock.autoAdvance = false
        compose.setContent { SecimTemasi(hareketAzalt = false) { Uygulama(UygulamaDurumu(), bugun = gun) } }
        animasyonlarBitsin()
        kaydet("40-anketler")
    }

    @Test
    fun anketler_katildiklarim() {
        compose.mainClock.autoAdvance = false
        compose.setContent { SecimTemasi(hareketAzalt = false) { Uygulama(genelSecimeKatilmis(), bugun = gun) } }
        animasyonlarBitsin()
        kaydet("41-anketler-katildiklarim")
        compose.onRoot().performTouchInput { swipeUp(startY = bottom * 0.8f, endY = top + bottom * 0.15f) }
        animasyonlarBitsin()
        kaydet("42-anketler-asagi")
    }

    @Test
    fun oyVerme_bos() {
        compose.mainClock.autoAdvance = false
        compose.setContent { SecimTemasi(hareketAzalt = false) { OyVermeEkrani(Anketler.gencSecmen, onGeri = {}, onGonder = {}) } }
        animasyonlarBitsin()
        kaydet("43-oy-verme-bos")
    }

    @Test
    fun oyVerme_secimlerle() {
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                OyVermeEkrani(Anketler.gencSecmen, onGeri = {}, onGonder = {}, baslangicParti = "dem", baslangicLider = "dem", baslangicAsla = "yrp")
            }
        }
        kaydet("44-oy-verme-parti")
        compose.onNodeWithTag("oy-listesi").performScrollToKey("baslik-LIDER")
        kaydet("45-oy-verme-lider")
        compose.onNodeWithTag("oy-listesi").performScrollToKey("baslik-ASLA")
        kaydet("46-oy-verme-asla")
    }

    @Test
    fun oyGonderildi() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            SecimTemasi(hareketAzalt = false) {
                OyGonderildiEkrani(
                    anket = Anketler.gencSecmen,
                    oy = Oy(Anketler.gencSecmen.id, "dem", "dem", null, 0L),
                    kilitteTakip = true,
                    onTakipDegistir = {},
                    onKilitGorunumu = {},
                    onAnketlereDon = {},
                    halkaZamani = 2600L,
                )
            }
        }
        animasyonlarBitsin()
        kaydet("47-oy-gonderildi")
    }

    @Test
    fun canli() {
        compose.mainClock.autoAdvance = false
        compose.setContent { SecimTemasi(hareketAzalt = false) { CanliEkrani(Anketler.genelSecim, bugun = gun) } }
        animasyonlarBitsin()
        kaydet("48-canli")
    }

    // ---- Davranış ----

    @Test
    fun oyAkisi_katilSecGonderTakip() {
        val durum = UygulamaDurumu()
        compose.setContent { SecimTemasi(hareketAzalt = true) { Uygulama(durum, bugun = gun) } }

        katil(Anketler.gencSecmen.id)
        // Parti seçilmeden gönderilemez
        compose.onNodeWithTag("gonder-pasif").assertIsDisplayed()
        compose.onNodeWithTag("parti-dem").performClick()
        // Desteklenen parti "asla" bölümünde seçilemez
        compose.onNodeWithTag("oy-listesi").performScrollToKey("asla-0")
        compose.onNodeWithTag("asla-dem").assertIsNotEnabled()
        compose.onNodeWithTag("gonder").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Oyun sayıldı").assertIsDisplayed()
        assertTrue(durum.katildiMi(Anketler.gencSecmen.id))
        assertEquals("dem", durum.oy(Anketler.gencSecmen.id)?.partiId)
        // "Kilit ekranında takip et" varsayılan açık: kilit ekranındaki anket değişir
        assertEquals(Anketler.gencSecmen.id, durum.kilitAnketiId)
        compose.onNodeWithTag("takip-anahtari").assertIsOn().performClick()
        compose.onNodeWithTag("takip-anahtari").assertIsOff()
        assertEquals(Anketler.genelSecim.id, durum.kilitAnketiId)

        compose.onNodeWithText("Anketlere dön").performScrollTo().tikla()
        compose.waitForIdle()
        // Artık "Katıldıklarım" altında, "Sonuçlar" düğmesiyle
        compose.onNode(hasText("Sonuçlar") and hasAnyAncestor(hasTestTag("anket-${Anketler.gencSecmen.id}"))).assertExists()
    }

    @Test
    fun kilitEkraninaAl_tekAnket() {
        val durum = UygulamaDurumu()
        compose.setContent { SecimTemasi(hareketAzalt = true) { Uygulama(durum, bugun = gun) } }
        compose.onNodeWithTag("takip-${Anketler.genelSecim.id}").assertIsOn()
        compose.onNodeWithTag("takip-${Anketler.liderTercihi.id}").assertIsOff().performScrollTo().tikla()
        assertEquals(Anketler.liderTercihi.id, durum.kilitAnketiId)
        // Aynı anda yalnızca bir anket: önceki "Takip et"e döner, yenisi "Takipte" ve kısa doğrulama
        compose.onNodeWithTag("takip-${Anketler.genelSecim.id}").assertIsOff()
        compose.onNodeWithTag("takip-${Anketler.liderTercihi.id}").assertIsOn()
        kaydet("67-takip")
    }

    @Test
    fun yalnizAsla_partiYerineAslaZorunlu() {
        var gonderilen: Oy? = null
        compose.setContent {
            SecimTemasi(hareketAzalt = true) { OyVermeEkrani(Anketler.aslaOyVermem, onGeri = {}, onGonder = { gonderilen = it }) }
        }
        compose.onNodeWithTag("parti-ak").assertDoesNotExist()
        compose.onNodeWithTag("gonder-pasif").assertIsDisplayed()
        compose.onNodeWithTag("asla-zafer").performScrollTo().tikla()
        compose.onNodeWithTag("gonder").performClick()
        assertEquals("zafer", gonderilen?.aslaPartiId)
        assertEquals(null, gonderilen?.partiId)
    }

    @Test
    fun durum_tekOyVeCihazdaSaklanir() {
        val baglam = ApplicationProvider.getApplicationContext<android.content.Context>()
        val durum = UygulamaDurumu.yukle(baglam)
        assertTrue(durum.oyKaydet(Oy("genc-secmen", "chp", null, "ak", 123L)))
        assertFalse("ikinci oy kabul edilmemeli", durum.oyKaydet(Oy("genc-secmen", "ak", null, null, 456L)))
        durum.kilideAl("genc-secmen")
        durum.zilDegistir("genc-secmen")

        val yeniden = UygulamaDurumu.yukle(baglam)
        assertEquals(Oy("genc-secmen", "chp", null, "ak", 123L), yeniden.oy("genc-secmen"))
        assertEquals("genc-secmen", yeniden.kilitAnketiId)
        assertTrue(yeniden.zilAcikMi("genc-secmen"))
    }

    private fun katil(anketId: String) {
        // Dokunuş yerine anlamsal tıklama: performScrollTo öğeyi ekranın alt kenarına getirir,
        // orada yüzen alt gezinme hapının altında kalır ve dokunuş hapa gider.
        compose.onNode(hasText("Katıl") and hasAnyAncestor(hasTestTag("anket-$anketId"))).performScrollTo().tikla()
        compose.waitForIdle()
    }

    private fun SemanticsNodeInteraction.tikla() = performSemanticsAction(SemanticsActions.OnClick)
}
