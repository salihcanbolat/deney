package com.salihcanbolat.secimanketi

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.salihcanbolat.secimanketi.bildirim.CanliAnketBildirimi
import com.salihcanbolat.secimanketi.bildirim.SeritCizici
import com.salihcanbolat.secimanketi.ekran.Uygulama
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeEkrani
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.OrnekDurumlar
import com.salihcanbolat.secimanketi.veri.Partiler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import com.salihcanbolat.secimanketi.veri.SonucGorunumu
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
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Ekran 6: durumlar (sembolik, henüz oy yok, yayın durduruldu, bağlantı yok, giriş gerekli). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-port-xxhdpi")
class DurumlarTest {
    @get:Rule
    val compose = createComposeRule()

    private val uygulama: Application = ApplicationProvider.getApplicationContext()
    private val gun = LocalDate.of(2026, 10, 25)

    /** Testlerdeki örnek yeniden yayın zamanı (yalnızca görüntü için). */
    private val yayinBitisi = LocalDateTime.of(2026, 11, 3, 21, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val sonGuncelleme = LocalDateTime.of(2026, 10, 25, 21, 40).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun kaydet(ad: String) = compose.onRoot().captureRoboImage("../../build/screenshots/android/$ad.png")

    private fun kaydet(resim: Bitmap, ad: String) {
        val dosya = File("../../build/screenshots/android/$ad.png")
        dosya.parentFile?.mkdirs()
        dosya.outputStream().use { resim.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun durumla(anketDurumu: AnketDurumu) = UygulamaDurumu().apply {
        anketDurumunuGuncelle(kilitAnketiId, anketDurumu)
    }

    // ---- Veri ----

    @Test
    fun gorunum_oncelikleri() {
        assertEquals(SonucGorunumu.SEMBOLIK, OrnekDurumlar.sembolik().gorunum)
        assertEquals(SonucGorunumu.HENUZ_OY_YOK, OrnekDurumlar.henuzOyYok().gorunum)
        // Yasak her şeyin önünde; bağlantı yokken de sonuç gösterilmez, "çevrimdışı" sayılmaz.
        val yasakVeCevrimdisi = OrnekDurumlar.yayinDurduruldu().copy(baglantiVar = false)
        assertEquals(SonucGorunumu.YAYIN_DURDURULDU, yasakVeCevrimdisi.gorunum)
        assertFalse(yasakVeCevrimdisi.cevrimdisi)
        assertTrue(OrnekDurumlar.cevrimdisi(sonGuncelleme).cevrimdisi)
    }

    @Test
    fun yayinYasagi_seritHicYok_henuzOyYok_meclisSirasi() {
        assertTrue(SembolikVeri.serit(Anketler.genelSecim, OrnekDurumlar.yayinDurduruldu()).isEmpty())
        val bos = SembolikVeri.serit(Anketler.genelSecim, OrnekDurumlar.henuzOyYok())
        assertEquals(Partiler.tumu.take(8).map { it.id }, bos.map { it.parti.id })
        assertTrue(bos.all { it.sonuc.partiDestek == null && it.sonuc.liderDestek == null })
    }

    @Test
    fun bildirim_yasaktaPartiAdiYok() {
        val durum = OrnekDurumlar.yayinDurduruldu(yayinBitisi)
        val metin = CanliAnketBildirimi.erisilebilirlikMetni(Anketler.genelSecim, SembolikVeri.serit(Anketler.genelSecim, durum), durum)
        Partiler.tumu.forEach { assertFalse(it.kisaAd, metin.contains(it.kisaAd)) }
        assertTrue(metin.contains("Yeniden yayın: 3 Kasım, 21:00"))
        assertEquals("GENEL · YASAK", CanliAnketBildirimi.durumEtiketi(Anketler.genelSecim, durum))
        assertEquals("ÇEVRİMDIŞI · 21:40", CanliAnketBildirimi.durumEtiketi(Anketler.genelSecim, OrnekDurumlar.cevrimdisi(sonGuncelleme)))
        assertEquals("GENEL · SEMBOLİK", CanliAnketBildirimi.durumEtiketi(Anketler.genelSecim, OrnekDurumlar.sembolik()))
    }

    @Test
    fun durum_cihazdaSaklanir() {
        val durum = UygulamaDurumu.yukle(uygulama)
        durum.anketDurumunuGuncelle(Anketler.genelSecim.id, OrnekDurumlar.cevrimdisi(sonGuncelleme))
        assertEquals(OrnekDurumlar.cevrimdisi(sonGuncelleme), UygulamaDurumu.yukle(uygulama).anketDurumu(Anketler.genelSecim.id))
    }

    // ---- Ekran görüntüleri ----

    @Test
    fun anketler_yayinDurduruldu() {
        compose.setContent { SecimTemasi(hareketAzalt = true) { Uygulama(durumla(OrnekDurumlar.yayinDurduruldu(yayinBitisi)), bugun = gun) } }
        compose.onNodeWithTag("yasak-paneli", useUnmergedTree = true).assertIsDisplayed()
        kaydet("50-durum-anketler-yasak")
    }

    @Test
    fun anketler_baglantiYok() {
        compose.setContent { SecimTemasi(hareketAzalt = true) { Uygulama(durumla(OrnekDurumlar.cevrimdisi(sonGuncelleme)), bugun = gun) } }
        compose.onNodeWithTag("baglanti-yok").assertIsDisplayed()
        kaydet("51-durum-anketler-cevrimdisi")
    }

    @Test
    fun anketler_henuzOyYok() {
        val durum = UygulamaDurumu().apply {
            Anketler.tumu.forEach { anketDurumunuGuncelle(it.id, OrnekDurumlar.henuzOyYok()) }
        }
        compose.setContent { SecimTemasi(hareketAzalt = true) { Uygulama(durum, bugun = gun) } }
        kaydet("52-durum-anketler-henuz-oy-yok")
    }

    @Test
    fun kilit_yayinDurduruldu() {
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                KilitIncelemeEkrani(anketDurumu = OrnekDurumlar.yayinDurduruldu(yayinBitisi), sabitZaman = LocalDateTime.of(2026, 10, 25, 21, 40), uyanis = false)
            }
        }
        // Yasakta şerit (ve logolar) yok
        compose.onNodeWithTag("serit").assertDoesNotExist()
        kaydet("53-durum-kilit-yasak")
    }

    @Test
    fun kilit_henuzOyYok() {
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                KilitIncelemeEkrani(anketDurumu = OrnekDurumlar.henuzOyYok(), sabitZaman = LocalDateTime.of(2026, 10, 25, 21, 40), uyanis = false)
            }
        }
        kaydet("54-durum-kilit-henuz-oy-yok")
    }

    @Test
    fun kilit_cevrimdisi() {
        compose.setContent {
            SecimTemasi(hareketAzalt = true) {
                KilitIncelemeEkrani(anketDurumu = OrnekDurumlar.cevrimdisi(sonGuncelleme), sabitZaman = LocalDateTime.of(2026, 10, 25, 21, 40), uyanis = false)
            }
        }
        compose.onNodeWithText("ÇEVRİMDIŞI · 21:40").assertIsDisplayed()
        kaydet("55-durum-kilit-cevrimdisi")
    }

    @Test
    fun girisGerekli_hesapDugmesindenAcilirKapanir() {
        compose.setContent { SecimTemasi(hareketAzalt = true) { Uygulama(UygulamaDurumu(), bugun = gun) } }
        compose.onNodeWithContentDescription("Hesap ve Google ile giriş").performSemanticsAction(SemanticsActions.OnClick)
        compose.onNodeWithTag("giris-gerekli").assertIsDisplayed()
        compose.onNodeWithText("Google ile giriş yap").performSemanticsAction(SemanticsActions.OnClick)
        compose.onNodeWithText("Google ile giriş bu sürümde henüz yok; sunucu bağlanınca açılacak.").assertIsDisplayed()
        kaydet("56-durum-giris-gerekli")
        compose.onNodeWithText("Göz atmaya devam et").performSemanticsAction(SemanticsActions.OnClick)
        compose.onNodeWithTag("giris-gerekli").assertDoesNotExist()
    }

    @Test
    fun bildirimResimleri() {
        val yasak = OrnekDurumlar.yayinDurduruldu(yayinBitisi)
        kaydet(SeritCizici.buyuk(uygulama, emptyList(), yasak), "57-bildirim-yasak-genis")
        kaydet(SeritCizici.kucuk(uygulama, emptyList(), yasak), "58-bildirim-yasak-dar")
        val henuz = OrnekDurumlar.henuzOyYok()
        kaydet(SeritCizici.buyuk(uygulama, SembolikVeri.serit(Anketler.genelSecim, henuz), henuz), "59-bildirim-henuz-oy-yok")
        val cevrimdisi = OrnekDurumlar.cevrimdisi(sonGuncelleme)
        kaydet(SeritCizici.buyuk(uygulama, SembolikVeri.serit(Anketler.genelSecim, cevrimdisi), cevrimdisi), "60-bildirim-cevrimdisi")
    }
}
