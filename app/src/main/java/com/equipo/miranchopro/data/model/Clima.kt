package com.equipo.miranchopro.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ClimaDia(
    val dia: String,
    val temperatura: String,
    val condicion: String,
    val icono: ImageVector
)
