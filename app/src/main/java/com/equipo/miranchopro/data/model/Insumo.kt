package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "insumos")
data class Insumo(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val tipo: String, // e.g., "Alimento", "Forraje", "Minerales"
    val cantidad: Double,
    val unidadMedida: String, // e.g., "kg", "bultos", "ton"
    val stockMinimo: Double = 10.0
)
