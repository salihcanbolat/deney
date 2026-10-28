package com.salihcanbolat.secimanketi.tema

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Uygulamanın tek teması (yalnızca koyu). */
@Composable
fun SecimTemasi(hareketAzalt: Boolean? = null, icerik: @Composable () -> Unit) {
    val context = LocalContext.current
    val azalt = hareketAzalt ?: remember(context) { Hareket.hareketAzaltildiMi(context) }
    CompositionLocalProvider(LocalHareketAzalt provides azalt, content = icerik)
}
