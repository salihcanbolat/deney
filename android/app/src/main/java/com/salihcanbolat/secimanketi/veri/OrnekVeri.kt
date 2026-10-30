package com.salihcanbolat.secimanketi.veri

import com.salihcanbolat.secimanketi.R

/**
 * Partiler ve genel başkanlar (Ekim 2026), Meclis sandalye sırasıyla.
 * Logolar ve fotoğraflar tasarım paketinden (tasarim/varliklar/); yeniden düzenlenmez.
 * Eksik logo ve fotoğraflar tasarim/TASARIM.md "Eksikler" bölümünde.
 */
object Partiler {
    val tumu: List<Parti> = listOf(
        Parti(
            id = "ak", kisaAd = "AK Parti", uzunAd = "Adalet ve Kalkınma Partisi", kod = "AK",
            logo = R.drawable.logo_ak, glif = GLIFLER[0],
            liderler = listOf(Lider("Recep Tayyip Erdoğan", R.drawable.lider_ak_recep_tayyip_erdogan)), sandalyeSirasi = 1,
        ),
        Parti(
            id = "yeni", kisaAd = "YENİ Parti", uzunAd = "YENİ Parti", kod = "YENİ",
            logo = null, glif = GLIFLER[1],
            liderler = listOf(Lider("Özgür Özel", R.drawable.lider_yeni_ozgur_ozel)), sandalyeSirasi = 2,
        ),
        Parti(
            id = "dem", kisaAd = "DEM Parti", uzunAd = "Halkların Eşitlik ve Demokrasi Partisi", kod = "DEM",
            logo = R.drawable.logo_dem, glif = GLIFLER[2],
            liderler = listOf(Lider("Tülay Hatimoğulları Oruç", R.drawable.lider_dem_tulay_hatimogullari_oruc), Lider("Tuncer Bakırhan", R.drawable.lider_dem_tuncer_bakirhan)), sandalyeSirasi = 3,
        ),
        Parti(
            id = "mhp", kisaAd = "MHP", uzunAd = "Milliyetçi Hareket Partisi", kod = "MHP",
            logo = R.drawable.logo_mhp, glif = GLIFLER[3],
            liderler = listOf(Lider("Devlet Bahçeli", R.drawable.lider_mhp_devlet_bahceli)), sandalyeSirasi = 4,
        ),
        Parti(
            id = "chp", kisaAd = "CHP", uzunAd = "Cumhuriyet Halk Partisi", kod = "CHP",
            logo = R.drawable.logo_chp, glif = GLIFLER[4],
            liderler = listOf(Lider("Kemal Kılıçdaroğlu", R.drawable.lider_chp_kemal_kilicdaroglu)), sandalyeSirasi = 5,
        ),
        Parti(
            id = "iyi", kisaAd = "İYİ Parti", uzunAd = "İYİ Parti", kod = "İYİ",
            logo = R.drawable.logo_iyi, glif = GLIFLER[5],
            liderler = listOf(Lider("Müsavat Dervişoğlu", R.drawable.lider_iyi_musavat_dervisoglu)), sandalyeSirasi = 6,
        ),
        Parti(
            id = "yeniyol", kisaAd = "Yeni Yol", uzunAd = "Yeni Yol Partisi", kod = "YYP",
            logo = R.drawable.logo_yeni_yol, glif = GLIFLER[6],
            liderler = listOf(Lider("İzzettin Küçük")), sandalyeSirasi = 7,
        ),
        Parti(
            id = "hudapar", kisaAd = "HÜDA PAR", uzunAd = "Hür Dava Partisi", kod = "HÜDA",
            logo = R.drawable.logo_huda_par, glif = GLIFLER[7],
            liderler = listOf(Lider("Zekeriya Yapıcıoğlu")), sandalyeSirasi = 8,
        ),
        Parti(
            id = "yrp", kisaAd = "Yeniden Refah", uzunAd = "Yeniden Refah Partisi", kod = "YRP",
            logo = R.drawable.logo_yeniden_refah, glif = GLIFLER[8],
            liderler = listOf(Lider("Fatih Erbakan")), sandalyeSirasi = 9,
        ),
        Parti(
            id = "tip", kisaAd = "TİP", uzunAd = "Türkiye İşçi Partisi", kod = "TİP",
            logo = null, glif = GLIFLER[9],
            liderler = listOf(Lider("Erkan Baş")), sandalyeSirasi = 10,
        ),
        Parti(
            id = "dbp", kisaAd = "DBP", uzunAd = "Demokratik Bölgeler Partisi", kod = "DBP",
            logo = R.drawable.logo_dbp, glif = GLIFLER[10],
            liderler = listOf(Lider("Çiğdem Kılıçgün Uçar"), Lider("Keskin Bayındır")), sandalyeSirasi = 11,
        ),
        Parti(
            id = "emep", kisaAd = "EMEP", uzunAd = "Emek Partisi", kod = "EMEP",
            logo = R.drawable.logo_emep, glif = GLIFLER[11],
            liderler = listOf(Lider("Seyit Aslan")), sandalyeSirasi = 12,
        ),
        Parti(
            id = "saadet", kisaAd = "Saadet", uzunAd = "Saadet Partisi", kod = "SP",
            logo = R.drawable.logo_saadet, glif = GLIFLER[12],
            liderler = listOf(Lider("Mahmut Arıkan")), sandalyeSirasi = 13,
        ),
        Parti(
            id = "dsp", kisaAd = "DSP", uzunAd = "Demokratik Sol Parti", kod = "DSP",
            logo = null, glif = GLIFLER[13],
            liderler = listOf(Lider("Önder Aksakal")), sandalyeSirasi = 14,
        ),
        Parti(
            id = "dp", kisaAd = "DP", uzunAd = "Demokrat Parti", kod = "DP",
            logo = null, glif = GLIFLER[14],
            liderler = listOf(Lider("Gültekin Uysal")), sandalyeSirasi = 15,
        ),
        Parti(
            id = "zafer", kisaAd = "Zafer", uzunAd = "Zafer Partisi", kod = "ZP",
            logo = R.drawable.logo_zafer, glif = GLIFLER[15],
            liderler = listOf(Lider("Ümit Özdağ")), sandalyeSirasi = 16,
        ),
        Parti(
            id = "anahtar", kisaAd = "Anahtar", uzunAd = "Anahtar Parti", kod = "AP",
            logo = null, glif = GLIFLER[16],
            liderler = listOf(Lider("Yavuz Ağıralioğlu")), sandalyeSirasi = 17,
        ),
    )

    fun bul(id: String): Parti = tumu.first { it.id == id }
}

/** Logosu olmayan partiler için nötr glifler (tasarım dosyalarından, sandalye sırasıyla). */
val GLIFLER = listOf(
    "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0",
    "M12 3l9 9-9 9-9-9z",
    "M12 4l8 15H4z",
    "M8 5v14M16 5v14",
    "M12 3l8 4.5v9L12 21l-8-4.5v-9z",
    "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
    "M5 5h14v14H5z",
    "M4.5 12a2 2 0 1 0 4 0a2 2 0 1 0 -4 0M10 12a2 2 0 1 0 4 0a2 2 0 1 0 -4 0M15.5 12a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
    "M3 12c3-4 6-4 9 0s6 4 9 0",
    "M4 16a8 8 0 0 1 16 0z",
    "M5 5h6v6H5zM13 5h6v6h-6zM5 13h6v6H5zM13 13h6v6h-6z",
    "M12 3l9 7-3.5 10h-11L3 10z",
    "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0M8 12h8",
    "M5 8h14M5 12h14M5 16h14",
    "M8 3h8l5 5v8l-5 5H8l-5-5V8z",
    "M4 6h16l-8 14z",
    "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0M9 9h6v6H9z",
)

object Anketler {
    val genelSecim = Anket("genel-secim", "Genel Seçim Anketi", AnketTuru.DESTEK_ASLA, "Genel")
    val gencSecmen = Anket("genc-secmen", "Genç Seçmen Anketi", AnketTuru.DESTEK_ASLA, "Genç Seçmen")
    val liderTercihi = Anket("lider-tercihi", "Lider Tercihi Anketi", AnketTuru.YALNIZ_DESTEK, "Lider Tercihi")
    val aslaOyVermem = Anket("asla-oy-vermem", "Asla Oy Vermem Anketi", AnketTuru.YALNIZ_ASLA, "Asla Oy Vermem")
    val tumu = listOf(genelSecim, gencSecmen, liderTercihi, aslaOyVermem)

    fun bul(id: String): Anket = tumu.firstOrNull { it.id == id } ?: genelSecim
}

/**
 * Şimdilik gerçek veri yok: tüm partiler eşit, sembolik değer alır ve bu açıkça belirtilir.
 * Hiçbir parti önde ya da geride görünmez; sıra Meclis sandalye sırasıdır.
 */
object SembolikVeri {
    const val ESIT_DEGER = 0.5f

    fun sonuclar(anket: Anket): List<Sonuc> = Partiler.tumu.map { parti ->
        val destek = anket.tur != AnketTuru.YALNIZ_ASLA
        val asla = anket.tur != AnketTuru.YALNIZ_DESTEK
        Sonuc(
            partiId = parti.id,
            partiDestek = if (destek) ESIT_DEGER else null,
            liderDestek = if (destek) ESIT_DEGER else null,
            partiyeAsla = if (asla) ESIT_DEGER else null,
            lidereAsla = if (asla) ESIT_DEGER else null,
        )
    }

    fun durum(): AnketDurumu = AnketDurumu(
        katilimciSayisi = 0, sonGuncelleme = null, sembolikMi = true,
        yayinYasagiMi = false, yayinYasagiBitis = null,
    )

    /**
     * Şeritteki partiler, anketin durumuna göre:
     * - Yayın yasağında boş: logoların sırası bile sonucu ele verir.
     * - Henüz oy yokken değerler boş, sıra Meclis sandalye sırası.
     */
    fun serit(anket: Anket = Anketler.genelSecim, durum: AnketDurumu = durum()): List<SeritOgesi> =
        siralama(anket, durum).take(8)

    /** Tüm partiler, şeritle aynı kurallarla (anket ayrıntısındaki sonuç listesi). */
    fun siralama(anket: Anket = Anketler.genelSecim, durum: AnketDurumu = durum()): List<SeritOgesi> {
        val hepsi = Partiler.tumu.size
        return when (durum.gorunum) {
            SonucGorunumu.YAYIN_DURDURULDU -> emptyList()
            SonucGorunumu.HENUZ_OY_YOK -> seritSirasi(Partiler.tumu, Partiler.tumu.map { Sonuc(it.id, null, null, null, null) }, hepsi)
            else -> seritSirasi(Partiler.tumu, sonuclar(anket), hepsi)
        }
    }
}

/**
 * Ekran 6 durumlarının örnekleri. Sunucu gelene kadar yalnızca önizleme, testler ve hata ayıklama
 * sürümündeki durum önizlemesi için; gerçek durum sunucudan gelecek.
 */
object OrnekDurumlar {
    fun sembolik() = SembolikVeri.durum()

    fun henuzOyYok() = AnketDurumu(
        katilimciSayisi = 0, sonGuncelleme = null, sembolikMi = false,
        yayinYasagiMi = false, yayinYasagiBitis = null,
    )

    fun yayinDurduruldu(bitis: Long? = null) = AnketDurumu(
        katilimciSayisi = 0, sonGuncelleme = null, sembolikMi = true,
        yayinYasagiMi = true, yayinYasagiBitis = bitis,
    )

    fun cevrimdisi(sonGuncelleme: Long?) = SembolikVeri.durum().copy(sonGuncelleme = sonGuncelleme, baglantiVar = false)
}
