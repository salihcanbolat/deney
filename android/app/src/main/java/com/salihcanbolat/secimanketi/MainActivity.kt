package com.salihcanbolat.secimanketi

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.PathInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.salihcanbolat.secimanketi.bildirim.CanliAnketBildirimi
import com.salihcanbolat.secimanketi.ekran.Uygulama
import com.salihcanbolat.secimanketi.tema.Hareket
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.UygulamaDurumu

/**
 * Uygulamanın giriş noktası: açılış ekranı (hareketli mum simgesi), ilk açılışta tanıtım, sonra
 * Anketler (1) → Oy verme (2) → Oy gönderildi (3), Kilit ekranı (4), Canlı, ayrıntı ve Hesap.
 * Kilit ekranındaki anket ya da durumu değişince kilit ekranı bildirimi (5) güncellenir.
 */
class MainActivity : ComponentActivity() {
    private lateinit var durum: UygulamaDurumu

    private val bildirimIzni = registerForActivityResult(ActivityResultContracts.RequestPermission()) { verildi ->
        if (verildi && durum.bildirimAcik) bildirimiGuncelle(durum.kilitAnketi, durum.anketDurumu(durum.kilitAnketiId))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val acilis = installSplashScreen()
        super.onCreate(savedInstanceState)
        acilis.setOnExitAnimationListener { gorunum ->
            // Simge hafifçe büyüyerek, zemin solarak çekilir; uygulama arkadan belirir.
            val egri = PathInterpolator(0.16f, 0.84f, 0.24f, 1f)
            val simge: View? = runCatching { gorunum.iconView }.getOrNull()
            val animasyonlar = buildList {
                add(ObjectAnimator.ofFloat(gorunum.view, View.ALPHA, 1f, 0f))
                if (simge != null) {
                    add(ObjectAnimator.ofFloat(simge, View.SCALE_X, 1f, 1.12f))
                    add(ObjectAnimator.ofFloat(simge, View.SCALE_Y, 1f, 1.12f))
                }
            }
            AnimatorSet().apply {
                playTogether(animasyonlar)
                duration = Hareket.BilgiKartiMs.toLong()
                interpolator = egri
                doOnEnd { gorunum.remove() }
                start()
            }
        }
        enableEdgeToEdge()
        durum = UygulamaDurumu.yukle(this)
        setContent {
            SecimTemasi {
                Uygulama(
                    durum = durum,
                    onKilitAnketiDegisti = ::bildirimiGuncelle,
                    onBildirimKapat = { CanliAnketBildirimi.kaldir(this) },
                    onBildirimIzniIste = ::bildirimIzniIste,
                )
            }
        }
    }

    /** Tanıtımda istenir; izin yoksa Hesap ekranı ayarlara yönlendirir. */
    private fun bildirimIzniIste() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !CanliAnketBildirimi.izinVarMi(this)) {
            bildirimIzni.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun bildirimiGuncelle(anket: Anket, anketDurumu: AnketDurumu) {
        CanliAnketBildirimi.goster(this, anket, anketDurumu)
    }
}
