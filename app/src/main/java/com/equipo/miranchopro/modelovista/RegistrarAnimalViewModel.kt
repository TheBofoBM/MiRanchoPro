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
    var tipo by mutableStateOf("Becerro") // Por defecto para nacimientos
    var raza by mutableStateOf("Serrana")
    var edad by mutableStateOf("0 meses")
    var peso by mutableStateOf("")
    var caracteristica by mutableStateOf("")
    var origen by mutableStateOf("De parto")
    
    // Para completar registros rápidos
    private var esEdicionPendiente by mutableStateOf(false)
    private var idTemporalOriginal by mutableStateOf<String?>(null)
    var horaNacimientoRegistrada by mutableStateOf<String?>(null)

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

    fun cargarParaCompletar(idTemp: String) {
        viewModelScope.launch {
            repositorio.getAnimalById(idTemp)?.let { animal ->
                esEdicionPendiente = true
                idTemporalOriginal = idTemp
                idArete = "" // El usuario debe poner el arete real
                tipo = animal.tipo
                origen = animal.origen
                horaNacimientoRegistrada = animal.horaNacimiento
            }
        }
    }

    fun registrarAnimal() {
        if (idArete.isBlank() || peso.isBlank() || tipo.isBlank()) {
            mensajeError = "El arete, Tipo y Peso son obligatorios"
            return
        }

        if (idArete.startsWith("TEMP-")) {
            mensajeError = "Por favor asigna un número de arete válido"
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
            val animalAGuardar = Animal(
                idArete = idArete,
                nombre = nombre,
                tipo = tipo,
                raza = if (raza.isBlank()) "No especificada" else raza,
                edad = if (edad.isBlank()) "Recién nacido" else edad,
                peso = pesoDouble,
                caracteristica = caracteristica,
                origen = origen,
                color = "No especificado",
                marcas = "",
                estado = "Sano",
                horaNacimiento = horaNacimientoRegistrada
            )
            
            val resultado = if (esEdicionPendiente && idTemporalOriginal != null) {
                // Si venimos de un pendiente, borramos el temporal e insertamos el nuevo
                // (O podríamos actualizar la PK si Room lo permitiera fácilmente, pero borrar e insertar es más limpio para cambio de ID)
                val temporal = repositorio.getAnimalById(idTemporalOriginal!!)
                if (temporal != null) repositorio.eliminarAnimal(temporal)
                repositorio.registrarAnimal(animalAGuardar)
            } else {
                repositorio.registrarAnimal(animalAGuardar)
            }
            
            estaCargando = false
            
            resultado.onSuccess {
                val msg = if (esEdicionPendiente) "¡Registro completado con éxito!" else "Animal #$idArete registrado"
                limpiarCampos()
                _eventoUI.emit(EventoUI.Exito(msg))
            }.onFailure { exception ->
                mensajeError = "Error: El arete ya existe o hubo un fallo en la BD"
                _eventoUI.emit(EventoUI.Error(mensajeError!!))
            }
        }
    }

    fun limpiarCampos() {
        idArete = ""
        nombre = ""
        raza = "Serrana"
        edad = ""
        peso = ""
        caracteristica = ""
        origen = "De parto"
        esEdicionPendiente = false
        idTemporalOriginal = null
        horaNacimientoRegistrada = null
        mensajeError = null
    }
}
