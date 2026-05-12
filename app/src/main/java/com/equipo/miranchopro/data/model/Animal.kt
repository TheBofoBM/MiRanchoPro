package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class Animal(
    @PrimaryKey
    val idArete: String,
    val nombre: String = "",
    val tipo: String = "Vaca",
    val raza: String = "Serrana",
    val edad: String = "Desconocida",
    val peso: Double,
    val caracteristica: String = "",
    val origen: String = "De parto", // "Comprada" o "De parto"
    val color: String = "No especificado",
    val marcas: String = "",
    val ubicacion: String = "Lote A",
    val estado: String = "Sano",
    val fechaRegistro: Long = System.currentTimeMillis() // Añadimos la fecha de registro
)
