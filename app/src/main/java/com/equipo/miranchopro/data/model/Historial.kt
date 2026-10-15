package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pesajes", indices = [Index("animalId")])
data class Pesaje(@PrimaryKey val id: String = UUID.randomUUID().toString(), val animalId: String,
    val fecha: Long, val peso: Double, val notas: String = "", val responsable: String = "",
    val registradoEn: Long = System.currentTimeMillis())

@Entity(tableName = "eventos_animales", indices = [Index("animalId")])
data class EventoAnimal(@PrimaryKey val id: String = UUID.randomUUID().toString(), val animalId: String,
    val fecha: Long = System.currentTimeMillis(), val tipo: String, val detalle: String, val responsable: String = "")

@Entity(tableName = "partos", indices = [Index(value = ["gestacionId"], unique = true), Index("madreId")])
data class Parto(@PrimaryKey val id: String = UUID.randomUUID().toString(), val madreId: String,
    val gestacionId: Int?, val fecha: Long, val resultado: String, val notas: String = "", val responsable: String = "")

@Entity(tableName = "movimientos_existencias", indices = [Index("productoId")])
data class MovimientoExistencia(@PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productoId: String, val clase: String, val nombre: String, val fecha: Long = System.currentTimeMillis(),
    val tipo: String, val cantidad: Double, val saldo: Double, val unidad: String,
    val motivo: String, val responsable: String = "")
