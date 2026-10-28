package com.salihcanbolat.secimanketi

import com.salihcanbolat.secimanketi.veri.Lider
import com.salihcanbolat.secimanketi.veri.Partiler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import com.salihcanbolat.secimanketi.veri.Sonuc
import com.salihcanbolat.secimanketi.veri.seritSirasi
import com.salihcanbolat.secimanketi.veri.yuzde
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VeriTest {
    @Test
    fun sembolikVeride_sira_meclisSandalyeSirasi() {
        val serit = SembolikVeri.serit()
        assertEquals(listOf("ak", "yeni", "dem", "mhp", "chp", "iyi", "yeniyol", "hudapar"), serit.map { it.parti.id })
        assertTrue(serit.all { it.sonuc.partiDestek == SembolikVeri.ESIT_DEGER && it.sonuc.liderDestek == SembolikVeri.ESIT_DEGER })
    }

    @Test
    fun gercekVeride_sira_partiOyu_esitlikte_sandalye() {
        val sonuclar = Partiler.tumu.map { Sonuc(it.id, 0.01f, 0.01f, null, null) } +
            listOf(Sonuc("chp", 0.3f, 0.2f, null, null), Sonuc("mhp", 0.3f, 0.1f, null, null))
        val serit = seritSirasi(Partiler.tumu, sonuclar.reversed().distinctBy { it.partiId })
        assertEquals(listOf("mhp", "chp"), serit.take(2).map { it.parti.id })
    }

    @Test
    fun yuzdeBicimi() {
        assertEquals("%16,4", yuzde(0.164f, sembolik = false))
        assertEquals("%—", yuzde(0.164f, sembolik = true))
        assertEquals("%—", yuzde(null, sembolik = false))
    }

    @Test
    fun basHarfler_turkce() {
        assertEquals("İK", Lider("İzzettin Küçük").basHarfler)
        assertEquals("ÇU", Lider("Çiğdem Kılıçgün Uçar").basHarfler)
        assertEquals("RE", Lider("Recep Tayyip Erdoğan").basHarfler)
    }

    @Test
    fun esGenelBaskanlar() {
        assertEquals(2, Partiler.bul("dem").liderler.size)
        assertEquals(2, Partiler.bul("dbp").liderler.size)
        assertEquals("Tülay Hatimoğulları Oruç & Tuncer Bakırhan", Partiler.bul("dem").liderAdlari)
    }
}
