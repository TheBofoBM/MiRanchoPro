package com.example.miranchopro.data.repository

import com.example.miranchopro.data.model.Animal
import kotlinx.coroutines.delay

class AnimalRepository {

    // Simulación de una base de datos en memoria
    private var mockAnimal = Animal("ART-123", 450.0, "Blanco con Negro", "Mancha en ojo derecho")

    suspend fun getAnimalById(idArete: String): Animal? {
        delay(500) // Simular latencia de red/DB
        return if (mockAnimal.idArete == idArete) mockAnimal else null
    }

    suspend fun updateAnimal(animal: Animal): Boolean {
        delay(500) // Simular latencia de red/DB
        mockAnimal = animal
        return true
    }
}
