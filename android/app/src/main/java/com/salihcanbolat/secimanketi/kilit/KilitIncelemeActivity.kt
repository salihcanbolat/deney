package com.salihcanbolat.secimanketi.kilit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.salihcanbolat.secimanketi.tema.SecimTemasi

/**
 * Ekran 4'ün kabı. Manifestte `showWhenLocked="true"`: kilit açılmadan kilit ekranının üstünde
 * gösterilir. `requestDismissKeyguard` çağrılmaz; geri hareketi ya da güç düğmesi kilit ekranına döner.
 * Açılış animasyonu temada kapalı (windowAnimationStyle = null).
 */
class KilitIncelemeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecimTemasi {
                KilitIncelemeEkrani()
            }
        }
    }
}
