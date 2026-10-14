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
import com.equipo.miranchopro.utils.Validaciones
import com.equipo.miranchopro.data.local.AlmacenFotosAnimales
import kotlinx.coroutines.*

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

    var erroresCampos by mutableStateOf<Map<String, String>>(emptyMap())
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
                listaLotes = listOf("Sin Lote") + lotes.map { it.nombre }
            }
        }
    }

    fun cargarAnimal(id: String) {
        if (idArete == id) return
        viewModelScope.launch {
            estaCargando = true
            try {
                val a = repositorio.getAnimalById(id) ?: throw IllegalArgumentException("El animal ya no existe")
                idArete = a.idArete; nombre = a.nombre; peso = a.peso.toString(); color = a.color
                marcas = a.marcas; tipo = a.tipo; raza = a.raza; edad = Validaciones.edad(a.fechaNacimiento)
                ubicacion = a.ubicacion; origen = a.origen; estado = a.estado; caracteristica = a.caracteristica
                fotoPath = a.fotoPath; fechaRegistroOriginal = a.fechaRegistro
                fechaRegistroFormateada = Validaciones.fechaTexto(a.fechaRegistro)
                fechaNacimiento = Validaciones.fechaTexto(a.fechaNacimiento)
                horaNacimiento = a.horaNacimiento ?: ""
                cargarEmbarazoActivo(id)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mensajeError = Validaciones.mensaje(e, "No se pudo cargar el animal") }
            finally { estaCargando = false }
        }
    }

    private suspend fun cargarEmbarazoActivo(id: String) {
        embarazoActivo = repositorio.getEmbarazoActivo(id)
        fechaProbablePartoFormateada = embarazoActivo?.let { Validaciones.fechaTexto(it.fechaProbableParto) }
    }

    fun registrarGestacion() {
        if (estaCargando) return
        mensajeError = Validaciones.errorFecha(fechaInseminacionSeleccionada, "La fecha de monta")
        if (mensajeError != null) return
        ejecutar {
            repositorio.registrarEmbarazo(idArete, Validaciones.fecha(fechaInseminacionSeleccionada)!!, tipoMating, "Registrado manualmente").getOrThrow()
            cargarEmbarazoActivo(idArete)
            mostrarDialogoRegistroEmbarazo = false
            _eventoUI.emit(EventoUI.EmbarazoRegistrado)
        }
    }

    fun cerrarGestacion(estadoFinal: String) = ejecutar {
        repositorio.cerrarGestacion(idArete, estadoFinal)
        cargarEmbarazoActivo(idArete)
    }

    fun crearUriTemporalCamara(context: Context): Uri {
        val directory = File(context.getExternalFilesDir(null), "FotoAnimales").apply { mkdirs() }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File.createTempFile("IMG_EDIT_", ".jpg", directory))
    }
    fun confirmarFotoCamara(uri: Uri) { fotoUri = uri }

    fun actualizarAnimal(context: Context) {
        if (estaCargando) return
        erroresCampos = Validaciones.erroresAnimal(idArete, peso, fechaNacimiento)
        if (erroresCampos.isNotEmpty()) { mensajeError = "Revisa los campos señalados"; return }
        ejecutar {
            val anterior = repositorio.getAnimalById(idArete) ?: throw IllegalArgumentException("El animal ya no existe")
            var nuevaFoto: String? = null
            try {
                nuevaFoto = fotoUri?.let { AlmacenFotosAnimales.guardar(context, it) }
                val actualizado = anterior.copy(nombre = nombre.trim(), peso = Validaciones.decimal(peso)!!,
                    color = color.trim().ifBlank { "No especificado" }, marcas = marcas.trim(), tipo = tipo, raza = raza.trim(),
                    edad = Validaciones.edad(Validaciones.fecha(fechaNacimiento)), ubicacion = ubicacion, origen = origen,
                    caracteristica = caracteristica.trim(), fechaNacimiento = Validaciones.fecha(fechaNacimiento), fotoPath = nuevaFoto ?: fotoPath)
                repositorio.actualizarSeguro(actualizado)
                fotoPath = actualizado.fotoPath; fotoUri = null
                _eventoUI.emit(EventoUI.Exito)
            } finally {
                nuevaFoto?.let { archivo ->
                    withContext(NonCancellable + Dispatchers.IO) {
                        val registrado = runCatching { repositorio.getAnimalById(idArete)?.fotoPath == archivo }.getOrDefault(true)
                        if (!registrado) AlmacenFotosAnimales.eliminarArchivoNuevo(context, archivo)
                    }
                }
            }
        }
    }

    fun confirmarBaja() {
        if (motivoBaja !in listOf("Vendido", "Muerto", "Agregado accidentalmente")) { mensajeError = "Selecciona el motivo de baja"; return }
        ejecutar {
            val animal = repositorio.getAnimalById(idArete) ?: throw IllegalArgumentException("El animal ya no existe")
            repositorio.actualizarSeguro(animal.copy(estado = "Baja", marcas = "${animal.marcas} | Baja: $motivoBaja"))
            mostrarDialogoBaja = false
            _eventoUI.emit(EventoUI.BajaExitosa)
        }
    }

    private fun ejecutar(bloque: suspend () -> Unit) {
        if (estaCargando) return
        estaCargando = true; mensajeError = null
        viewModelScope.launch {
            try { bloque() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mensajeError = Validaciones.mensaje(e); _eventoUI.emit(EventoUI.Error(mensajeError!!)) }
            finally { estaCargando = false }
        }
    }
}
