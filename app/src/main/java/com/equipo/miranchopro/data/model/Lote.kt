package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotes")
data class Lote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-incremental
    val nombre: String,
    val capacidadMax: Int,
    var ocupacion: Int
)