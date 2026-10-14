package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CardCategoriaLujo(
    titulo: String,
    subtitulo: String,
    colorFondo: Color = Color.Unspecified,
    icono: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Pets,
    colorIcono: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val oscuro = colores.background.luminance() < 0.5f
    val acentoBase = if (colorIcono == Color.Unspecified) colores.primary else colorIcono
    val acento = if (oscuro && colorIcono != Color.Unspecified) lerp(acentoBase, Color.White, 0.3f) else acentoBase
    val fondo = when {
        colorFondo == Color.Unspecified -> colores.surfaceVariant
        oscuro -> lerp(colores.surface, acentoBase, 0.13f)
        else -> colorFondo
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = colores.onSurface),
        border = BorderStroke(1.dp, colores.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(acento.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = acento, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

