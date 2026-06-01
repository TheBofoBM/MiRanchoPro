package com.equipo.miranchopro.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Prioridad(val fondo: Color, val texto: Color, val etiqueta: String) {
    ALTA(Color(0xFFFFEBEE), Color(0xFFC62828), "Alta"),
    MEDIA(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Media"),
    BAJA(Color(0xFFF1F8E9), Color(0xFF2E7D32), "Baja")
}

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val responsable: String,
    val prioridad: Prioridad,
    val estaHecha: Boolean = false,
    val fecha: String? = null
)
