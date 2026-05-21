package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registros_salud")
data class RegistroSalud(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idArete: String,
    val fecha: Long,            // Fecha de aplicación
    val proximaFecha: Long? = null, // Próxima fecha (opcional)
    val tipo: String,
    val medicamento: String,    // Tratamiento o Vacuna
    val veterinario: String,
    val notas: String
)