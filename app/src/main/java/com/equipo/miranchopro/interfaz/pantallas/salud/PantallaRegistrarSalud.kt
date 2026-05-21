package com.equipo.miranchopro.interfaz.pantallas.salud

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
import androidx.compose.material.icons.filled.DateRange
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
import com.equipo.miranchopro.modelovista.RegistrarSaludViewModel

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
private val ColorSectionBg   = Color(0xFFF0F4F8)

// ─── Pantalla principal ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarSalud(
    viewModel: RegistrarSaludViewModel,
    onVolver: () -> Unit
) {
    var expandedArete by remember { mutableStateOf(false) }
    var expandedTipo  by remember { mutableStateOf(false) }
    var mensajeError  by remember { mutableStateOf("") }

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
                        text = "Registrar Tratamiento",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = ColorText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .alpha(alpha)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Banner informativo ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorBadgeBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🩺", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nuevo registro de salud",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                        Text(
                            text = "Completa los datos del tratamiento o vacuna aplicada",
                            fontSize = 11.sp,
                            color = ColorLabel
                        )
                    }
                }
            }

            // ── Sección 1: Animal y tipo ──────────────────────────────────
            SeccionFormulario(
                icono = "🐄",
                titulo = "Identificación",
                descripcion = "¿A qué animal se le aplica?"
            ) {
                // Selector de Animal
                SaludDropdownField(
                    label = "ANIMAL (TAG / ARETE)",
                    valor = viewModel.idArete,
                    placeholder = "Seleccionar animal...",
                    expandido = expandedArete,
                    onExpandChange = { expandedArete = !expandedArete },
                    onDismiss = { expandedArete = false }
                ) {
                    viewModel.animalesDisponibles.forEach { arete ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🐄", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(arete, fontSize = 13.sp, color = ColorText)
                                }
                            },
                            onClick = { viewModel.idArete = arete; expandedArete = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Tipo
                SaludDropdownField(
                    label = "TIPO DE TRATAMIENTO",
                    valor = viewModel.tipo,
                    placeholder = "Seleccionar tipo...",
                    expandido = expandedTipo,
                    onExpandChange = { expandedTipo = !expandedTipo },
                    onDismiss = { expandedTipo = false }
                ) {
                    viewModel.tiposDisponibles.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo, fontSize = 13.sp, color = ColorText) },
                            onClick = { viewModel.tipo = tipo; expandedTipo = false }
                        )
                    }
                }
            }

            // ── Sección 2: Detalle del tratamiento ────────────────────────
            SeccionFormulario(
                icono = "💊",
                titulo = "Detalle del tratamiento",
                descripcion = "Medicamento o vacuna aplicada"
            ) {
                SaludInputField(
                    label = "TRATAMIENTO / VACUNA",
                    value = viewModel.medicamento,
                    onValueChange = { viewModel.medicamento = it },
                    placeholder = "Ej: Vacuna triple bovina"
                )

                Spacer(modifier = Modifier.height(14.dp))

                SaludInputField(
                    label = "VETERINARIO RESPONSABLE",
                    value = viewModel.veterinario,
                    onValueChange = { viewModel.veterinario = it },
                    placeholder = "Ej: Dr. García"
                )
            }

            // ── Sección 3: Fechas ─────────────────────────────────────────
            SeccionFormulario(
                icono = "📅",
                titulo = "Fechas",
                descripcion = "Aplicación y seguimiento"
            ) {
                SaludInputField(
                    label = "FECHA DE APLICACIÓN",
                    value = viewModel.fechaAplicacion,
                    onValueChange = { viewModel.fechaAplicacion = it },
                    placeholder = "dd/mm/aaaa",
                    keyboardType = KeyboardType.Text,
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                SaludInputField(
                    label = "PRÓXIMA FECHA — OPCIONAL",
                    value = viewModel.proximaFecha,
                    onValueChange = { viewModel.proximaFecha = it },
                    placeholder = "dd/mm/aaaa",
                    keyboardType = KeyboardType.Text,
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = ColorSubtext,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // ── Sección 4: Notas ──────────────────────────────────────────
            SeccionFormulario(
                icono = "📝",
                titulo = "Observaciones",
                descripcion = "Información adicional del registro"
            ) {
                Column {
                    Text(
                        text = "NOTAS ADICIONALES — OPCIONAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorLabel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.notas,
                        onValueChange = { viewModel.notas = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Sin complicaciones, animal estable...", color = ColorPlaceholder, fontSize = 13.sp) },
                        minLines = 3,
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

            // ── Mensaje de error ──────────────────────────────────────────
            if (mensajeError.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorErrorBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚠", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mensajeError,
                            color = ColorError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Botón guardar ─────────────────────────────────────────────
            Button(
                onClick = {
                    if (viewModel.idArete.isBlank() || viewModel.medicamento.isBlank() ||
                        viewModel.fechaAplicacion.isBlank() || viewModel.veterinario.isBlank()
                    ) {
                        mensajeError = "Por favor, completa todos los campos obligatorios."
                    } else {
                        mensajeError = ""
                        viewModel.guardarRegistro(
                            alTerminar = { onVolver() },
                            alFallar   = { error -> mensajeError = error }
                        )
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
                    text = "GUARDAR TRATAMIENTO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            TextButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cancelar", fontSize = 13.sp, color = ColorSubtext)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─── Sección con encabezado visual ──────────────────────────────────────────

@Composable
private fun SeccionFormulario(
    icono: String,
    titulo: String,
    descripcion: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ColorSectionBg)
            .padding(16.dp)
    ) {
        // Encabezado de sección
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(ColorBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icono, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = titulo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorText
                )
                Text(
                    text = descripcion,
                    fontSize = 11.sp,
                    color = ColorLabel
                )
            }
        }

        HorizontalDivider(
            color = ColorFieldBorder,
            thickness = 1.dp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        contenido()
    }
}

// ─── Campo de texto reutilizable ─────────────────────────────────────────────

@Composable
private fun SaludInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
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
            placeholder = { Text(placeholder, color = ColorPlaceholder, fontSize = 13.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
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

// ─── Dropdown reutilizable ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaludDropdownField(
    label: String,
    valor: String,
    placeholder: String,
    expandido: Boolean,
    onExpandChange: () -> Unit,
    onDismiss: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
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
        ExposedDropdownMenuBox(
            expanded = expandido,
            onExpandedChange = { onExpandChange() }
        ) {
            OutlinedTextField(
                value = valor,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(placeholder, color = ColorPlaceholder, fontSize = 13.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorInputBg,
                    unfocusedContainerColor = ColorInputBg,
                    focusedBorderColor = ColorPrimary,
                    unfocusedBorderColor = ColorFieldBorder,
                    focusedTextColor = ColorText,
                    unfocusedTextColor = if (valor.isEmpty()) ColorPlaceholder else ColorText
                )
            )
            ExposedDropdownMenu(
                expanded = expandido,
                onDismissRequest = onDismiss,
                content = contenido
            )
        }
    }
}