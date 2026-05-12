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
    private val repositorio: AnimalRepository
) : ViewModel() {

    var idArete by mutableStateOf("")
        private set

    var peso by mutableStateOf("")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")
    var tipo by mutableStateOf("")
    var raza by mutableStateOf("")
    var edad by mutableStateOf("")
    var ubicacion by mutableStateOf("")
    var estado by mutableStateOf("")

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    // Estados para el proceso de Baja
    var mostrarDialogoBaja by mutableStateOf(false)
    var motivoBaja by mutableStateOf("")
    var situacionMuerte by mutableStateOf("")
    var otroMotivoMuerte by mutableStateOf("")

    private val _eventoUI = MutableSharedFlow<EventoUI>()
    val eventoUI = _eventoUI.asSharedFlow()

    sealed class EventoUI {
        object Exito : EventoUI()
        object BajaExitosa : EventoUI()
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
                tipo = animal.tipo
                raza = animal.raza
                edad = animal.edad
                ubicacion = animal.ubicacion
                estado = animal.estado
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
                marcas = marcas,
                tipo = tipo,
                raza = raza,
                edad = edad,
                ubicacion = ubicacion,
                estado = estado
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

    fun confirmarBaja() {
        if (motivoBaja.isBlank()) return
        
        viewModelScope.launch {
            estaCargando = true
            
            // CP-06.2: Simulación de validación de tareas/tratamientos pendientes
            // En el futuro, aquí consultaríamos al repositorio médico
            val tieneTratamientosActivos = false 
            
            if (tieneTratamientosActivos) {
                _eventoUI.emit(EventoUI.Error("No se puede dar de baja: El animal tiene tratamientos médicos activos."))
                estaCargando = false
                return@launch
            }

            val animal = repositorio.getAnimalById(idArete)
            if (animal != null) {
                val detalleBaja = when (motivoBaja) {
                    "Muerto" -> {
                        val situacion = if (situacionMuerte == "Otro") otroMotivoMuerte else situacionMuerte
                        "Baja por muerte ($situacion)"
                    }
                    else -> "Baja por $motivoBaja"
                }

                // Actualizamos el estado a "Baja"
                val animalDeBaja = animal.copy(
                    estado = "Baja",
                    marcas = "${animal.marcas} | Detalle: $detalleBaja"
                )
                
                val exito = repositorio.updateAnimal(animalDeBaja)
                estaCargando = false
                if (exito) {
                    mostrarDialogoBaja = false
                    _eventoUI.emit(EventoUI.BajaExitosa)
                } else {
                    _eventoUI.emit(EventoUI.Error("Error al procesar la baja"))
                }
            }
        }
    }
}
