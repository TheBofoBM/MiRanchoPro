package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.model.Animal
import kotlinx.coroutines.delay

class AnimalRepository {
    private var mockAnimal = Animal("ART-123", 450.0, "Blanco con Negro", "Mancha en ojo derecho")

    suspend fun getAnimalById(idArete: String): Animal? {
        delay(500)
        return if (mockAnimal.idArete == idArete) mockAnimal else null
    }

    suspend fun updateAnimal(animal: Animal): Boolean {
        delay(500)
        mockAnimal = animal
        return true
    }
}
