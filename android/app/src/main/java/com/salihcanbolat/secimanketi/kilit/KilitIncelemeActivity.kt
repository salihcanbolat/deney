package com.salihcanbolat.secimanketi.kilit

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.salihcanbolat.secimanketi.tema.SecimTemasi
import com.salihcanbolat.secimanketi.veri.UygulamaDurumu

/**
 * Ekran 4'ün kabı. Bildirime dokununca açılır.
 * - Kilit açılmadan, kilit ekranının üstünde gösterilir (showWhenLocked).
 * - `requestDismissKeyguard` çağrılmaz; kilit açık kalmaz. Geri hareketi ya da güç düğmesi kilit ekranına döner.
 * - Açılış ve kapanış animasyonu yoktur; ekran değişmiyormuş gibi görünür.
 */
class KilitIncelemeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        animasyonsuz()
        enableEdgeToEdge()
        // Bildirimdeki anketle aynı: kullanıcının kilit ekranına aldığı anket.
        val kilitAnketi = UygulamaDurumu.yukle(this).kilitAnketi
        setContent {
            SecimTemasi {
                KilitIncelemeEkrani(anket = kilitAnketi)
            }
        }
    }

    override fun finish() {
        super.finish()
        animasyonsuz(kapanis = true)
    }

    private fun animasyonsuz(kapanis: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(if (kapanis) Activity.OVERRIDE_TRANSITION_CLOSE else Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
