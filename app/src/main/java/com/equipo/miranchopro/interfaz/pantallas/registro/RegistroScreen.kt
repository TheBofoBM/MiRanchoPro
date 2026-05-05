package com.equipo.miranchopro.interfaz.pantallas.registro

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.viewmodel.RegistroViewModel

// Colores consistentes con el Wireframe
private val ColorBackground  = Color(0xFFFFFFFF)
private val ColorText        = Color(0xFF2C3E50)
private val ColorPrimary     = Color(0xFF0E8A5A)
private val ColorFieldBorder = Color(0xFFE0E0E0)
private val ColorLabel       = Color(0xFF7F8C8D)
private val ColorSubtext     = Color(0xFF95A5A6)
private val ColorInputBg     = Color(0xFFF8F9FA)
private val ColorPlaceholder = Color(0xFFBDC3C7)

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var forzarErrorBD by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.registroExitoso) {
        if (viewModel.registroExitoso) {
            Toast.makeText(context, "Perfil de Administrador creado", Toast.LENGTH_SHORT).show()
            onRegistroExitoso()
        }
    }

    LaunchedEffect(viewModel.mensajeError) {
        viewModel.mensajeError?.let {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp, vertical = 48.dp)
        ) {
            RanchoLogo()

            Spacer(modifier = Modifier.height(32.dp))

            Text("Crea tu cuenta", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorText)
            Text("Únete para gestionar tu rancho", fontSize = 13.sp, color = ColorSubtext, modifier = Modifier.padding(top = 4.dp, bottom = 28.dp))

            RanchoInputField(
                label = "CORREO ELECTRÓNICO",
                value = viewModel.correo,
                onValueChange = { viewModel.correo = it },
                placeholder = "tu@ejemplo.com",
                keyboardType = KeyboardType.Email,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ColorLabel, modifier = Modifier.size(16.dp)) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            RanchoInputField(
                label = "CONTRASEÑA",
                value = viewModel.contrasena,
                onValueChange = { viewModel.contrasena = it },
                placeholder = "••••••••",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColorLabel, modifier = Modifier.size(16.dp)) },
                trailingIcon = { Text(if (passwordVisible) "Ocultar" else "Ver", color = ColorPrimary, fontSize = 12.sp, modifier = Modifier.clickable { passwordVisible = !passwordVisible }) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            RanchoInputField(
                label = "CONFIRMAR CONTRASEÑA",
                value = viewModel.confirmarContrasena,
                onValueChange = { viewModel.confirmarContrasena = it },
                placeholder = "••••••••",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColorLabel, modifier = Modifier.size(16.dp)) }
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Checkbox(
                    checked = forzarErrorBD,
                    onCheckedChange = { forzarErrorBD = it },
                    colors = CheckboxDefaults.colors(checkedColor = ColorPrimary, uncheckedColor = ColorSubtext),
                    modifier = Modifier.scale(0.8f)
                )
                Text("Simular Error de BD (CP-02.3)", fontSize = 11.sp, color = ColorSubtext)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.registrarUsuario(forzarErrorBD) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary, contentColor = Color.White)
            ) {
                Text("REGISTRARSE", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("¿Ya tienes cuenta? ", fontSize = 13.sp, color = ColorSubtext)
                Text("Inicia sesión", fontSize = 13.sp, color = ColorPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onIrALogin() })
            }
        }
    }
}

// ─── Componentes Auxiliares Reutilizados ───
@Composable
private fun RanchoLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).background(ColorPrimary, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
            Text("🌱", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(buildAnnotatedString { append("Rancho "); pushStyle(androidx.compose.ui.text.SpanStyle(color = ColorPrimary)); append("Ganado"); pop() }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorText)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RanchoInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text, visualTransformation: VisualTransformation = VisualTransformation.None, leadingIcon: @Composable (() -> Unit)? = null, trailingIcon: @Composable (() -> Unit)? = null) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(placeholder, color = ColorPlaceholder, fontSize = 14.sp) }, visualTransformation = visualTransformation, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), singleLine = true, leadingIcon = leadingIcon, trailingIcon = trailingIcon, shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = ColorInputBg, unfocusedContainerColor = ColorInputBg, focusedBorderColor = ColorPrimary, unfocusedBorderColor = ColorFieldBorder, focusedTextColor = ColorText, unfocusedTextColor = ColorText, cursorColor = ColorPrimary))
    }
}