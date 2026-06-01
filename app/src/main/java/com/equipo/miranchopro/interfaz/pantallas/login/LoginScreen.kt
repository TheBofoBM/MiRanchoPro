package com.equipo.miranchopro.interfaz.pantallas.login

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.viewmodel.LoginViewModel

private val ForestGreen = Color(0xFF004D40)
private val EmeraldPrimary = Color(0xFF00897B)
private val DarkSlate = Color(0xFF263238)
private val SoftGray = Color(0xFF78909C)
private val InputBackground = Color(0xFFF1F4F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onForgotPassword: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.loginExitoso) {
        if (viewModel.loginExitoso && viewModel.usuarioLogueado != null) {
            onLoginExitoso(viewModel.usuarioLogueado!!)
        }
    }

    LaunchedEffect(viewModel.mensajeError) {
        viewModel.mensajeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Círculo decorativo de fondo
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-150).dp, y = (-100).dp)
                .clip(CircleShape)
                .background(EmeraldPrimary.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER CURVO CON LOGO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(bottomStart = 80.dp, bottomEnd = 80.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(ForestGreen, EmeraldPrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(20.dp, RoundedCornerShape(30.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🌱", fontSize = 56.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Normal)) { append("Mi Rancho ") }
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) { append("PRO") }
                        },
                        fontSize = 36.sp
                    )
                    Text(
                        "GESTIÓN GANADERA INTELIGENTE",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            // --- FORMULARIO EN TARJETA ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = (-40).dp)
                    .fillMaxWidth()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(32.dp)) {
                        Text(
                            "Bienvenido",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkSlate
                        )
                        Text(
                            "Inicia sesión para continuar",
                            fontSize = 14.sp,
                            color = SoftGray,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // Input: Email
                        ModernLoginInput(
                            value = viewModel.correo,
                            onValueChange = { viewModel.correo = it },
                            label = "Correo Electrónico",
                            icon = Icons.Outlined.Email,
                            placeholder = "ejemplo@correo.com",
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Input: Contraseña
                        ModernLoginInput(
                            value = viewModel.contrasena,
                            onValueChange = { viewModel.contrasena = it },
                            label = "Contraseña",
                            icon = Icons.Outlined.Lock,
                            placeholder = "••••••••",
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible }
                        )

                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = EmeraldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                                .clickable { onForgotPassword() }
                        )

                        Button(
                            onClick = { viewModel.iniciarSesion(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .shadow(8.dp, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text(
                                "INICIAR SESIÓN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿Aún no tienes cuenta? ", color = SoftGray, fontSize = 14.sp)
                    Text(
                        "Regístrate",
                        color = EmeraldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.clickable { onRegisterClick() }
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernLoginInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    Column {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkSlate,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(icon, null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp)) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = DarkSlate,
                unfocusedTextColor = DarkSlate
            )
        )
    }
}
