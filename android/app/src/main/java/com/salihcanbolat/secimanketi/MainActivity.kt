package com.salihcanbolat.secimanketi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.salihcanbolat.secimanketi.bildirim.CanliAnketBildirimi
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeEkrani
import com.salihcanbolat.secimanketi.tema.SecimTemasi

/**
 * Şimdilik Ekran 4 ile açılır ve kilit ekranı bildirimini (Ekran 5) gösterir.
 * Anketler ekranı (1) ve "Kilit ekranında takip et" anahtarı (3) sonraki adımda gelir.
 */
class MainActivity : ComponentActivity() {
    private val bildirimIzni = registerForActivityResult(ActivityResultContracts.RequestPermission()) { verildi ->
        if (verildi) CanliAnketBildirimi.goster(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecimTemasi {
                KilitIncelemeEkrani()
            }
        }
        if (CanliAnketBildirimi.izinVarMi(this)) {
            CanliAnketBildirimi.goster(this)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bildirimIzni.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
