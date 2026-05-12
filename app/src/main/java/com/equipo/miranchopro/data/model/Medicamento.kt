package com.equipo.miranchopro.data.model

import java.util.UUID

data class Medicamento(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val dosis: String,
    val stock: Int,
    val unidadMedida: String, // e.g., ml, mg, unidades
    val descripcion: String = ""
)
