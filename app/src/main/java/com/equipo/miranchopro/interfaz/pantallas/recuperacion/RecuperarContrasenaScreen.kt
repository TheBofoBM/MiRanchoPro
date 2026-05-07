package com.equipo.miranchopro.interfaz.pantallas.recuperacion

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.viewmodel.RecuperarContrasenaViewModel

// Colores del Wireframe
private val ColorBackground  = Color(0xFFFFFFFF)
private val ColorText        = Color(0xFF2C3E50)
private val ColorPrimary     = Color(0xFF0E8A5A)
private val ColorFieldBorder = Color(0xFFE0E0E0)
private val ColorLabel       = Color(0xFF7F8C8D)
private val ColorSubtext     = Color(0xFF95A5A6)
private val ColorInputBg     = Color(0xFFF8F9FA)
private val ColorPlaceholder = Color(0xFFBDC3C7)

@Composable
fun RecuperarContrasenaScreen(
    onVolverClick: () -> Unit,
    viewModel: RecuperarContrasenaViewModel
) {
    val context = LocalContext.current
    var forzarErrorServidor by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.mensajeUI) {
        viewModel.mensajeUI?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensaje()
        }
    }

    LaunchedEffect(viewModel.correoEnviado) {
        if (viewModel.correoEnviado) {
            // Si tiene éxito, lo regresamos al login después de un momento
            onVolverClick()
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(600), label = "")

    Box(
        modifier = Modifier.fillMaxSize().background(ColorBackground).alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxHeight()
                .padding(horizontal = 36.dp, vertical = 48.dp)
        ) {
            // Botón de regreso
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .clickable { onVolverClick() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = ColorPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver al Login", color = ColorPrimary, fontWeight = FontWeight.Bold)
            }

            Text("Recuperar contraseña", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorText)
            Text(
                "Ingresa tu correo registrado y te enviaremos un enlace para restablecer tu contraseña.",
                fontSize = 13.sp, color = ColorSubtext, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Input Correo
            Column {
                Text("CORREO ELECTRÓNICO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = viewModel.correo,
                    onValueChange = { viewModel.correo = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("tu@ejemplo.com", color = ColorPlaceholder, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ColorLabel, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = ColorInputBg, unfocusedContainerColor = ColorInputBg, focusedBorderColor = ColorPrimary, unfocusedBorderColor = ColorFieldBorder, focusedTextColor = ColorText, unfocusedTextColor = ColorText, cursorColor = ColorPrimary)
                )
            }

            // Checkbox para CP-03.3
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                Checkbox(
                    checked = forzarErrorServidor,
                    onCheckedChange = { forzarErrorServidor = it },
                    colors = CheckboxDefaults.colors(checkedColor = ColorPrimary, uncheckedColor = ColorSubtext),
                    modifier = Modifier.scale(0.8f)
                )
                Text("Simular Error de Servidor (CP-03.3)", fontSize = 11.sp, color = ColorSubtext)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Enviar
            Button(
                onClick = { viewModel.enviarEnlaceRecuperacion(forzarErrorServidor) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = Color.White)
            ) {
                Text("ENVIAR ENLACE", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}