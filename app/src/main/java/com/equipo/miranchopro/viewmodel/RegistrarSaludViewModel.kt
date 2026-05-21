package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.RegistroSalud
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.SaludRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class RegistrarSaludViewModel(
    private val saludRepo: SaludRepository,
    private val animalRepo: AnimalRepository
) : ViewModel() {

    var idArete by mutableStateOf("")
    var tipo by mutableStateOf("Vacuna")
    var medicamento by mutableStateOf("")
    var fechaAplicacion by mutableStateOf("") // dd/mm/aaaa
    var proximaFecha by mutableStateOf("")    // dd/mm/aaaa
    var veterinario by mutableStateOf("")
    var notas by mutableStateOf("")

    var animalesDisponibles by mutableStateOf<List<String>>(emptyList())
        private set

    val tiposDisponibles = listOf("Vacuna", "Tratamiento", "Desparasitación", "Revisión")

    init {
        viewModelScope.launch {
            animalRepo.obtenerTodos().collect { lista ->
                animalesDisponibles = lista.filter { it.estado != "Baja" }.map { it.idArete }
            }
        }
    }

    fun guardarRegistro(alTerminar: () -> Unit, alFallar: (String) -> Unit) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        try {
            // Intentamos convertir las cadenas de fecha a Timestamps de tipo Long
            val dateAplicacion = sdf.parse(fechaAplicacion) ?: throw Exception("Fecha de aplicación inválida")
            val dateProxima = if (proximaFecha.isNotBlank()) sdf.parse(proximaFecha) else null

            viewModelScope.launch {
                val nuevoRegistro = RegistroSalud(
                    idArete = idArete,
                    fecha = dateAplicacion.time,
                    proximaFecha = dateProxima?.time,
                    tipo = tipo,
                    medicamento = medicamento,
                    veterinario = veterinario,
                    notas = if (notas.isBlank()) "Sin complicaciones" else notas
                )
                saludRepo.insertar(nuevoRegistro)
                alTerminar()
            }
        } catch (e: Exception) {
            alFallar("Error en el formato de fecha (debe ser dd/mm/aaaa)")
        }
    }
}