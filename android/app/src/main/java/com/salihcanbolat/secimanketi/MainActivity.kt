package com.salihcanbolat.secimanketi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeEkrani
import com.salihcanbolat.secimanketi.tema.SecimTemasi

/** Şimdilik Ekran 4 ile açılır; Anketler ekranı (1) sonraki adımda gelir. */
class MainActivity : ComponentActivity() {
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
