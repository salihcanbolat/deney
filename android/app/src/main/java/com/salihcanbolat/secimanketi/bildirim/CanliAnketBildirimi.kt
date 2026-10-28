package com.salihcanbolat.secimanketi.bildirim

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.salihcanbolat.secimanketi.R
import com.salihcanbolat.secimanketi.kilit.KilitIncelemeActivity
import com.salihcanbolat.secimanketi.tema.Renk
import com.salihcanbolat.secimanketi.bilesen.cevrimdisiEtiketi
import com.salihcanbolat.secimanketi.bilesen.yenidenYayinMetni
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.AnketDurumu
import com.salihcanbolat.secimanketi.veri.SonucGorunumu
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import com.salihcanbolat.secimanketi.veri.SeritOgesi
import java.util.Locale

private val Turkce = Locale.forLanguageTag("tr")

/**
 * Ekran 5 · Kilit ekranındaki kalıcı "Canlı Anket" bildirimi.
 *
 * - İçinde kompakt mum şeridi (Bitmap) ve açıklama satırı bulunur.
 * - Kişisel tercih içermez; bu yüzden VISIBILITY_PUBLIC. Kullanıcı "hassas içeriği gizle" ayarını
 *   açtıysa sistem şeridi gizler, bu değiştirilemez.
 * - Dokununca Ekran 4 kilit açılmadan açılır (KilitIncelemeActivity, showWhenLocked).
 */
object CanliAnketBildirimi {
    /**
     * Kanal önemi DEFAULT, ama sessiz: Android 12+ (Pixel varsayılanı) kilit ekranında LOW önemli
     * "sessiz bildirimleri" gizliyor. Ses ve titreşim kanalda kapalı. Kanalın önemi sonradan
     * değiştirilemediği için kimlik yenilendi; eski kanal silinir.
     */
    const val KANAL = "canli_anket_kilit"
    private const val ESKI_KANAL = "canli_anket"
    const val KIMLIK = 1

    fun kanalOlustur(context: Context) {
        val kanal = NotificationChannel(KANAL, "Canlı anket", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Kilit ekranında seçili anketin canlı şeridi. Sessizdir, kişisel tercih içermez."
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
            setShowBadge(false)
        }
        val yonetici = context.getSystemService(NotificationManager::class.java)
        yonetici.deleteNotificationChannel(ESKI_KANAL)
        yonetici.createNotificationChannel(kanal)
    }

    fun izinVarMi(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Bildirimi gösterir ya da günceller (anket ya da durumu değiştikçe yeniden çağrılır). */
    fun goster(
        context: Context,
        anket: Anket = Anketler.genelSecim,
        durum: AnketDurumu = SembolikVeri.durum(),
        ogeler: List<SeritOgesi> = SembolikVeri.serit(anket, durum),
    ) {
        if (!izinVarMi(context)) return
        kanalOlustur(context)
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(KIMLIK, olustur(context, anket, ogeler, durum))
    }

    fun kaldir(context: Context) {
        NotificationManagerCompat.from(context).cancel(KIMLIK)
    }

    fun olustur(context: Context, anket: Anket, ogeler: List<SeritOgesi>, durum: AnketDurumu): android.app.Notification {
        val aciklama = erisilebilirlikMetni(anket, ogeler, durum)
        val buyuk = RemoteViews(context.packageName, R.layout.bildirim_serit).apply {
            setImageViewBitmap(R.id.serit, SeritCizici.buyuk(context, ogeler, durum))
            setContentDescription(R.id.serit, aciklama)
        }
        val kucuk = RemoteViews(context.packageName, R.layout.bildirim_serit_kucuk).apply {
            setImageViewBitmap(R.id.serit, SeritCizici.kucuk(context, ogeler, durum))
            setContentDescription(R.id.serit, aciklama)
        }
        return NotificationCompat.Builder(context, KANAL)
            .setSmallIcon(R.drawable.ic_bildirim)
            .setColor(Renk.Vurgu.toArgb())
            .setContentTitle("Canlı Anket · ${anket.ad}")
            .setContentText(durumSatiri(durum))
            .setSubText(durumEtiketi(anket, durum))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(kucuk)
            .setCustomBigContentView(buyuk)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(incelemeNiyeti(context))
            .build()
    }

    /** Ekran 4'ü açılış animasyonu olmadan, kilit açılmadan açar. */
    private fun incelemeNiyeti(context: Context): PendingIntent {
        val niyet = Intent(context, KilitIncelemeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        return PendingIntent.getActivity(context, 0, niyet, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Ekran 6'daki kilit ekranı karşılığının üst satırı: "GENEL · SEMBOLİK", "GENEL · YASAK",
     * "ÇEVRİMDIŞI · 21:40". Kişisel bilgi içermez.
     */
    fun durumEtiketi(anket: Anket, durum: AnketDurumu): String {
        val ad = anket.kisaAd.uppercase(Turkce)
        return when {
            durum.gorunum == SonucGorunumu.YAYIN_DURDURULDU -> "$ad · YASAK"
            durum.cevrimdisi -> cevrimdisiEtiketi(durum)
            durum.gorunum == SonucGorunumu.SEMBOLIK -> "$ad · SEMBOLİK"
            else -> ad
        }
    }

    /** Alt satır: "Parti / Lider · Sembolik · eşit", "Henüz oy yok", "Yayın durduruldu", "Son bilinen veri". */
    fun durumSatiri(durum: AnketDurumu): String = when {
        durum.gorunum == SonucGorunumu.YAYIN_DURDURULDU -> "Yayın durduruldu · " + yenidenYayinMetni(durum.yayinYasagiBitis)
        durum.cevrimdisi -> "Son bilinen veri"
        durum.gorunum == SonucGorunumu.HENUZ_OY_YOK -> "Henüz oy yok"
        durum.gorunum == SonucGorunumu.SEMBOLIK -> "Parti / Lider · Sembolik · eşit"
        else -> "Parti / Lider"
    }

    /** Tasarımdaki aria metni: anket, durum, partiler (yasakta partiler yok). */
    fun erisilebilirlikMetni(anket: Anket, ogeler: List<SeritOgesi>, durum: AnketDurumu): String = buildString {
        append("Canlı Anket bildirimi, ${anket.ad}. ")
        if (durum.gorunum == SonucGorunumu.YAYIN_DURDURULDU) {
            append("Yayın yasağı nedeniyle sonuçlar gizlendi. ")
            append(yenidenYayinMetni(durum.yayinYasagiBitis)).append(". ")
        } else {
            append(ogeler.joinToString(", ") { it.parti.kisaAd }).append(". ")
            when (durum.gorunum) {
                SonucGorunumu.SEMBOLIK -> append("Parti ve lider desteği sembolik, eşit. ")
                SonucGorunumu.HENUZ_OY_YOK -> append("Henüz oy yok. ")
                else -> Unit
            }
            if (durum.cevrimdisi) append("Bağlantı yok, son bilinen veri. ")
        }
        append("Kilidi açmadan incelemek için dokun.")
    }
}
