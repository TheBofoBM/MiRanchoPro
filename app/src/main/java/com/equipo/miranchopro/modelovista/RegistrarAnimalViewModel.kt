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
    var nombre by mutableStateOf("")
    var tipo by mutableStateOf("Vaca")
    var raza by mutableStateOf("")
    var edad by mutableStateOf("")
    var peso by mutableStateOf("")
    var caracteristica by mutableStateOf("")
    var origen by mutableStateOf("De parto")
    
    val tiposDisponibles = listOf("Vaca", "Toro", "Becerro", "Novillo", "Vaquilla")
    val origenesDisponibles = listOf("De parto", "Comprada")

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
        if (idArete.isBlank() || peso.isBlank() || tipo.isBlank()) {
            mensajeError = "Tag, Tipo y Peso son obligatorios"
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
                nombre = nombre,
                tipo = tipo,
                raza = if (raza.isBlank()) "No especificada" else raza,
                edad = if (edad.isBlank()) "No especificada" else edad,
                peso = pesoDouble,
                caracteristica = caracteristica,
                origen = origen,
                color = "No especificado",
                marcas = "",
                estado = "Sano"
            )
            
            val resultado = repositorio.registrarAnimal(nuevoAnimal)
            
            estaCargando = false
            
            resultado.onSuccess {
                val msg = "Animal #$idArete registrado exitosamente en $tipo"
                limpiarCampos()
                _eventoUI.emit(EventoUI.Exito(msg))
            }.onFailure { exception ->
                mensajeError = "Error: El tag ya existe o hubo un fallo en la BD"
                _eventoUI.emit(EventoUI.Error(mensajeError!!))
            }
        }
    }

    fun limpiarCampos() {
        idArete = ""
        nombre = ""
        raza = ""
        edad = ""
        peso = ""
        caracteristica = ""
        origen = "De parto"
        mensajeError = null
    }
}
