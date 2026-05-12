package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.AnimalDao
import com.equipo.miranchopro.data.model.Animal
import kotlinx.coroutines.flow.Flow

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
