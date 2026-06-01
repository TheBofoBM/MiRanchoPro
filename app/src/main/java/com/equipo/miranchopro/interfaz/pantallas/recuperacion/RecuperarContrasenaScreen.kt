package com.equipo.miranchopro.interfaz.pantallas.recuperacion

import android.widget.Toast
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.viewmodel.PasoRecuperacion
import com.equipo.miranchopro.viewmodel.RecuperarContrasenaViewModel

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
            // Botón de regreso (solo si no es éxito)
            if (viewModel.pasoActual != PasoRecuperacion.EXITO) {
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
            }

            Crossfade(targetState = viewModel.pasoActual, label = "pasos") { paso ->
                when (paso) {
                    PasoRecuperacion.INGRESAR_CORREO -> {
                        SeccionIngresarCorreo(viewModel, forzarErrorServidor) { forzarErrorServidor = it }
                    }
                    PasoRecuperacion.VERIFICAR_CODIGO -> {
                        SeccionVerificarCodigo(viewModel)
                    }
                    PasoRecuperacion.NUEVA_CONTRASENA -> {
                        SeccionNuevaContrasena(viewModel)
                    }
                    PasoRecuperacion.EXITO -> {
                        SeccionExitoRecuperacion(onVolverClick)
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionIngresarCorreo(
    viewModel: RecuperarContrasenaViewModel,
    forzarError: Boolean,
    onForzarErrorChange: (Boolean) -> Unit
) {
    Column {
        Text("Recuperar contraseña", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorText)
        Text(
            "Ingresa tu correo registrado y te enviaremos un código para restablecer tu contraseña.",
            fontSize = 13.sp, color = ColorSubtext, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        RanchoInput(
            label = "CORREO ELECTRÓNICO",
            value = viewModel.correo,
            onValueChange = { viewModel.correo = it },
            placeholder = "tu@ejemplo.com",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
            Checkbox(
                checked = forzarError,
                onCheckedChange = onForzarErrorChange,
                colors = CheckboxDefaults.colors(checkedColor = ColorPrimary)
            )
            Text("Simular Fallo de Servidor (Ex-01)", fontSize = 11.sp, color = ColorSubtext)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.enviarCodigo(forzarError) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
        ) {
            Text("ENVIAR CÓDIGO", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeccionVerificarCodigo(viewModel: RecuperarContrasenaViewModel) {
    Column {
        Text("Verificar Código", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorText)
        Text(
            "Ingresa el código de 6 dígitos que enviamos a ${viewModel.correo}",
            fontSize = 13.sp, color = ColorSubtext, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        RanchoInput(
            label = "CÓDIGO DE VERIFICACIÓN",
            value = viewModel.codigoIngresado,
            onValueChange = { if(it.length <= 6) viewModel.codigoIngresado = it },
            placeholder = "123456",
            icon = Icons.Default.Pin,
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.verificarCodigo() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
        ) {
            Text("VERIFICAR", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        
        TextButton(
            onClick = { viewModel.pasoActual = PasoRecuperacion.INGRESAR_CORREO },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Text("Reenviar código", color = ColorPrimary)
        }
    }
}

@Composable
fun SeccionNuevaContrasena(viewModel: RecuperarContrasenaViewModel) {
    Column {
        Text("Nueva Contraseña", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorText)
        Text(
            "Establece tu nueva contraseña de acceso.",
            fontSize = 13.sp, color = ColorSubtext, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        RanchoInput(
            label = "NUEVA CONTRASEÑA",
            value = viewModel.nuevaContrasena,
            onValueChange = { viewModel.nuevaContrasena = it },
            placeholder = "••••••••",
            icon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        RanchoInput(
            label = "CONFIRMAR CONTRASEÑA",
            value = viewModel.confirmarContrasena,
            onValueChange = { viewModel.confirmarContrasena = it },
            placeholder = "••••••••",
            icon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.restablecerContrasena() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
        ) {
            Text("RESTABLECER", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeccionExitoRecuperacion(onVolver: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎉", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("¡Todo listo!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorText)
        Text(
            text = "Tu contraseña ha sido actualizada correctamente. Ya puedes iniciar sesión.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 13.sp, color = ColorSubtext, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
        ) {
            Text("IR AL LOGIN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RanchoInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = ColorPlaceholder, fontSize = 14.sp) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            leadingIcon = { Icon(icon, contentDescription = null, tint = ColorLabel, modifier = Modifier.size(16.dp)) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorInputBg,
                unfocusedContainerColor = ColorInputBg,
                focusedBorderColor = ColorPrimary,
                unfocusedBorderColor = ColorFieldBorder,
                focusedTextColor = ColorText,
                unfocusedTextColor = ColorText,
                cursorColor = ColorPrimary
            )
        )
    }
}
