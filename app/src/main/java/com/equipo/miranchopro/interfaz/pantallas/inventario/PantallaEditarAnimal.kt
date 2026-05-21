package com.equipo.miranchopro.interfaz.pantallas.inventario

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.modelovista.EditarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

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
private val ColorSectionBg   = Color(0xFFF0F4F8)
private val ColorError       = Color(0xFFD32F2F)
private val ColorErrorBg     = Color(0xFFFDECEC)

// ─── Pantalla principal ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarAnimal(
    idArete: String,
    viewModel: EditarAnimalViewModel = viewModel(),
    alVolver: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var expandedLotes by remember { mutableStateOf(false) }

    LaunchedEffect(idArete) { viewModel.cargarAnimal(idArete) }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is EditarAnimalViewModel.EventoUI.Exito ->
                    snackbarHostState.showSnackbar("Animal actualizado correctamente")
                is EditarAnimalViewModel.EventoUI.BajaExitosa -> {
                    snackbarHostState.showSnackbar("Animal dado de baja")
                    alVolver()
                }
                is EditarAnimalViewModel.EventoUI.Error ->
                    snackbarHostState.showSnackbar(evento.mensaje)
            }
        }
    }

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
                        text = "Editar Animal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = ColorText)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.mostrarDialogoBaja = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Dar de baja", tint = ColorError)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->

        if (viewModel.estaCargando && viewModel.idArete.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize()
                    .alpha(alpha)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── Banner de identificación ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorBadgeBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ColorPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🐄", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ID Arete",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorLabel,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = viewModel.idArete.ifBlank { idArete },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorPrimary
                            )
                        }
                    }
                }

                // ── Sección 1: Datos físicos ──────────────────────────────
                SeccionEditar(icono = "📏", titulo = "Datos físicos", descripcion = "Medidas y características del animal") {

                    AnimalInputField(
                        label = "PESO (KG)",
                        value = viewModel.peso,
                        onValueChange = { viewModel.peso = it },
                        placeholder = "Ej: 420.5",
                        keyboardType = KeyboardType.Decimal
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimalInputField(
                        label = "EDAD",
                        value = viewModel.edad,
                        onValueChange = { viewModel.edad = it },
                        placeholder = "Ej: 2 años, 6 meses"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimalInputField(
                        label = "COLOR",
                        value = viewModel.color,
                        onValueChange = { viewModel.color = it },
                        placeholder = "Ej: Negro con blanco"
                    )
                }

                // ── Sección 2: Ubicación ──────────────────────────────────
                SeccionEditar(icono = "📍", titulo = "Ubicación", descripcion = "Lote o potrero asignado") {

                    Text(
                        text = "ASIGNAR A LOTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorLabel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedLotes,
                        onExpandedChange = { expandedLotes = !expandedLotes },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.ubicacion,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Seleccionar lote...", color = ColorPlaceholder, fontSize = 13.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLotes) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ColorInputBg,
                                unfocusedContainerColor = ColorInputBg,
                                focusedBorderColor = ColorPrimary,
                                unfocusedBorderColor = ColorFieldBorder,
                                focusedTextColor = ColorText,
                                unfocusedTextColor = if (viewModel.ubicacion.isEmpty()) ColorPlaceholder else ColorText
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLotes,
                            onDismissRequest = { expandedLotes = false }
                        ) {
                            viewModel.lotesDisponibles.forEach { nombreLote ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "🌿", fontSize = 13.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(nombreLote, fontSize = 13.sp, color = ColorText)
                                        }
                                    },
                                    onClick = {
                                        viewModel.ubicacion = nombreLote
                                        expandedLotes = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Sección 3: Observaciones ──────────────────────────────
                SeccionEditar(icono = "📝", titulo = "Observaciones", descripcion = "Marcas o señas particulares") {

                    Text(
                        text = "MARCAS Y SEÑAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorLabel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.marcas,
                        onValueChange = { viewModel.marcas = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Cicatriz en cuarto trasero izquierdo...", color = ColorPlaceholder, fontSize = 13.sp) },
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

                // ── Mensaje de error ──────────────────────────────────────
                if (viewModel.mensajeError != null) {
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
                                text = viewModel.mensajeError!!,
                                color = ColorError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ── Botón actualizar ──────────────────────────────────────
                Button(
                    onClick = { viewModel.actualizarAnimal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !viewModel.estaCargando,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = ColorFieldBorder
                    )
                ) {
                    if (viewModel.estaCargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "ACTUALIZAR DATOS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // ── Botón dar de baja ─────────────────────────────────────
                OutlinedButton(
                    onClick = { viewModel.mostrarDialogoBaja = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorError),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorError)
                ) {
                    Text(
                        text = "🗑  Dar de Baja",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // ── Diálogo: Dar de baja ──────────────────────────────────────────────────
    if (viewModel.mostrarDialogoBaja) {
        DialogoDarDeBaja(viewModel = viewModel)
    }
}

// ─── Sección con encabezado visual ──────────────────────────────────────────

@Composable
private fun SeccionEditar(
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
                Text(text = titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorText)
                Text(text = descripcion, fontSize = 11.sp, color = ColorLabel)
            }
        }
        HorizontalDivider(color = ColorFieldBorder, thickness = 1.dp, modifier = Modifier.padding(bottom = 14.dp))
        contenido()
    }
}

// ─── Campo de texto reutilizable ─────────────────────────────────────────────

@Composable
private fun AnimalInputField(
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
            placeholder = { Text(placeholder, color = ColorPlaceholder, fontSize = 13.sp) },
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

// ─── Diálogo: Dar de baja ────────────────────────────────────────────────────

@Composable
private fun DialogoDarDeBaja(viewModel: EditarAnimalViewModel) {
    val motivos    = listOf("Vendido", "Muerto", "Agregado accidentalmente")
    val situaciones = listOf("Médica", "Accidente", "Otro")

    AlertDialog(
        onDismissRequest = { viewModel.mostrarDialogoBaja = false },
        containerColor = ColorBackground,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(ColorErrorBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🗑", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Dar de baja",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorText
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "MOTIVO DE LA BAJA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorLabel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                motivos.forEach { motivo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (viewModel.motivoBaja == motivo) ColorBadgeBg else Color.Transparent
                            )
                            .clickable { viewModel.motivoBaja = motivo }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = viewModel.motivoBaja == motivo,
                            onClick = { viewModel.motivoBaja = motivo },
                            colors = RadioButtonDefaults.colors(selectedColor = ColorPrimary)
                        )
                        Text(
                            text = motivo,
                            fontSize = 13.sp,
                            color = if (viewModel.motivoBaja == motivo) ColorPrimary else ColorText,
                            fontWeight = if (viewModel.motivoBaja == motivo) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }

                if (viewModel.motivoBaja == "Muerto") {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = ColorFieldBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "CAUSA DE LA MUERTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorLabel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    situaciones.forEach { situacion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (viewModel.situacionMuerte == situacion) ColorErrorBg else Color.Transparent
                                )
                                .clickable { viewModel.situacionMuerte = situacion }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = viewModel.situacionMuerte == situacion,
                                onClick = { viewModel.situacionMuerte = situacion },
                                colors = RadioButtonDefaults.colors(selectedColor = ColorError)
                            )
                            Text(
                                text = situacion,
                                fontSize = 13.sp,
                                color = if (viewModel.situacionMuerte == situacion) ColorError else ColorText,
                                fontWeight = if (viewModel.situacionMuerte == situacion) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }

                    if (viewModel.situacionMuerte == "Otro") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.otroMotivoMuerte,
                            onValueChange = { viewModel.otroMotivoMuerte = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Especifique...", color = ColorPlaceholder, fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ColorInputBg,
                                unfocusedContainerColor = ColorInputBg,
                                focusedBorderColor = ColorError,
                                unfocusedBorderColor = ColorFieldBorder,
                                focusedTextColor = ColorText,
                                unfocusedTextColor = ColorText
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.confirmarBaja() },
                enabled = viewModel.motivoBaja.isNotBlank() &&
                        (viewModel.motivoBaja != "Muerto" || viewModel.situacionMuerte.isNotBlank()),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorError,
                    contentColor = Color.White,
                    disabledContainerColor = ColorFieldBorder
                )
            ) {
                Text("Confirmar Baja", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.mostrarDialogoBaja = false }) {
                Text("Cancelar", fontSize = 13.sp, color = ColorSubtext)
            }
        }
    )
}