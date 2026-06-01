package com.equipo.miranchopro.modelovista

import android.content.Context
import android.net.Uri
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
import java.io.File
import java.io.FileOutputStream
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
    var fechaNacimiento by mutableStateOf("")
    var peso by mutableStateOf("")
    var caracteristica by mutableStateOf("")
    var origen by mutableStateOf("De parto")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")
    var ubicacion by mutableStateOf("Lote A")
    
    // NUEVO: Estado para la foto
    var fotoUri by mutableStateOf<Uri?>(null)
    var fotoPath by mutableStateOf<String?>(null)

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

    fun cargarParaCompletar(idTemp: String) {
        viewModelScope.launch {
            repositorio.getAnimalById(idTemp)?.let { animal ->
                esEdicionPendiente = true
                idTemporalOriginal = idTemp
                idArete = ""
                tipo = animal.tipo
                origen = "De parto"
                ubicacion = "Lote recién nacidos"
                horaNacimientoRegistrada = animal.horaNacimiento ?: "00:00:00"
                fotoPath = animal.fotoPath // Cargar foto si ya tiene

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaNacimiento = sdf.format(Date(animal.fechaRegistro))
            }
        }
    }

    private fun guardarImagenEnInterno(context: Context, uri: Uri): String? {
        return try {
            val fileName = "animal_${idArete.ifBlank { "temp_" + System.currentTimeMillis() }}.jpg"
            val file = File(context.filesDir, "fotos_animales").apply { if (!exists()) mkdirs() }
            val destFile = File(file, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun registrarAnimal(context: Context) {
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
        
        // Si hay una nueva URI de foto, la guardamos físicamente
        fotoUri?.let { uri ->
            fotoPath = guardarImagenEnInterno(context, uri)
        }

        viewModelScope.launch {
            val animalAGuardar = Animal(
                idArete = idArete.trim(),
                nombre = nombre.trim(),
                tipo = tipo,
                raza = if (raza.isBlank()) "Serrana" else raza,
                edad = calcularEdad(fechaNacimiento),
                peso = pesoDouble,
                caracteristica = caracteristica,
                origen = origen,
                color = if (color.isBlank()) "No especificado" else color,
                marcas = marcas,
                ubicacion = ubicacion,
                estado = "Sano",
                horaNacimiento = horaNacimientoRegistrada,
                fechaRegistro = System.currentTimeMillis(),
                fotoPath = fotoPath // Guardamos la ruta
            )

            val resultado = if (esEdicionPendiente && idTemporalOriginal != null) {
                repositorio.getAnimalById(idTemporalOriginal!!)?.let { repositorio.eliminarAnimal(it) }
                repositorio.registrarAnimal(animalAGuardar)
            } else {
                repositorio.registrarAnimal(animalAGuardar)
            }

            estaCargando = false
            resultado.onSuccess {
                limpiarCampos()
                _eventoUI.emit(EventoUI.Exito("Registro guardado con éxito"))
            }.onFailure {
                _eventoUI.emit(EventoUI.Error("Error al guardar: ${it.message}"))
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
            if (meses < 0) { anios--; meses += 12 }
            when {
                anios > 0 -> "$anios años, $meses meses"
                meses > 0 -> "$meses meses"
                else -> "Recién nacido"
            }
        } catch (e: Exception) { "Desconocida" }
    }

    fun limpiarCampos() {
        idArete = ""; nombre = ""; tipo = "Becerro"; raza = "Serrana"
        fechaNacimiento = ""; peso = ""; caracteristica = ""; origen = "De parto"
        color = ""; marcas = ""; ubicacion = "Lote A"
        esEdicionPendiente = false; idTemporalOriginal = null
        horaNacimientoRegistrada = "00:00:00"
        fotoUri = null; fotoPath = null
    }
}
