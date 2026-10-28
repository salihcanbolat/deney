package com.salihcanbolat.secimanketi

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.salihcanbolat.secimanketi.bildirim.CanliAnketBildirimi
import com.salihcanbolat.secimanketi.bildirim.SeritCizici
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeActivity
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Ekran 5: kilit ekranı bildirimi. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-port-xxhdpi")
class BildirimTest {
    private val uygulama: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun bildirim_kilitEkranindaGorunur_kaliciSessiz_ve4uAcar() {
        shadowOf(uygulama).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        CanliAnketBildirimi.goster(uygulama)

        val yonetici = uygulama.getSystemService(NotificationManager::class.java)
        val bildirimler = shadowOf(yonetici).allNotifications
        assertEquals(1, bildirimler.size)
        val bildirim = bildirimler.single()
        assertEquals(Notification.VISIBILITY_PUBLIC, bildirim.visibility)
        assertTrue("kalıcı olmalı", bildirim.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertTrue("yalnızca bir kez uyarmalı", bildirim.flags and Notification.FLAG_ONLY_ALERT_ONCE != 0)
        assertEquals(CanliAnketBildirimi.KANAL, bildirim.channelId)

        val kanal = yonetici.getNotificationChannel(CanliAnketBildirimi.KANAL)
        assertEquals(Notification.VISIBILITY_PUBLIC, kanal.lockscreenVisibility)
        // LOW olursa Android 12+ kilit ekranında "sessiz bildirim" diye gizlenir.
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, kanal.importance)
        assertNull("sessiz olmalı", kanal.sound)
        assertFalse("titreşmemeli", kanal.shouldVibrate())

        val niyet = shadowOf(bildirim.contentIntent).savedIntent
        assertEquals(KilitIncelemeActivity::class.java.name, niyet.component?.className)
    }

    @Test
    fun bildirim_kisiselTercihIcermez() {
        val metin = CanliAnketBildirimi.erisilebilirlikMetni(Anketler.genelSecim, SembolikVeri.serit(), SembolikVeri.durum())
        assertTrue(metin.startsWith("Canlı Anket bildirimi, Genel Seçim Anketi."))
        assertTrue(metin.contains("sembolik"))
        // Oy, "senin" ya da kullanıcıya ait bir ifade yok.
        listOf("senin", "oyun", "tercihin").forEach { assertTrue(it, !metin.lowercase().contains(it)) }
    }

    @Test
    fun seritResimleri() {
        kaydet(SeritCizici.buyuk(uygulama, SembolikVeri.serit(), SembolikVeri.durum()), "20-bildirim-serit-genis")
        kaydet(SeritCizici.kucuk(uygulama, SembolikVeri.serit(), SembolikVeri.durum()), "21-bildirim-serit-dar")
    }

    /** Bildirim gövdesi, Android'in koyu bildirim kartına yerleştirilmiş hâliyle (yaklaşık önizleme). */
    @Test
    fun bildirimGovdesi_onizleme() {
        val etkinlik = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val bildirim = CanliAnketBildirimi.olustur(etkinlik, Anketler.genelSecim, SembolikVeri.serit(), SembolikVeri.durum())
        val d = etkinlik.resources.displayMetrics.density
        val kok = FrameLayout(etkinlik).apply { setBackgroundColor(Renk.ZeminKilitGradyan[1].toArgb()) }
        val kart = LinearLayout(etkinlik).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1F2128.toInt())
            setPadding((16 * d).toInt(), (12 * d).toInt(), (16 * d).toInt(), (14 * d).toInt())
        }
        kart.addView(TextView(etkinlik).apply {
            text = "Seçim Anketi · Genel Seçim Anketi"
            setTextColor(0xFFC7CEE3.toInt())
            textSize = 12f
            setPadding(0, 0, 0, (8 * d).toInt())
        })
        val govde = bildirim.bigContentView!!.apply(etkinlik, kart)
        kart.addView(govde)
        kok.addView(kart, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP).apply {
            topMargin = (214 * d).toInt(); leftMargin = (8 * d).toInt(); rightMargin = (8 * d).toInt()
        })
        etkinlik.setContentView(kok)
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertTrue((govde.findViewById<ImageView>(R.id.serit).drawable) != null)
        kok.captureRoboImage("../../build/screenshots/android/22-bildirim-onizleme.png")
    }

    private fun kaydet(resim: Bitmap, ad: String) {
        val dosya = File("../../build/screenshots/android/$ad.png")
        dosya.parentFile?.mkdirs()
        dosya.outputStream().use { resim.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
