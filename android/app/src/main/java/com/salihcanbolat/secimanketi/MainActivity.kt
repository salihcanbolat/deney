package com.salihcanbolat.secimanketi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.salihcanbolat.secimanketi.bildirim.CanliAnketBildirimi
import com.salihcanbolat.secimanketi.ekran.Uygulama
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.UygulamaDurumu

/**
 * Uygulamanın giriş noktası: Anketler (1) → Oy verme (2) → Oy gönderildi (3), Kilit ekranı (4) ve Canlı.
 * Kilit ekranındaki anket değişince kilit ekranı bildirimi (5) güncellenir.
 */
class MainActivity : ComponentActivity() {
    private lateinit var durum: UygulamaDurumu

    private val bildirimIzni = registerForActivityResult(ActivityResultContracts.RequestPermission()) { verildi ->
        if (verildi) bildirimiGuncelle(durum.kilitAnketi, durum.anketDurumu(durum.kilitAnketiId))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        durum = UygulamaDurumu.yukle(this)
        setContent {
            SecimTemasi {
                Uygulama(durum = durum, onKilitAnketiDegisti = ::bildirimiGuncelle)
            }
        }
        if (!CanliAnketBildirimi.izinVarMi(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bildirimIzni.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun bildirimiGuncelle(anket: Anket, anketDurumu: AnketDurumu) {
        CanliAnketBildirimi.goster(this, anket, anketDurumu)
    }
}
