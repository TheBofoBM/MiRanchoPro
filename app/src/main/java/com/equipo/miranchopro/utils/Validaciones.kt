package com.equipo.miranchopro.utils

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Validaciones {
    fun correo(valor: String) = valor.trim().lowercase(Locale.ROOT)
    fun correoValido(valor: String) = Regex("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$").matches(correo(valor)) && valor.trim().length <= 254
    fun decimal(valor: String): Double? {
        val texto = valor.trim().replace(',', '.')
        if (!Regex("^\\d+(?:\\.\\d+)?$").matches(texto)) return null
        return texto.toDoubleOrNull()?.takeIf { it.isFinite() }
    }
    fun fecha(valor: String): Long? {
        val texto = valor.trim()
        if (!Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(texto)) return null
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
        val posicion = ParsePosition(0)
        return formato.parse(texto, posicion)?.time?.takeIf { posicion.index == texto.length }
    }
    fun fechaTexto(valor: Long?) = valor?.let { SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Date(it)) } ?: ""
    fun finDeHoy(ahora: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
        timeInMillis = ahora; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    fun errorFecha(valor: String, etiqueta: String, opcional: Boolean = false, permitirFutura: Boolean = false): String? {
        if (valor.isBlank()) return if (opcional) null else "$etiqueta es obligatoria"
        val fecha = fecha(valor) ?: return "Usa una fecha válida con formato dd/mm/aaaa"
        if (fecha < fecha("01/01/1900")!!) return "La fecha debe ser posterior a 1899"
        return if (!permitirFutura && fecha > finDeHoy()) "La fecha no puede estar en el futuro" else null
    }
    fun erroresAnimal(arete: String, peso: String, nacimiento: String): Map<String, String> = buildMap {
        if (arete.trim().isEmpty() || arete.trim().length > 80 || arete.any { it.isISOControl() }) put("arete", "Ingresa un arete de 1 a 80 caracteres")
        if (decimal(peso)?.let { it > 0.0 } != true) put("peso", "Ingresa un peso mayor que cero en kg")
        errorFecha(nacimiento, "La fecha de nacimiento", opcional = true)?.let { put("fecha", it) }
    }
    fun edad(nacimiento: Long?, ahora: Long = System.currentTimeMillis()): String {
        if (nacimiento == null || nacimiento > ahora) return "Desconocida"
        val inicio = Calendar.getInstance().apply { timeInMillis = nacimiento }
        val hoy = Calendar.getInstance().apply { timeInMillis = ahora }
        var meses = (hoy.get(Calendar.YEAR) - inicio.get(Calendar.YEAR)) * 12 + hoy.get(Calendar.MONTH) - inicio.get(Calendar.MONTH)
        if (hoy.get(Calendar.DAY_OF_MONTH) < inicio.get(Calendar.DAY_OF_MONTH)) meses--
        return when { meses >= 12 -> "${meses / 12} ${if(meses / 12 == 1) "año" else "años"}, ${meses % 12} meses"; meses > 0 -> "$meses ${if(meses==1) "mes" else "meses"}"; else -> "Menor de un mes" }
    }
    fun mensaje(error: Throwable, respaldo: String = "No se pudo guardar. Inténtalo de nuevo.") =
        if (error is IllegalArgumentException) error.message ?: respaldo else respaldo
}
