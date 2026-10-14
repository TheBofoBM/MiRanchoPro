package com.equipo.miranchopro.interfaz.tema
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
object PreferenciasApp {
    val tema=MutableStateFlow("sistema")
    fun cargar(context: Context) { tema.value=context.getSharedPreferences("preferencias",Context.MODE_PRIVATE).getString("tema","sistema") ?: "sistema" }
    fun elegir(context: Context, valor: String) {
        require(valor in listOf("sistema","claro","oscuro"))
        context.getSharedPreferences("preferencias",Context.MODE_PRIVATE).edit().putString("tema",valor).apply()
        tema.value=valor
    }
}
