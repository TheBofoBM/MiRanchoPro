package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AltaAnimalViewModel(
    private val repositorio: AnimalRepository
) : ViewModel() {

    var idArete by mutableStateOf("")
    var peso by mutableStateOf("")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")

    // Valores por defecto
    var tipo by mutableStateOf("Vaca")
    var raza by mutableStateOf("Serrana")
    var edad by mutableStateOf("Desconocida")
    var ubicacion by mutableStateOf("Lote A")

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    private val _eventoUI = MutableSharedFlow<EventoUI>()
    val eventoUI = _eventoUI.asSharedFlow()

    sealed class EventoUI {
        object RegistroExitoso : EventoUI()
        data class Error(val mensaje: String) : EventoUI()
    }

    fun registrarAnimal() {
        if (idArete.isBlank() || peso.isBlank() || color.isBlank()) {
            mensajeError = "El ID Arete, peso y color son obligatorios"
            return
        }

        val pesoDouble = peso.toDoubleOrNull()
        if (pesoDouble == null) {
            mensajeError = "El peso debe ser un número válido"
            return
        }

        mensajeError = null
        viewModelScope.launch {
            estaCargando = true

            val nuevoAnimal = Animal(
                idArete = idArete,
                peso = pesoDouble,
                color = color,
                marcas = marcas,
                tipo = tipo,
                raza = raza,
                edad = edad,
                ubicacion = ubicacion,
                estado = "Sano"
            )

            // CORRECCIÓN: El método correcto en tu repositorio es registrarAnimal
            val resultado = repositorio.registrarAnimal(nuevoAnimal)
            estaCargando = false

            resultado.onSuccess {
                _eventoUI.emit(EventoUI.RegistroExitoso)
            }.onFailure {
                // Room lanzará una excepción si el Arete (PrimaryKey) ya existe
                _eventoUI.emit(EventoUI.Error("El Arete '$idArete' ya está registrado o hubo un error."))
            }
        }
    }
}