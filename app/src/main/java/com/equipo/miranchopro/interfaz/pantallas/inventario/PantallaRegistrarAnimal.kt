package com.equipo.miranchopro.interfaz.pantallas.inventario

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
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
fun PantallaRegistrarAnimal(
    viewModel: RegistrarAnimalViewModel = viewModel(),
    alFinalizar: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedLote by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is RegistrarAnimalViewModel.EventoUI.Exito -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                    alFinalizar()
                }
                is RegistrarAnimalViewModel.EventoUI.Error ->
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
                        text = "Registrar Animal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = alFinalizar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = ColorText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
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
                            text = "Nuevo registro de animal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                        Text(
                            text = "Completa los datos para agregar al inventario",
                            fontSize = 11.sp,
                            color = ColorLabel
                        )
                    }
                }
            }

            // ── Sección 1: Identificación ─────────────────────────────────
            SeccionRegistrar(
                icono = "🏷",
                titulo = "Identificación",
                descripcion = "Datos únicos del animal"
            ) {
                AnimalRegistrarInputField(
                    label = "ID ARETE (TAG)",
                    value = viewModel.idArete,
                    onValueChange = { viewModel.idArete = it },
                    placeholder = "Ej: MX-00124"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dropdown tipo de animal
                Text(
                    text = "TIPO DE ANIMAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorLabel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = !expandedTipo }
                ) {
                    OutlinedTextField(
                        value = viewModel.tipo,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccionar tipo...", color = ColorPlaceholder, fontSize = 13.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ColorInputBg,
                            unfocusedContainerColor = ColorInputBg,
                            focusedBorderColor = ColorPrimary,
                            unfocusedBorderColor = ColorFieldBorder,
                            focusedTextColor = ColorText,
                            unfocusedTextColor = if (viewModel.tipo.isEmpty()) ColorPlaceholder else ColorText
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        viewModel.tiposDisponibles.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo, fontSize = 13.sp, color = ColorText) },
                                onClick = { viewModel.tipo = tipo; expandedTipo = false }
                            )
                        }
                    }
                }
            }

            // ── Sección 2: Datos físicos ──────────────────────────────────
            SeccionRegistrar(
                icono = "📏",
                titulo = "Datos físicos",
                descripcion = "Medidas y características"
            ) {
                AnimalRegistrarInputField(
                    label = "PESO (KG)",
                    value = viewModel.peso,
                    onValueChange = { viewModel.peso = it },
                    placeholder = "Ej: 380.0",
                    keyboardType = KeyboardType.Decimal
                )

                Spacer(modifier = Modifier.height(14.dp))

                AnimalRegistrarInputField(
                    label = "EDAD",
                    value = viewModel.edad,
                    onValueChange = { viewModel.edad = it },
                    placeholder = "Ej: 1 año, 4 meses"
                )

                Spacer(modifier = Modifier.height(14.dp))

                AnimalRegistrarInputField(
                    label = "COLOR",
                    value = viewModel.color,
                    onValueChange = { viewModel.color = it },
                    placeholder = "Ej: Pardo con manchas blancas"
                )
            }

            // ── Sección 3: Ubicación ──────────────────────────────────────
            SeccionRegistrar(
                icono = "📍",
                titulo = "Ubicación",
                descripcion = "Lote o potrero asignado — opcional"
            ) {
                Text(
                    text = "ASIGNAR A LOTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorLabel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expandedLote,
                    onExpandedChange = { expandedLote = !expandedLote }
                ) {
                    OutlinedTextField(
                        value = viewModel.ubicacion,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Sin asignar (opcional)", color = ColorPlaceholder, fontSize = 13.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLote) },
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
                        expanded = expandedLote,
                        onDismissRequest = { expandedLote = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin asignar", fontSize = 13.sp, color = ColorSubtext) },
                            onClick = { viewModel.ubicacion = ""; expandedLote = false }
                        )
                        viewModel.lotesDisponibles.forEach { lote ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🌿", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(lote, fontSize = 13.sp, color = ColorText)
                                    }
                                },
                                onClick = { viewModel.ubicacion = lote; expandedLote = false }
                            )
                        }
                    }
                }
            }

            // ── Sección 4: Observaciones ──────────────────────────────────
            SeccionRegistrar(
                icono = "📝",
                titulo = "Observaciones",
                descripcion = "Marcas o señas particulares"
            ) {
                Text(
                    text = "MARCAS PARTICULARES",
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
                    placeholder = { Text("Ej: Marca en oreja derecha, cicatriz en lomo...", color = ColorPlaceholder, fontSize = 13.sp) },
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

            // ── Mensaje de error ──────────────────────────────────────────
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

            // ── Botón guardar ─────────────────────────────────────────────
            Button(
                onClick = { viewModel.registrarAnimal() },
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
                        text = "GUARDAR ANIMAL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            TextButton(
                onClick = alFinalizar,
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
private fun SeccionRegistrar(
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
private fun AnimalRegistrarInputField(
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