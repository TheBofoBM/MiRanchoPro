package com.equipo.miranchopro.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.AlmacenFotosAnimales
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.LoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegistrarAnimalViewModel(
    private val repositorio: AnimalRepository,
    private val loteRepositorio: LoteRepository
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
    var ubicacion by mutableStateOf("Sin Lote")
    
    // Para mantener la fecha de registro original en casos de "Parto detectado"
    private var fechaRegistroOriginal: Long? = null

    // Lista de lotes disponibles
    var listaLotes by mutableStateOf<List<String>>(listOf("Sin Lote"))
        private set

    // Estado para la foto
    var fotoUri by mutableStateOf<Uri?>(null)
    var fotoPath by mutableStateOf<String?>(null)
    
    // URI temporal para la cámara
    private var tempCameraUri: Uri? = null

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

    init {
        cargarLotes()
    }

    private fun cargarLotes() {
        viewModelScope.launch {
            loteRepositorio.obtenerTodosLosLotes().collect { lotes ->
                listaLotes = if (lotes.isEmpty()) listOf("Sin Lote") else lotes.map { it.nombre }
                if (ubicacion !in listaLotes) {
                    ubicacion = if (listaLotes.isNotEmpty()) listaLotes.first() else "Sin Lote"
                }
            }
        }
    }

    fun cargarParaCompletar(idTemp: String) {
        viewModelScope.launch {
            repositorio.getAnimalById(idTemp)?.let { animal ->
                esEdicionPendiente = true
                idTemporalOriginal = idTemp
                idArete = ""
                tipo = animal.tipo
                origen = animal.origen
                ubicacion = animal.ubicacion
                horaNacimientoRegistrada = animal.horaNacimiento ?: "00:00:00"
                fotoPath = animal.fotoPath
                fechaRegistroOriginal = animal.fechaRegistro // Guardamos el momento del shake

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaNacimiento = sdf.format(Date(animal.fechaRegistro))
            }
        }
    }

    fun crearUriTemporalCamara(context: Context): Uri {
        val directory = File(context.getExternalFilesDir(null), "FotoAnimales")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File.createTempFile("IMG_", ".jpg", directory)
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

    fun registrarAnimal(context: Context) {
        if (estaCargando) return
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
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaNacLong = try { sdf.parse(fechaNacimiento)?.time } catch (e: Exception) { null }

            var animalAGuardar = Animal(
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
                fechaRegistro = fechaRegistroOriginal ?: System.currentTimeMillis(), // Usar original si existe
                fechaNacimiento = fechaNacLong,
                fotoPath = fotoPath
            )

            var nuevaFotoPath: String? = null
            var guardadoConfirmado = false
            try {
                fotoUri?.let { uri ->
                    nuevaFotoPath = AlmacenFotosAnimales.guardar(context, uri)
                    animalAGuardar = animalAGuardar.copy(fotoPath = nuevaFotoPath)
                }
                val resultado = if (esEdicionPendiente && idTemporalOriginal != null) {
                    repositorio.completarNacimientoPendiente(idTemporalOriginal!!, animalAGuardar)
                } else {
                    repositorio.registrarAnimal(animalAGuardar)
                }
                resultado.getOrThrow()
                guardadoConfirmado = true
                limpiarCampos()
                _eventoUI.emit(EventoUI.Exito("Registro guardado con éxito"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventoUI.emit(EventoUI.Error("No se pudo guardar. Revisa el arete y la fotografía e inténtalo de nuevo."))
            } finally {
                try {
                    if (!guardadoConfirmado) {
                        nuevaFotoPath?.let { path ->
                            withContext(NonCancellable + Dispatchers.IO) {
                                // Una cancelación puede llegar después de confirmar el INSERT.
                                // Si no se puede comprobar la base, se conserva el archivo.
                                val fotoRegistrada = runCatching {
                                    repositorio.getAnimalById(animalAGuardar.idArete)?.fotoPath == path
                                }.getOrDefault(true)
                                if (!fotoRegistrada) AlmacenFotosAnimales.eliminarArchivoNuevo(context, path)
                            }
                        }
                    }
                } finally {
                    estaCargando = false
                }
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
        color = ""; marcas = ""; ubicacion = listaLotes.firstOrNull() ?: "Sin Lote"
        esEdicionPendiente = false; idTemporalOriginal = null
        horaNacimientoRegistrada = "00:00:00"
        fechaRegistroOriginal = null
        fotoUri = null; fotoPath = null
        tempCameraUri = null
    }
}
