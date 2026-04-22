package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.model.Animal
import kotlinx.coroutines.delay

class AnimalRepository {
    private companion object {
        val animalesMock = mutableListOf(
            Animal("001", "Vaca", "Holstein", "3 años", 520.0, "Blanco y Negro", "Ninguna", "Lote A", "Sano"),
            Animal("002", "Toro", "Angus", "4 años", 780.0, "Negro", "Cicatriz en lomo", "Lote B", "Sano"),
            Animal("003", "Becerro", "Holstein", "6 meses", 180.0, "Blanco", "Mancha café", "Lote A", "Sano"),
            Animal("004", "Vaca", "Jersey", "5 años", 450.0, "Café claro", "Ninguna", "Lote C", "En tratamiento"),
            Animal("005", "Novillo", "Charolais", "2 años", 320.0, "Blanco", "Ninguna", "Lote B", "Sano")
        )
    }

    suspend fun obtenerTodos(): List<Animal> {
        delay(500)
        return animalesMock
    }

    suspend fun getAnimalById(idArete: String): Animal? {
        delay(300)
        return animalesMock.find { it.idArete == idArete }
    }

    suspend fun registrarAnimal(animal: Animal): Result<Boolean> {
        delay(500)
        if (animalesMock.any { it.idArete == animal.idArete }) {
            return Result.failure(Exception("El arete ya existe"))
        }
        animalesMock.add(animal)
        return Result.success(true)
    }

    suspend fun updateAnimal(animal: Animal): Boolean {
        delay(500)
        val index = animalesMock.indexOfFirst { it.idArete == animal.idArete }
        if (index != -1) {
            animalesMock[index] = animal
            return true
        }
        return false
    }
}
