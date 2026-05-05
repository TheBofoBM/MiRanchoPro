package com.equipo.miranchopro.interfaz.pantallas.login

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.viewmodel.LoginViewModel

// ─── Colores adaptados al Wireframe (Rancho Ganado) ──────────────────────────

private val ColorBackground  = Color(0xFFFFFFFF) // Fondo blanco limpio
private val ColorText        = Color(0xFF2C3E50) // Texto oscuro principal
private val ColorPrimary     = Color(0xFF0E8A5A) // Verde característico
private val ColorFieldBorder = Color(0xFFE0E0E0) // Bordes sutiles
private val ColorLabel       = Color(0xFF7F8C8D) // Gris para etiquetas
private val ColorSubtext     = Color(0xFF95A5A6) // Gris más claro para subtextos
private val ColorBadgeBg     = Color(0xFFE8F5E9) // Fondo verde muy tenue para badges
private val ColorInputBg     = Color(0xFFF8F9FA) // Fondo casi blanco para campos
private val ColorPlaceholder = Color(0xFFBDC3C7) // Color de los placeholders

// ─── Pantalla principal ──────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
    onGoogleClick: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var forzarErrorConexion by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.loginExitoso) {
        if (viewModel.loginExitoso) onLoginExitoso()
    }

    LaunchedEffect(viewModel.mensajeError) {
        viewModel.mensajeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fade_in"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .alpha(alpha),
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

            Text(
                text = "Bienvenido",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ColorText
            )
            Text(
                text = "Inicia sesión en tu cuenta",
                fontSize = 13.sp,
                color = ColorSubtext,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            RanchoInputField(
                label = "CORREO ELECTRÓNICO",
                value = viewModel.correo,
                onValueChange = { viewModel.correo = it },
                placeholder = "tu@ejemplo.com",
                keyboardType = KeyboardType.Email,
                leadingIcon = {
                    Icon(
                        Icons.Default.Email, contentDescription = null,
                        tint = ColorLabel, modifier = Modifier.size(16.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            RanchoInputField(
                label = "CONTRASEÑA",
                value = viewModel.contrasena,
                onValueChange = { viewModel.contrasena = it },
                placeholder = "••••••••",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock, contentDescription = null,
                        tint = ColorLabel, modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    Text(
                        text = if (passwordVisible) "Ocultar" else "Ver",
                        color = ColorPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = forzarErrorConexion,
                    onCheckedChange = { forzarErrorConexion = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ColorPrimary,
                        uncheckedColor = ColorSubtext
                    ),
                    modifier = Modifier.scale(0.8f)
                )
                Text("Simular Error de Conexión", fontSize = 11.sp, color = ColorSubtext)
            }

            Text(
                text = "¿Olvidaste tu contraseña?",
                color = ColorPrimary,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 22.dp)
                    .clickable { onForgotPassword() }
            )

            Button(
                onClick = { viewModel.iniciarSesion(forzarErrorConexion) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "INICIAR SESIÓN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "¿No tienes cuenta? ", fontSize = 13.sp, color = ColorSubtext)
                Text(
                    text = "Regístrate gratis",
                    fontSize = 13.sp,
                    color = ColorPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Componentes auxiliares ──────────────────────────────────────────────────

@Composable
private fun RanchoLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(ColorPrimary, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Cambié la casa en ruinas por una hojita más ad hoc al verde
            Text(text = "🌱", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            buildAnnotatedString {
                append("Rancho ")
                pushStyle(androidx.compose.ui.text.SpanStyle(color = ColorPrimary))
                append("Ganado")
                pop()
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RanchoInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ColorLabel,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = ColorPlaceholder, fontSize = 14.sp) },
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
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

@Composable
private fun RanchoBadge(text: String) {
    Row(
        modifier = Modifier
            .background(ColorBadgeBg, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(ColorPrimary, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = text, fontSize = 11.sp, color = ColorPrimary) // Texto en verde para contrastar con el fondo tenue
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 400, heightDp = 750)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginExitoso = {})
}