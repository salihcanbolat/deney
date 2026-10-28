package com.salihcanbolat.secimanketi.bildirim

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.PathParser
import com.salihcanbolat.secimanketi.R
import com.salihcanbolat.secimanketi.bilesen.Simgeler
import com.salihcanbolat.secimanketi.bilesen.saatMetni
import com.salihcanbolat.secimanketi.bilesen.yenidenYayinMetni
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.SonucGorunumu
import com.salihcanbolat.secimanketi.veri.SeritOgesi
import com.salihcanbolat.secimanketi.veri.yuzde
import kotlin.math.sqrt

/**
 * Kilit ekranı bildirimi için mum şeridini bir Bitmap'e çizer. Bildirimin `RemoteViews`'u mumları
 * doğrudan çizemez (tasarim/TASARIM.md, Android notları); şerit uygulamada çizilip resim olarak verilir.
 *
 * Ölçüler Ekran 5'teki kompakt şeritle aynıdır (sütun 92 dp, logo kutusu 34 dp, mum 7 dp).
 * Bildirim zemini sistemin açık ya da koyu temasına göre değişebildiği için resim kendi koyu
 * zeminiyle çizilir; yazılar her iki durumda da okunur kalır.
 */
object SeritCizici {
    private const val KILIT_YOLU = Simgeler.KILIT

    /** Genişletilmiş bildirim: başlık sistemde, burada şerit ve açıklama satırı. */
    fun buyuk(context: Context, ogeler: List<SeritOgesi>, durum: AnketDurumu, genislikDp: Float = 360f): Bitmap {
        val d = olcek(context)
        val kenar = 12f
        val yukseklikDp = kenar + 92f + 8f + 13f + 10f
        return cizim(context, genislikDp, yukseklikDp) { c, p ->
            val alan = RectF(kenar * d, kenar * d, (genislikDp - kenar) * d, (kenar + 92f) * d)
            if (durum.gorunum == SonucGorunumu.YAYIN_DURDURULDU) {
                yasakPaneli(c, p, alan, d)
            } else {
                sutunlar(c, p, context, ogeler, durum.sembolikGosterim, x0 = alan.left, y0 = alan.top, genislik = alan.width(), d = d)
            }
            aciklama(c, p, context, durum, x0 = (kenar + 4) * d, xSon = (genislikDp - kenar - 4) * d, taban = (kenar + 92f + 8f + 10f) * d, d = d)
        }
    }

    /** Daraltılmış bildirim (≈ 48 dp): kısa mumlar ve logolar; yayın yasağında kilit ve kısa metin. */
    fun kucuk(context: Context, ogeler: List<SeritOgesi>, durum: AnketDurumu, genislikDp: Float = 360f): Bitmap {
        val d = olcek(context)
        val yukseklikDp = 48f
        return cizim(context, genislikDp, yukseklikDp) { c, p ->
            if (durum.gorunum == SonucGorunumu.YAYIN_DURDURULDU) {
                val alan = RectF(12f * d, 6f * d, 150f * d, (yukseklikDp - 6f) * d)
                yasakPaneli(c, p, alan, d, kilitBoyu = 18f)
                val baslik = yaziBoyasi(context, R.font.geist_semibold, 13f, Renk.Metin, d).apply { textAlign = Paint.Align.LEFT }
                val alt = yaziBoyasi(context, R.font.geist_regular, 11f, Renk.Asla, d).apply { textAlign = Paint.Align.LEFT }
                c.drawText("Yayın durduruldu", 162f * d, 21f * d, baslik)
                c.drawText(yenidenYayinMetni(durum.yayinYasagiBitis), 162f * d, 37f * d, alt)
                return@cizim
            }
            val sutunGen = (genislikDp - 24f - 7 * 3f) / 8f
            ogeler.forEachIndexed { i, oge ->
                val x = (12f + i * (sutunGen + 3f)) * d
                val ortaX = x + sutunGen * d / 2
                val logo = 24f * d
                val logoUst = (yukseklikDp - 6f) * d - logo
                if (degerVar(oge)) {
                    mumCifti(c, p, ortaX, taban = logoUst - 3 * d, yukseklik = 14f * d, genislik = 6f * d, fitil = 2f * d, d = d)
                }
                logoKutusu(c, p, context, oge, RectF(ortaX - logo / 2, logoUst, ortaX + logo / 2, logoUst + logo), d)
            }
        }
    }

    /** Henüz oy yokken değer yok: mum çizilmez, yalnızca logo. */
    private fun degerVar(oge: SeritOgesi) = oge.sonuc.partiDestek != null || oge.sonuc.liderDestek != null

    /**
     * Yayın durduruldu: aynı boyda, bulanık çubuklar ve ortada turuncu kilit. Logo yok; sıraları
     * sonucu ele verir.
     */
    private fun yasakPaneli(c: Canvas, p: Paint, alan: RectF, d: Float, kilitBoyu: Float = 28f) {
        p.style = Paint.Style.FILL
        p.color = Color(0x80040714).toArgb()
        c.drawRoundRect(alan, 20 * d, 20 * d, p)
        val ic = RectF(alan.left + 14 * d, alan.top + 10 * d, alan.right - 14 * d, alan.bottom - 10 * d)
        val aralik = 5 * d
        val cubuk = (ic.width() - 7 * aralik) / 8
        val bulanik = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFF2A3560).copy(alpha = 0.6f).toArgb()
            maskFilter = BlurMaskFilter(6 * d, BlurMaskFilter.Blur.NORMAL)
        }
        repeat(8) { i ->
            val x = ic.left + i * (cubuk + aralik)
            c.drawRoundRect(RectF(x, ic.bottom - ic.height() * 0.7f, x + cubuk, ic.bottom), 5 * d, 5 * d, bulanik)
        }
        val kilit = PathParser.createPathFromPathData(KILIT_YOLU)
        val boyut = kilitBoyu * d
        kilit.transform(Matrix().apply {
            setScale(boyut / 24f, boyut / 24f)
            postTranslate(alan.centerX() - boyut / 2, alan.centerY() - boyut / 2)
        })
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.8f * boyut / 24f
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.color = Renk.Asla.toArgb()
        c.drawPath(kilit, p)
        p.style = Paint.Style.FILL
    }

    /**
     * Piksel yoğunluğu, en çok 2×. Bildirimler bir süreçten ötekine taşınır ve resim boyutu
     * sınırlıdır; 2× kilit ekranında net, boyutu küçük tutar.
     */
    private fun olcek(context: Context): Float = context.resources.displayMetrics.density.coerceAtMost(2f)

    private fun cizim(context: Context, genislikDp: Float, yukseklikDp: Float, ciz: (Canvas, Paint) -> Unit): Bitmap {
        val d = olcek(context)
        // Opak, 16 bit: köşeleri bildirimin kendi kartı yuvarlar; boyut yarıya iner.
        val bitmap = Bitmap.createBitmap((genislikDp * d).toInt(), (yukseklikDp * d).toInt(), Bitmap.Config.RGB_565)
        val c = Canvas(bitmap)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        c.drawColor(Renk.CamKart.copy(alpha = 1f).toArgb())
        ciz(c, p)
        return bitmap
    }

    private fun sutunlar(c: Canvas, p: Paint, context: Context, ogeler: List<SeritOgesi>, sembolik: Boolean, x0: Float, y0: Float, genislik: Float, d: Float) {
        val aralik = 3 * d
        val sutunGen = (genislik - aralik * (ogeler.size - 1)) / ogeler.size
        val sutunYuk = 92 * d
        val yazi = yaziBoyasi(context, R.font.geist_medium, 11f, Renk.MetinIkincil, d)
        ogeler.forEachIndexed { i, oge ->
            val x = x0 + i * (sutunGen + aralik)
            val kutu = RectF(x, y0, x + sutunGen, y0 + sutunYuk)
            val r = 16 * d
            p.style = Paint.Style.FILL
            p.color = Color.White.copy(alpha = 0.05f).toArgb()
            c.drawRoundRect(kutu, r, r, p)
            p.style = Paint.Style.STROKE
            p.strokeWidth = 1 * d
            p.color = Color.White.copy(alpha = 0.06f).toArgb()
            c.drawRoundRect(RectF(kutu.left + d / 2, kutu.top + d / 2, kutu.right - d / 2, kutu.bottom - d / 2), r, r, p)
            p.style = Paint.Style.FILL

            val ortaX = kutu.centerX()
            // Üstte yüzde (11 sp)
            val yuzdeTaban = kutu.top + 6 * d + 11 * d * 0.8f
            c.drawText(yuzde(oge.sonuc.partiDestek, sembolik), ortaX, yuzdeTaban, yazi)
            // Altta logo kutusu (34 dp)
            val logo = 34 * d
            val logoUst = kutu.bottom - 4 * d - logo
            logoKutusu(c, p, context, oge, RectF(ortaX - logo / 2, logoUst, ortaX + logo / 2, logoUst + logo), d)
            // Ortada mumlar: alanın (oran + %30)'u
            val alanUst = kutu.top + 6 * d + 11 * d + 4 * d
            val alanAlt = logoUst - 4 * d
            val alan = alanAlt - alanUst
            val oran = ((oge.sonuc.partiDestek ?: 0f) + 0.3f).coerceIn(0f, 1f)
            val liderOran = ((oge.sonuc.liderDestek ?: 0f) + 0.3f).coerceIn(0f, 1f)
            if (degerVar(oge)) mumCifti(c, p, ortaX, alanAlt, alan * oran, 7 * d, 3 * d, d, liderYukseklik = alan * liderOran)
        }
    }

    /** Yapışık iki mum: solda parti (fildişi), sağda lider (mavi); fitil ve çizgili dolgu. */
    private fun mumCifti(
        c: Canvas, p: Paint, ortaX: Float, taban: Float, yukseklik: Float, genislik: Float, fitil: Float, d: Float,
        liderYukseklik: Float = yukseklik,
    ) {
        mum(c, p, ortaX - genislik, taban, yukseklik, genislik, fitil, Renk.MumParti, 0.7f, 0.6f, sol = true, d = d)
        mum(c, p, ortaX, taban, liderYukseklik, genislik, fitil, Renk.MumLider, 0.9f, 0.5f, sol = false, d = d)
    }

    private fun mum(
        c: Canvas, p: Paint, x: Float, taban: Float, yukseklik: Float, genislik: Float, fitil: Float,
        renk: Color, fitilAlfa: Float, golgeAlfa: Float, sol: Boolean, d: Float,
    ) {
        val ust = taban - yukseklik
        p.style = Paint.Style.FILL
        p.color = renk.copy(alpha = fitilAlfa).toArgb()
        c.drawRect(x + genislik / 2 - 0.75f * d, ust, x + genislik / 2 + 0.75f * d, ust + fitil, p)

        val govde = RectF(x, ust + fitil, x + genislik, taban)
        val (ustSol, ustSag, altSag, altSol) = if (sol) listOf(4f, 0f, 0f, 2f) else listOf(0f, 4f, 2f, 0f)
        val yol = Path().apply {
            addRoundRect(
                govde,
                floatArrayOf(ustSol * d, ustSol * d, ustSag * d, ustSag * d, altSag * d, altSag * d, altSol * d, altSol * d),
                Path.Direction.CW,
            )
        }
        c.save()
        c.clipPath(yol)
        p.color = renk.copy(alpha = golgeAlfa).toArgb()
        c.drawRect(govde, p)
        p.color = renk.copy(alpha = 0.95f).toArgb()
        p.strokeWidth = 3 * d
        val adim = 6 * d * sqrt(2f)
        var sx = govde.left - govde.height()
        while (sx < govde.right + govde.height()) {
            c.drawLine(sx, govde.bottom, sx + govde.height(), govde.top, p)
            sx += adim
        }
        c.restore()
    }

    private fun logoKutusu(c: Canvas, p: Paint, context: Context, oge: SeritOgesi, kutu: RectF, d: Float) {
        val r = 10 * d * (kutu.width() / (34 * d))
        p.style = Paint.Style.FILL
        p.color = (if (oge.parti.logo != null) Renk.LogoKutusu else Renk.Yuzey2).toArgb()
        c.drawRoundRect(kutu, r, r, p)
        val logo = oge.parti.logo
        if (logo != null) {
            BitmapFactory.decodeResource(context.resources, logo)?.let { resim ->
                val ic = RectF(kutu.left + 2 * d, kutu.top + 2 * d, kutu.right - 2 * d, kutu.bottom - 2 * d)
                val olcek = minOf(ic.width() / resim.width, ic.height() / resim.height)
                val w = resim.width * olcek
                val h = resim.height * olcek
                val hedef = RectF(ic.centerX() - w / 2, ic.centerY() - h / 2, ic.centerX() + w / 2, ic.centerY() + h / 2)
                c.drawBitmap(resim, null, hedef, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            }
        } else {
            val glif = PathParser.createPathFromPathData(oge.parti.glif)
            val boyut = kutu.width() * 15f / 34f
            val olcek = boyut / 24f
            glif.transform(Matrix().apply {
                setScale(olcek, olcek)
                postTranslate(kutu.centerX() - boyut / 2, kutu.centerY() - boyut / 2)
            })
            p.style = Paint.Style.STROKE
            p.strokeWidth = 2 * olcek
            p.strokeCap = Paint.Cap.ROUND
            p.strokeJoin = Paint.Join.ROUND
            p.color = Renk.Glif.toArgb()
            c.drawPath(glif, p)
            p.style = Paint.Style.FILL
        }
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1 * d
        p.color = Color.White.copy(alpha = 0.1f).toArgb()
        c.drawRoundRect(RectF(kutu.left + d / 2, kutu.top + d / 2, kutu.right - d / 2, kutu.bottom - d / 2), r, r, p)
        p.style = Paint.Style.FILL
    }

    /**
     * "▮ Parti  ▮ Lider  Sembolik · eşit" ve sağda "Dokun, incele →". Duruma göre orta not:
     * "Henüz oy yok", "Çevrimdışı · 21:40"; yayın yasağında "Yayın durduruldu" ve yeniden yayın tarihi.
     */
    private fun aciklama(c: Canvas, p: Paint, context: Context, durum: AnketDurumu, x0: Float, xSon: Float, taban: Float, d: Float) {
        val yazi = yaziBoyasi(context, R.font.geist_regular, 11f, Renk.MetinEnSoluk, d).apply { textAlign = Paint.Align.LEFT }
        if (durum.gorunum == SonucGorunumu.YAYIN_DURDURULDU) {
            val baslik = yaziBoyasi(context, R.font.geist_semibold, 11f, Renk.Metin, d).apply { textAlign = Paint.Align.LEFT }
            c.drawText("Yayın durduruldu", x0, taban, baslik)
            yazi.color = Renk.Asla.toArgb()
            yazi.textAlign = Paint.Align.RIGHT
            c.drawText(yenidenYayinMetni(durum.yayinYasagiBitis), xSon, taban, yazi)
            return
        }
        var x = x0
        p.style = Paint.Style.FILL
        for ((ad, renk) in listOf("Parti" to Renk.MumParti, "Lider" to Renk.MumLider)) {
            p.color = renk.toArgb()
            c.drawRoundRect(RectF(x, taban - 8.5f * d, x + 6 * d, taban + 0.5f * d), 2 * d, 2 * d, p)
            x += 10 * d
            c.drawText(ad, x, taban, yazi)
            x += yazi.measureText(ad) + 10 * d
        }
        val not = when {
            durum.cevrimdisi -> "Çevrimdışı" + (durum.sonGuncelleme?.let { " · ${saatMetni(it)}" } ?: "")
            durum.gorunum == SonucGorunumu.SEMBOLIK -> "Sembolik · eşit"
            durum.gorunum == SonucGorunumu.HENUZ_OY_YOK -> "Henüz oy yok"
            else -> null
        }
        if (not != null) c.drawText(not, x, taban, yazi)
        yazi.textAlign = Paint.Align.RIGHT
        c.drawText("Dokun, incele →", xSon, taban, yazi)
    }

    private fun yaziBoyasi(context: Context, font: Int, sp: Float, renk: Color, d: Float): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = runCatching { ResourcesCompat.getFont(context, font) }.getOrNull() ?: Typeface.DEFAULT
            textSize = sp * d * context.resources.configuration.fontScale.coerceAtMost(1.3f)
            color = renk.toArgb()
            textAlign = Paint.Align.CENTER
            fontFeatureSettings = "tnum"
        }
}
