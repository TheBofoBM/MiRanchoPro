package com.equipo.miranchopro.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
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

class EditarAnimalViewModel(
    private val repositorio: AnimalRepository
) : ViewModel() {

    var idArete by mutableStateOf("")
        private set

    var nombre by mutableStateOf("")
    var peso by mutableStateOf("")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")
    var tipo by mutableStateOf("")
    var raza by mutableStateOf("")
    var edad by mutableStateOf("")
    var ubicacion by mutableStateOf("")
    var origen by mutableStateOf("")
    var estado by mutableStateOf("")
    var caracteristica by mutableStateOf("")
    var fechaNacimiento by mutableStateOf("")
    var horaNacimiento by mutableStateOf("")
    
    var fotoUri by mutableStateOf<Uri?>(null)
    var fotoPath by mutableStateOf<String?>(null)
    
    // URI temporal para la cámara
    private var tempCameraUri: Uri? = null

    var estaCargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

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
                nombre = animal.nombre
                peso = animal.peso.toString()
                color = animal.color
                marcas = animal.marcas
                tipo = animal.tipo
                raza = animal.raza
                edad = animal.edad
                ubicacion = animal.ubicacion
                origen = animal.origen
                estado = animal.estado
                caracteristica = animal.caracteristica
                fotoPath = animal.fotoPath
                
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaNacimiento = sdf.format(Date(animal.fechaRegistro))
                horaNacimiento = animal.horaNacimiento ?: "00:00:00"
            }
            estaCargando = false
        }
    }

    fun crearUriTemporalCamara(context: Context): Uri {
        val directory = File(context.getExternalFilesDir(null), "FotoAnimales")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File.createTempFile("IMG_EDIT_", ".jpg", directory)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        tempCameraUri = uri
        return uri
    }

    fun confirmarFotoCamara() {
        fotoUri = tempCameraUri
    }

    private fun guardarImagenEnInterno(context: Context, uri: Uri): String? {
        return try {
            val fileName = "animal_${idArete}.jpg"
            val directory = File(context.getExternalFilesDir(null), "FotoAnimales")
            if (!directory.exists()) directory.mkdirs()
            
            val destFile = File(directory, fileName)
            
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

    fun actualizarAnimal(context: Context) {
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
            
            // Si hay una nueva URI de foto, la guardamos físicamente
            fotoUri?.let { uri ->
                fotoPath = guardarImagenEnInterno(context, uri)
            }

            val animalActualizado = Animal(
                idArete = idArete,
                nombre = nombre,
                peso = pesoDouble,
                color = color,
                marcas = marcas,
                tipo = tipo,
                raza = raza,
                edad = edad,
                ubicacion = ubicacion,
                origen = origen,
                estado = estado,
                caracteristica = caracteristica,
                fotoPath = fotoPath,
                horaNacimiento = horaNacimiento
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
            val animal = repositorio.getAnimalById(idArete)
            if (animal != null) {
                val detalleBaja = when (motivoBaja) {
                    "Muerto" -> "Baja por muerte (${if (situacionMuerte == "Otro") otroMotivoMuerte else situacionMuerte})"
                    else -> "Baja por $motivoBaja"
                }
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
