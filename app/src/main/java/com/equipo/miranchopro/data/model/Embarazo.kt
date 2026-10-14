package com.equipo.miranchopro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "embarazos",
    indices = [Index(value = ["vacaId"])],
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["idArete"],
            childColumns = ["vacaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Embarazo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vacaId: String,
    val fechaInseminacion: Long,
    val tipo: String, // "Monta Natural" o "Inseminación"
    val fechaProbableParto: Long,
    val estado: String = "Activo", // "Activo", "Finalizado", "Interrumpido"
    val notas: String = ""
)
