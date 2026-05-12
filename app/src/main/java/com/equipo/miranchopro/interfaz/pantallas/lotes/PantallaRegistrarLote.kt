package com.equipo.miranchopro.interfaz.pantallas.lotes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.viewmodel.LotesViewModel

// ─── Colores consistentes con el resto de la app ─────────────────────────────

private val ColorBackground  = Color(0xFFFFFFFF)
private val ColorText        = Color(0xFF2C3E50)
private val ColorPrimary     = Color(0xFF0E8A5A)
private val ColorSubtext     = Color(0xFF95A5A6)
private val ColorLabel       = Color(0xFF7F8C8D)
private val ColorFieldBorder = Color(0xFFE0E0E0)
private val ColorInputBg     = Color(0xFFF8F9FA)
private val ColorPlaceholder = Color(0xFFBDC3C7)
private val ColorBadgeBg     = Color(0xFFE8F5E9)
private val ColorError       = Color(0xFFD32F2F)
private val ColorErrorBg     = Color(0xFFFDECEC)

// ─── Pantalla principal ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarLote(
    viewModel: LotesViewModel,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var capacidadStr by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "fade_in"
    )

    Scaffold(
        containerColor = ColorBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo Lote",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = ColorText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .alpha(alpha)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Encabezado descriptivo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorBadgeBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🐄", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Registrar potrero",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                        Text(
                            text = "Define el nombre y capacidad del nuevo lote",
                            fontSize = 11.sp,
                            color = ColorLabel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Campo: Nombre
            LoteInputField(
                label = "NOMBRE DEL POTRERO / LOTE",
                value = nombre,
                onValueChange = {
                    nombre = it
                    mensajeError = ""
                },
                placeholder = "Ej. Potrero Norte"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Campo: Capacidad
            LoteInputField(
                label = "CAPACIDAD MÁXIMA (CABEZAS)",
                value = capacidadStr,
                onValueChange = {
                    capacidadStr = it
                    mensajeError = ""
                },
                placeholder = "Ej. 50",
                keyboardType = KeyboardType.Number
            )

            // Mensaje de error
            if (mensajeError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorErrorBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = mensajeError,
                        color = ColorError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Botón guardar
            Button(
                onClick = {
                    val capacidad = capacidadStr.toIntOrNull()
                    if (nombre.isBlank() || capacidad == null || capacidad <= 0) {
                        mensajeError = "Por favor ingresa un nombre y una capacidad válida mayor a 0."
                    } else {
                        val nuevoLote = Lote(
                            nombre = nombre,
                            capacidadMaxima = capacidad,
                            ocupacionActual = 0
                        )
                        viewModel.guardarNuevoLote(nuevoLote)
                        onVolver()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "GUARDAR LOTE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón cancelar secundario
            TextButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 13.sp,
                    color = ColorSubtext
                )
            }
        }
    }
}

// ─── Componente de campo reutilizable ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoteInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
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