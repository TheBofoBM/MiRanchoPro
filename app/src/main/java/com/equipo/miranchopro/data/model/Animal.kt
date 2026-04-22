package com.equipo.miranchopro.data.model

data class Animal(
    val idArete: String,
    val tipo: String = "Vaca",
    val raza: String = "Serrana",
    val edad: String = "Desconocida",
    val peso: Double,
    val color: String,
    val marcas: String,
    val ubicacion: String = "Lote A",
    val estado: String = "Sano"
)
