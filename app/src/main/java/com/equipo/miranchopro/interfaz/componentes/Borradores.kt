package com.equipo.miranchopro.interfaz.componentes

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

val LocalRutaBorrador = staticCompositionLocalOf { "" }
val LocalFormularioBorrador = staticCompositionLocalOf<String?> { null }
val LocalOperacionBorrador = staticCompositionLocalOf<com.equipo.miranchopro.modelovista.OperacionViewModel?> { null }
@Composable fun ConBorradoresDe(operacion: com.equipo.miranchopro.modelovista.OperacionViewModel,contenido: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOperacionBorrador provides operacion,content=contenido)
}
object Borradores {
    private fun prefs(c: Context)=c.getSharedPreferences("borradores",Context.MODE_PRIVATE)
    fun leer(c: Context,clave: String): String?=prefs(c).getString(clave,null)
    fun guardar(c: Context,clave: String,valor: String) { prefs(c).edit().putString(clave,valor).apply() }
    fun borrar(c: Context,prefijo: String) { val editor=prefs(c).edit();prefs(c).all.keys.filter { it.startsWith(prefijo) }.forEach { editor.remove(it) };editor.apply() }
}

/** Persiste fuera de la Activity para recuperar el formulario tras terminar el proceso. */
@Composable
fun BorradorAnimal(clave: String,campos: Map<String,String>,restaurar: (Map<String,String>) -> Unit) {
    val context=LocalContext.current
    var cargado by remember(clave) { mutableStateOf(false) }
    LaunchedEffect(clave) {
        Borradores.leer(context,clave)?.let { texto -> runCatching { val json=JSONObject(texto);restaurar(json.keys().asSequence().associateWith { json.getString(it) }) } }
        cargado=true
    }
    LaunchedEffect(campos,cargado) { if(cargado) Borradores.guardar(context,clave,JSONObject(campos).toString()) }
}
