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
import com.equipo.miranchopro.data.model.Embarazo
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.LoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditarAnimalViewModel(
    private val repositorio: AnimalRepository,
    private val loteRepositorio: LoteRepository
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
    var fechaRegistroFormateada by mutableStateOf("")
    
    // Lista de lotes disponibles
    var listaLotes by mutableStateOf<List<String>>(listOf("Sin Lote"))
        private set

    // Estado para gestación
    var embarazoActivo by mutableStateOf<Embarazo?>(null)
    var fechaProbablePartoFormateada by mutableStateOf<String?>(null)
    var mostrarDialogoRegistroEmbarazo by mutableStateOf(false)
    var fechaInseminacionSeleccionada by mutableStateOf("")
    var tipoMating by mutableStateOf("Monta Natural")

    private var fechaRegistroOriginal: Long = System.currentTimeMillis()
    
    var fotoUri by mutableStateOf<Uri?>(null)
    var fotoPath by mutableStateOf<String?>(null)
    
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
        object EmbarazoRegistrado : EventoUI()
    }

    init {
        cargarLotes()
    }

    private fun cargarLotes() {
        viewModelScope.launch {
            loteRepositorio.obtenerTodosLosLotes().collect { lotes ->
                listaLotes = if (lotes.isEmpty()) listOf("Sin Lote") else lotes.map { it.nombre }
            }
        }
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
                fechaRegistroOriginal = animal.fechaRegistro
                
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaRegistroFormateada = sdf.format(Date(animal.fechaRegistro))
                
                val fechaAMostrar = animal.fechaNacimiento ?: animal.fechaRegistro
                fechaNacimiento = sdf.format(Date(fechaAMostrar))
                horaNacimiento = animal.horaNacimiento ?: "00:00:00"

                // Cargar embarazo si es hembra
                if (tipo in listOf("Vaca", "Vaquilla", "Becerra")) {
                    cargarEmbarazoActivo(id)
                }
            }
            estaCargando = false
        }
    }

    private fun cargarEmbarazoActivo(vacaId: String) {
        viewModelScope.launch {
            val embarazo = repositorio.getEmbarazoActivo(vacaId)
            embarazoActivo = embarazo
            if (embarazo != null) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaProbablePartoFormateada = sdf.format(Date(embarazo.fechaProbableParto))
            }
        }
    }

    fun registrarGestacion() {
        if (fechaInseminacionSeleccionada.isBlank()) {
            mensajeError = "La fecha es obligatoria"
            return
        }

        viewModelScope.launch {
            estaCargando = true
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaLong = sdf.parse(fechaInseminacionSeleccionada)?.time ?: System.currentTimeMillis()
                
                val resultado = repositorio.registrarEmbarazo(
                    vacaId = idArete,
                    fechaInseminacion = fechaLong,
                    tipo = tipoMating,
                    notas = "Registrado manualmente"
                )

                if (resultado.isSuccess) {
                    mostrarDialogoRegistroEmbarazo = false
                    cargarEmbarazoActivo(idArete)
                    _eventoUI.emit(EventoUI.EmbarazoRegistrado)
                } else {
                    _eventoUI.emit(EventoUI.Error("No se pudo registrar"))
                }
            } catch (e: Exception) {
                _eventoUI.emit(EventoUI.Error("Formato de fecha inválido"))
            } finally {
                estaCargando = false
            }
        }
    }

    fun crearUriTemporalCamara(context: Context): Uri {
        val directory = File(context.getExternalFilesDir(null), "FotoAnimales")
        if (!directory.exists()) directory.mkdirs()
        val file = File.createTempFile("IMG_EDIT_", ".jpg", directory)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun confirmarFotoCamara(uri: Uri) { fotoUri = uri }

    private fun guardarImagenEnInterno(context: Context, uri: Uri): String? {
        return try {
            val fileName = "animal_${idArete}.jpg"
            val directory = File(context.getExternalFilesDir(null), "FotoAnimales")
            if (!directory.exists()) directory.mkdirs()
            val destFile = File(directory, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) { null }
    }

    fun actualizarAnimal(context: Context) {
        if (peso.isBlank() || color.isBlank() || fechaNacimiento.isBlank()) {
            mensajeError = "Peso, color y fecha de nacimiento son obligatorios"
            return
        }
        val pesoDouble = peso.toDoubleOrNull() ?: 0.0
        estaCargando = true
        viewModelScope.launch {
            fotoUri?.let { uri -> fotoPath = guardarImagenEnInterno(context, uri) }
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaNacLong = try { sdf.parse(fechaNacimiento)?.time } catch (e: Exception) { null }
            val animalActualizado = Animal(
                idArete = idArete, nombre = nombre, peso = pesoDouble, color = color,
                marcas = marcas, tipo = tipo, raza = raza, edad = calcularEdad(fechaNacimiento),
                ubicacion = ubicacion, origen = origen, estado = estado, caracteristica = caracteristica,
                fotoPath = fotoPath, fechaRegistro = fechaRegistroOriginal, fechaNacimiento = fechaNacLong,
                horaNacimiento = horaNacimiento
            )
            val exito = repositorio.updateAnimal(animalActualizado)
            estaCargando = false
            if (exito) _eventoUI.emit(EventoUI.Exito) else _eventoUI.emit(EventoUI.Error("Error al actualizar"))
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
            if (anios > 0) "$anios años, $meses meses" else if (meses > 0) "$meses meses" else "Recién nacido"
        } catch (e: Exception) { "Desconocida" }
    }

    fun confirmarBaja() {
        if (motivoBaja.isBlank()) return
        viewModelScope.launch {
            estaCargando = true
            val animal = repositorio.getAnimalById(idArete)
            if (animal != null) {
                val detalleBaja = if (motivoBaja == "Muerto") "Baja por muerte ($situacionMuerte)" else "Baja por $motivoBaja"
                val animalDeBaja = animal.copy(estado = "Baja", marcas = "${animal.marcas} | Detalle: $detalleBaja")
                if (repositorio.updateAnimal(animalDeBaja)) {
                    mostrarDialogoBaja = false
                    _eventoUI.emit(EventoUI.BajaExitosa)
                } else _eventoUI.emit(EventoUI.Error("Error al procesar la baja"))
            }
            estaCargando = false
        }
    }
}
