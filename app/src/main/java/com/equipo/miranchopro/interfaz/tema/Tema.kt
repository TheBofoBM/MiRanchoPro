package com.equipo.miranchopro.interfaz.tema

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EsquemaColoresOscuro = darkColorScheme(
    primary = Morado80,
    secondary = MoradoGris80,
    tertiary = Rosa80
)

private val EsquemaColoresClaro = lightColorScheme(
    primary = Morado40,
    secondary = MoradoGris40,
    tertiary = Rosa40
)

@Composable
fun TemaMiRanchoPro(
    oscuro: Boolean = isSystemInDarkTheme(),
    colorDinamico: Boolean = true,
    contenido: @Composable () -> Unit
) {
    val esquemaColores = when {
        colorDinamico && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val contexto = LocalContext.current
            if (oscuro) dynamicDarkColorScheme(contexto) else dynamicLightColorScheme(contexto)
        }
        oscuro -> EsquemaColoresOscuro
        else -> EsquemaColoresClaro
    }

    MaterialTheme(
        colorScheme = esquemaColores,
        typography = Tipografia,
        content = contenido
    )
}
