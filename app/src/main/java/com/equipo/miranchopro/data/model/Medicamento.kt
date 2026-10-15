package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val dosis: String,
    val stock: Int,
    val unidadMedida: String, // e.g., ml, mg, unidades
    val descripcion: String = "",
    @androidx.room.ColumnInfo(defaultValue = "5") val stockMinimo: Int = 5,
    val fechaCaducidad: Long? = null
)
