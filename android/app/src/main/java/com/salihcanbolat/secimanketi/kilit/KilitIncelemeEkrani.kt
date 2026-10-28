package com.salihcanbolat.secimanketi.kilit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salihcanbolat.secimanketi.bilesen.SeritDurumu
import com.salihcanbolat.secimanketi.bilesen.SeritKarti
import com.salihcanbolat.secimanketi.bilesen.rememberSeritDurumu
import com.salihcanbolat.secimanketi.tema.Olcu
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import com.salihcanbolat.secimanketi.veri.SeritOgesi
import java.time.LocalDateTime

/**
 * Ekran 4 · Kilit ekranında inceleme. Ekran 5 (kilit ekranı bildirimi) ile piksel piksel aynıdır:
 * aynı duvar kâğıdı, aynı saat, şerit aynı konumda. Değişen tek şeyler şeridin çevresindeki kısa
 * mavi parıltı ve sağ alttaki ipucunun "Basılı tut, kaydır" olması.
 */
@Composable
fun KilitIncelemeEkrani(
    modifier: Modifier = Modifier,
    anket: Anket = Anketler.genelSecim,
    ogeler: List<SeritOgesi> = SembolikVeri.serit(anket),
    sembolik: Boolean = true,
    durum: SeritDurumu = rememberSeritDurumu(),
    sabitZaman: LocalDateTime? = null,
    uyanis: Boolean = true,
) {
    Box(modifier.fillMaxSize().kilitZemini()) {
        SuzulenIsik()
        KilitSaati(Modifier.padding(top = 64.dp, start = 28.dp, end = 28.dp), sabitZaman)
        SeritKarti(
            anket = anket,
            ogeler = ogeler,
            sembolik = sembolik,
            durum = durum,
            ipucu = "Basılı tut, kaydır",
            ipucuVurgulu = true,
            uyanis = uyanis,
            modifier = Modifier
                .padding(
                    top = Olcu.KilitSeridiKartUst,
                    start = Olcu.KilitSeridiKartYanBosluk,
                    end = Olcu.KilitSeridiKartYanBosluk,
                )
                .fillMaxWidth(),
        )
        KilitAltKismi()
    }
}
