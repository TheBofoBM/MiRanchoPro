package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class Animal(
    @PrimaryKey
    val idArete: String,
    val peso: Double,
    val color: String,
    val marcas: String,
    val tipo: String = "Vaca",
    val raza: String = "Serrana",
    val edad: String = "Desconocida",
    val ubicacion: String = "Lote A",
    val estado: String = "Sano"
)