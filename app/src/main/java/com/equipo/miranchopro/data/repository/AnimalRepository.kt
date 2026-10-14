package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.AnimalDao
import com.equipo.miranchopro.data.local.dao.EmbarazoDao
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.model.Embarazo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.*
import com.equipo.miranchopro.utils.Validaciones

class AnimalRepository(
    private val animalDao: AnimalDao,
    private val embarazoDao: EmbarazoDao
) {

    fun obtenerTodos(): Flow<List<Animal>> = animalDao.obtenerTodos()

    suspend fun getAnimalById(idArete: String): Animal? = animalDao.obtenerPorId(idArete)

    suspend fun registrarAnimal(animal: Animal): Result<Boolean> {
        return try {
            validarAnimal(animal)
            animalDao.insertarValidado(animal)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completarNacimientoPendiente(idTemporal: String, animal: Animal): Result<Boolean> {
        return try {
            validarAnimal(animal)
            animalDao.completarNacimientoPendiente(idTemporal, animal)
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarNacimientoRapido(): Result<String> {
        return try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val horaActual = sdf.format(Date())
            val idTemporal = "TEMP-${UUID.randomUUID()}"
            
            val animalPendiente = Animal(
                idArete = idTemporal,
                tipo = "Becerro",
                peso = 0.0,
                estado = "Pendiente",
                origen = "De parto",
                horaNacimiento = horaActual
            )
            
            animalDao.insertar(animalPendiente)
            Result.success(idTemporal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnimal(animal: Animal): Boolean {
        return try {
            validarAnimal(animal)
            animalDao.actualizarValidado(animal)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarAnimal(animal: Animal): Boolean {
        return try {
            animalDao.eliminar(animal)
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Funciones de Embarazo ---

    suspend fun registrarEmbarazo(vacaId: String, fechaInseminacion: Long, tipo: String, notas: String): Result<Boolean> {
        return try {
            // Calcular fecha probable de parto (283 días después)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = fechaInseminacion
                add(Calendar.DAY_OF_YEAR, 283)
            }
            
            val embarazo = Embarazo(
                vacaId = vacaId,
                fechaInseminacion = fechaInseminacion,
                tipo = tipo,
                fechaProbableParto = calendar.timeInMillis,
                notas = notas
            )
            embarazoDao.registrarValidado(embarazo)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmbarazoActivo(vacaId: String): Embarazo? = embarazoDao.getEmbarazoActivoPorVaca(vacaId)

    fun getHistorialEmbarazos(vacaId: String): Flow<List<Embarazo>> = embarazoDao.getHistorialEmbarazos(vacaId)

    suspend fun actualizarSeguro(animal: Animal) {
        validarAnimal(animal)
        animalDao.actualizarValidado(animal)
    }

    suspend fun cerrarGestacion(id: String, estado: String) {
        require(estado in listOf("Finalizado", "Interrumpido")) { "Estado de gestación inválido" }
        require(embarazoDao.cerrarActivos(id, estado) > 0) { "No hay una gestación activa" }
    }

    private fun validarAnimal(animal: Animal) {
        val errores = Validaciones.erroresAnimal(animal.idArete, animal.peso.toString(), Validaciones.fechaTexto(animal.fechaNacimiento))
        require(errores.isEmpty()) { errores.values.first() }
    }
}
