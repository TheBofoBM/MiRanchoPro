package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.AnimalDao
import com.equipo.miranchopro.data.model.Animal
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class AnimalRepository(private val animalDao: AnimalDao) {

    fun obtenerTodos(): Flow<List<Animal>> = animalDao.obtenerTodos()

    suspend fun getAnimalById(idArete: String): Animal? = animalDao.obtenerPorId(idArete)

    suspend fun registrarAnimal(animal: Animal): Result<Boolean> {
        return try {
            animalDao.insertar(animal)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarNacimientoRapido(): Result<String> {
        return try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val horaActual = sdf.format(Date())
            val idTemporal = "TEMP-${System.currentTimeMillis() % 10000}"
            
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
            animalDao.actualizar(animal)
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
}
