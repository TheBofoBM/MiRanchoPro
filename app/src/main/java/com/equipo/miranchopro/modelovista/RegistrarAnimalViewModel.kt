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
    private val repositorio: AnimalRepository
) : ViewModel() {

    var idArete by mutableStateOf("")
    var tipo by mutableStateOf("Vaca")
    var peso by mutableStateOf("")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")
    
    val tiposDisponibles = listOf("Vaca", "Toro", "Becerro", "Novillo", "Vaquilla")

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    private val _eventoUI = MutableSharedFlow<EventoUI>()
    val eventoUI = _eventoUI.asSharedFlow()

    sealed class EventoUI {
        data class Exito(val mensaje: String) : EventoUI()
        data class Error(val mensaje: String) : EventoUI()
    }

    fun registrarAnimal() {
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
                tipo = tipo,
                peso = pesoDouble,
                color = color,
                marcas = marcas
            )
            
            val resultado = repositorio.registrarAnimal(nuevoAnimal)
            
            estaCargando = false
            
            resultado.onSuccess {
                val msg = "Animal agregado exitosamente en la categoría $tipo"
                limpiarCampos()
                _eventoUI.emit(EventoUI.Exito(msg))
            }.onFailure { exception ->
                mensajeError = "Error al registrar: el arete ya existe o hubo un fallo en la base de datos"
                _eventoUI.emit(EventoUI.Error(mensajeError!!))
            }
        }
    }

    private fun limpiarCampos() {
        idArete = ""
        // No limpiamos el tipo por si registra varios del mismo
        peso = ""
        color = ""
        marcas = ""
        mensajeError = null
    }
}
