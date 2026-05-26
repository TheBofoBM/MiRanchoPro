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
import java.text.SimpleDateFormat
import java.util.*

class RegistrarAnimalViewModel(
    private val repositorio: AnimalRepository
) : ViewModel() {

    // Campos del formulario
    var idArete by mutableStateOf("")
    var nombre by mutableStateOf("")
    var tipo by mutableStateOf("Becerro")
    var raza by mutableStateOf("Serrana")
    var fechaNacimiento by mutableStateOf("") // Formato dd/MM/yyyy
    var peso by mutableStateOf("")
    var caracteristica by mutableStateOf("")
    var origen by mutableStateOf("De parto")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")
    var ubicacion by mutableStateOf("Lote A")

    // Estados internos
    var esEdicionPendiente by mutableStateOf(false)
        private set
    private var idTemporalOriginal by mutableStateOf<String?>(null)
    var horaNacimientoRegistrada by mutableStateOf("00:00:00")

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

    // Lógica para completar nacimientos registrados con el sensor (Shake)
    fun cargarParaCompletar(idTemp: String) {
        viewModelScope.launch {
            repositorio.getAnimalById(idTemp)?.let { animal ->
                esEdicionPendiente = true
                idTemporalOriginal = idTemp
                idArete = ""
                tipo = animal.tipo
                origen = "De parto" // Automático para nacimientos rápidos
                ubicacion = "Lote recién nacidos" // Automático según requerimiento
                horaNacimientoRegistrada = animal.horaNacimiento ?: "00:00:00"

                // La fecha de nacimiento es la fecha en que se detectó el sensor
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaNacimiento = sdf.format(Date(animal.fechaRegistro))
            }
        }
    }

    private fun calcularEdad(fecha: String): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaNac = sdf.parse(fecha) ?: return "Desconocida"
            val hoy = Calendar.getInstance()
            val nac = Calendar.getInstance().apply { time = fechaNac }

            var anios = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR)
            var meses = hoy.get(Calendar.MONTH) - nac.get(Calendar.MONTH)

            if (meses < 0) {
                anios--
                meses += 12
            }

            when {
                anios > 0 -> "$anios años, $meses meses"
                meses > 0 -> "$meses meses"
                else -> "Recién nacido"
            }
        } catch (e: Exception) {
            "Desconocida"
        }
    }

    fun registrarAnimal() {
        if (idArete.isBlank() || peso.isBlank() || fechaNacimiento.isBlank()) {
            mensajeError = "Arete, Peso y Fecha de Nacimiento son obligatorios"
            return
        }

        val pesoDouble = peso.toDoubleOrNull()
        if (pesoDouble == null) {
            mensajeError = "El peso debe ser un número válido"
            return
        }

        estaCargando = true
        mensajeError = null

        viewModelScope.launch {
            val animalAGuardar = Animal(
                idArete = idArete.trim(),
                nombre = nombre.trim(),
                tipo = tipo,
                raza = if (raza.isBlank()) "Serrana" else raza,
                edad = calcularEdad(fechaNacimiento), // Calculada automáticamente
                peso = pesoDouble,
                caracteristica = caracteristica,
                origen = origen,
                color = if (color.isBlank()) "No especificado" else color,
                marcas = marcas,
                ubicacion = ubicacion,
                estado = "Sano",
                horaNacimiento = horaNacimientoRegistrada,
                fechaRegistro = System.currentTimeMillis() // Se guarda automático para el reporte
            )

            val resultado = if (esEdicionPendiente && idTemporalOriginal != null) {
                // Borramos el registro temporal "TEMP-..." e insertamos el real
                repositorio.getAnimalById(idTemporalOriginal!!)?.let {
                    repositorio.eliminarAnimal(it)
                }
                repositorio.registrarAnimal(animalAGuardar)
            } else {
                repositorio.registrarAnimal(animalAGuardar)
            }

            estaCargando = false

            resultado.onSuccess {
                val msg = if (esEdicionPendiente) "¡Nacimiento completado!" else "Animal registrado"
                limpiarCampos()
                _eventoUI.emit(EventoUI.Exito(msg))
            }.onFailure {
                mensajeError = "Error: El arete ya existe o fallo de conexión"
                _eventoUI.emit(EventoUI.Error(mensajeError!!))
            }
        }
    }

    fun limpiarCampos() {
        idArete = ""
        nombre = ""
        tipo = "Becerro"
        raza = "Serrana"
        fechaNacimiento = ""
        peso = ""
        caracteristica = ""
        origen = "De parto"
        color = ""
        marcas = ""
        ubicacion = "Lote A"
        esEdicionPendiente = false
        idTemporalOriginal = null
        horaNacimientoRegistrada = "00:00:00"
    }
}