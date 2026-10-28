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
import com.salihcanbolat.secimanketi.veri.Anket
import com.salihcanbolat.secimanketi.veri.Anketler
import com.salihcanbolat.secimanketi.veri.SembolikVeri
import com.salihcanbolat.secimanketi.veri.SeritOgesi

/**
 * Ekran 5 · Kilit ekranındaki kalıcı "Canlı Anket" bildirimi.
 *
 * - İçinde kompakt mum şeridi (Bitmap) ve açıklama satırı bulunur.
 * - Kişisel tercih içermez; bu yüzden VISIBILITY_PUBLIC. Kullanıcı "hassas içeriği gizle" ayarını
 *   açtıysa sistem şeridi gizler, bu değiştirilemez.
 * - Dokununca Ekran 4 kilit açılmadan açılır (KilitIncelemeActivity, showWhenLocked).
 */
object CanliAnketBildirimi {
    const val KANAL = "canli_anket"
    const val KIMLIK = 1

    fun kanalOlustur(context: Context) {
        val kanal = NotificationChannel(KANAL, "Canlı anket", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Kilit ekranında seçili anketin canlı şeridi. Sessizdir, kişisel tercih içermez."
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(kanal)
    }

    fun izinVarMi(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Bildirimi gösterir ya da günceller (veri değiştikçe yeniden çağrılır). */
    fun goster(
        context: Context,
        anket: Anket = Anketler.genelSecim,
        ogeler: List<SeritOgesi> = SembolikVeri.serit(anket),
        sembolik: Boolean = true,
    ) {
        if (!izinVarMi(context)) return
        kanalOlustur(context)
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(KIMLIK, olustur(context, anket, ogeler, sembolik))
    }

    fun kaldir(context: Context) {
        NotificationManagerCompat.from(context).cancel(KIMLIK)
    }

    fun olustur(context: Context, anket: Anket, ogeler: List<SeritOgesi>, sembolik: Boolean): android.app.Notification {
        val aciklama = erisilebilirlikMetni(anket, ogeler, sembolik)
        val buyuk = RemoteViews(context.packageName, R.layout.bildirim_serit).apply {
            setImageViewBitmap(R.id.serit, SeritCizici.buyuk(context, ogeler, sembolik))
            setContentDescription(R.id.serit, aciklama)
        }
        val kucuk = RemoteViews(context.packageName, R.layout.bildirim_serit_kucuk).apply {
            setImageViewBitmap(R.id.serit, SeritCizici.kucuk(context, ogeler))
            setContentDescription(R.id.serit, aciklama)
        }
        return NotificationCompat.Builder(context, KANAL)
            .setSmallIcon(R.drawable.ic_bildirim)
            .setColor(Renk.Vurgu.toArgb())
            .setContentTitle("Canlı Anket · ${anket.ad}")
            .setContentText(if (sembolik) "Parti / Lider · Sembolik · eşit" else "Parti / Lider")
            .setSubText(anket.ad)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(kucuk)
            .setCustomBigContentView(buyuk)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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

    /** Tasarımdaki aria metni: anket, partiler ve verinin sembolik olduğu. */
    fun erisilebilirlikMetni(anket: Anket, ogeler: List<SeritOgesi>, sembolik: Boolean): String =
        "Canlı Anket bildirimi, ${anket.ad}. " +
            ogeler.joinToString(", ") { it.parti.kisaAd } + ". " +
            (if (sembolik) "Parti ve lider desteği sembolik, eşit. " else "") +
            "Kilidi açmadan incelemek için dokun."
}
