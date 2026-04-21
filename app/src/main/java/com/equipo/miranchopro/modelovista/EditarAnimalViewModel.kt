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

class EditarAnimalViewModel(
    private val repositorio: AnimalRepository = AnimalRepository()
) : ViewModel() {

    var idArete by mutableStateOf("")
        private set

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

    fun cargarAnimal(id: String) {
        viewModelScope.launch {
            estaCargando = true
            val animal = repositorio.getAnimalById(id)
            if (animal != null) {
                idArete = animal.idArete
                peso = animal.peso.toString()
                color = animal.color
                marcas = animal.marcas
            }
            estaCargando = false
        }
    }

    fun actualizarAnimal() {
        if (peso.isBlank() || color.isBlank()) {
            mensajeError = "El peso y el color son obligatorios"
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
            val animalActualizado = Animal(
                idArete = idArete,
                peso = pesoDouble,
                color = color,
                marcas = marcas
            )
            val exito = repositorio.updateAnimal(animalActualizado)
            estaCargando = false
            if (exito) {
                _eventoUI.emit(EventoUI.Exito)
            } else {
                _eventoUI.emit(EventoUI.Error("Error al actualizar el animal"))
            }
        }
    }
}
