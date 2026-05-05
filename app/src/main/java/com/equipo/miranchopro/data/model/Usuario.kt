package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    val correo: String, // Usaremos el correo como llave primaria porque no se puede repetir
    val contrasena: String,
    val rol: String
)