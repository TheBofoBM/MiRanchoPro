package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vacunaciones")
data class Vacunacion(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombreVacuna: String,
    val dosis: String,
    val fechaAplicacion: String,
    val idAnimal: String, // Arete del animal
    val proximaDosis: String? = null,
    val notas: String = ""
)
