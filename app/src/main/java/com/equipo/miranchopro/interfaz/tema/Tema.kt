package com.equipo.miranchopro.interfaz.tema
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val Claro=lightColorScheme(primary=Color(0xFF00695C),onPrimary=Color.White,primaryContainer=Color(0xFFCBEDE4),onPrimaryContainer=Color(0xFF00382F),secondary=Color(0xFF3C665C),secondaryContainer=Color(0xFFD7EBDD),onSecondaryContainer=Color(0xFF203E2C),surfaceVariant=Color(0xFFECF3EE),outlineVariant=Color(0xFFC6D5CA),background=Color(0xFFF6F9F7),surface=Color.White,onSurface=Color(0xFF17231E),onSurfaceVariant=Color(0xFF485A52),outline=Color(0xFF6C7E75))
private val Oscuro=darkColorScheme(primary=Color(0xFF80D5BE),onPrimary=Color(0xFF00382F),primaryContainer=Color(0xFF005143),onPrimaryContainer=Color(0xFFA4F2DA),secondary=Color(0xFFACD1C1),secondaryContainer=Color(0xFF2C4436),onSecondaryContainer=Color(0xFFD5EADD),surfaceVariant=Color(0xFF22352B),outlineVariant=Color(0xFF3F5848),background=Color(0xFF101814),surface=Color(0xFF17231E),onSurface=Color(0xFFE2ECE5),onSurfaceVariant=Color(0xFFBBCBC0),outline=Color(0xFF8B9E91))
@Composable
fun TemaMiRanchoPro(oscuro: Boolean=isSystemInDarkTheme(), colorDinamico: Boolean=false, contenido: @Composable () -> Unit) {
    val context=LocalContext.current;val view=LocalView.current
    LaunchedEffect(Unit){PreferenciasApp.cargar(context)}
    val modo by PreferenciasApp.tema.collectAsState()
    val esOscuro=when(modo){"oscuro"->true;"claro"->false;else->oscuro}
    SideEffect { (context as? Activity)?.window?.let { WindowCompat.getInsetsController(it,view).apply {isAppearanceLightStatusBars=!esOscuro;isAppearanceLightNavigationBars=!esOscuro} } }
    MaterialTheme(colorScheme=if(esOscuro) Oscuro else Claro,typography=Tipografia,content=contenido)
}
