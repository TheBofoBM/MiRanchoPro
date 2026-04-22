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

class RegistrarAnimalViewModel(
    private val repositorio: AnimalRepository = AnimalRepository()
) : ViewModel() {

    var idArete by mutableStateOf("")
    var peso by mutableStateOf("")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    private val _eventoUI = MutableSharedFlow<EventoUI>()
    val eventoUI = _eventoUI.asSharedFlow()

    sealed class EventoUI {
        object Exito : EventoUI()
        data class Error(val mensaje: String) : EventoUI()
    }

    fun registrarAnimal() {
        // Validación básica
        if (idArete.isBlank() || peso.isBlank() || color.isBlank()) {
            mensajeError = "El arete, peso y color son obligatorios"
            return
        }

        val pesoDouble = peso.toDoubleOrNull()
        if (pesoDouble == null) {
            mensajeError = "El peso debe ser un número válido"
            return
        }

        mensajeError = null
        estaCargando = true

        viewModelScope.launch {
            val nuevoAnimal = Animal(
                idArete = idArete,
                peso = pesoDouble,
                color = color,
                marcas = marcas
            )
            
            val resultado = repositorio.registrarAnimal(nuevoAnimal)
            
            estaCargando = false
            
            resultado.onSuccess {
                limpiarCampos()
                _eventoUI.emit(EventoUI.Exito)
            }.onFailure { exception ->
                // CP-05.2 y CP-05.3: El repositorio devuelve el error específico
                mensajeError = exception.message
                _eventoUI.emit(EventoUI.Error(exception.message ?: "Error desconocido"))
            }
        }
    }

    private fun limpiarCampos() {
        idArete = ""
        peso = ""
        color = ""
        marcas = ""
        mensajeError = null
    }

    fun descartarError() {
        mensajeError = null
    }
}
