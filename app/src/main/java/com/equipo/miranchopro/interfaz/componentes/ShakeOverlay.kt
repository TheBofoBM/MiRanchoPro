package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ShakeOverlay(
    onRegistrarNacimiento: () -> Unit,
    onRegistrarEnfermedad: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Parte superior: Registrar Nacimiento
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF4CAF50)) // Verde para nacimiento/vida
                .clickable { onRegistrarNacimiento() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "REGISTRAR NACIMIENTO",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Parte inferior: Registrar Enfermedad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE91E63)) // Rosa/Rojo para enfermedad/alerta
                .clickable { onRegistrarEnfermedad() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "REGISTRAR ENFERMEDAD",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    // Botón opcional para cerrar el overlay si se desea, 
    // pero el usuario pidió las dos opciones cubriendo la mitad.
    // Podríamos cerrar al hacer clic en cualquiera o tener un botón pequeño de "X".
}
