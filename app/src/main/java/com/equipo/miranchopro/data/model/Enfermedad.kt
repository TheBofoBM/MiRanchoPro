package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "enfermedades")
data class Enfermedad(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val sintomas: String,
    val tratamiento: String,
    val idAnimal: String, // Arete del animal
    val fechaDeteccion: String,
    val estado: String = "Activo", // e.g., "Activo", "Recuperado", "Fallecido"
    val notas: String = ""
)
