package com.equipo.miranchopro.interfaz.pantallas.lotes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.equipo.miranchopro.data.model.Animal
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
private val ColorCardBg      = Color(0xFFF8F9FA)
private val ColorError       = Color(0xFFD32F2F)
private val ColorErrorBg     = Color(0xFFFDECEC)
private val ColorWarning     = Color(0xFFF57C00)
private val ColorWarningBg   = Color(0xFFFFF3E0)

// ─── Pantalla principal ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleLote(
    lote: Lote,
    animalesEnLote: List<Animal>,
    todosLosLotes: List<Lote>,
    viewModel: LotesViewModel,
    onVolver: () -> Unit,
    onVerAnimal: (String) -> Unit
) {
    val progreso = if (lote.capacidadMaxima > 0)
        lote.ocupacionActual.toFloat() / lote.capacidadMaxima else 0f

    val esCritico     = progreso >= 0.9f
    val esAdvertencia = progreso >= 0.7f && !esCritico
    val estaVacio     = lote.ocupacionActual == 0

    val colorIndicador = when {
        esCritico     -> ColorError
        esAdvertencia -> ColorWarning
        else          -> ColorPrimary
    }

    // Estados de diálogos
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoMover    by remember { mutableStateOf(false) }
    var mostrarDialogoEditar   by remember { mutableStateOf(false) }

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
                        text = lote.nombre,
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
                actions = {
                    // Editar
                    IconButton(onClick = { mostrarDialogoEditar = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar lote", tint = ColorPrimary)
                    }
                    // Eliminar (solo habilitado si está vacío)
                    IconButton(
                        onClick = { mostrarDialogoEliminar = true },
                        enabled = estaVacio
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar lote",
                            tint = if (estaVacio) ColorError else ColorFieldBorder
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .alpha(alpha),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Sección 1: Indicador visual de capacidad ──────────────────
            item {
                TarjetaCapacidad(
                    lote = lote,
                    progreso = progreso,
                    colorIndicador = colorIndicador,
                    esCritico = esCritico,
                    esAdvertencia = esAdvertencia
                )
            }

            // ── Sección 2: Acciones operativas ────────────────────────────
            item {
                TarjetaAcciones(
                    estaVacio = estaVacio,
                    onMoverGanado = { mostrarDialogoMover = true },
                    onEliminarLote = { mostrarDialogoEliminar = true }
                )
            }

            // ── Sección 3: Encabezado lista animales ──────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANIMALES ASIGNADOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorLabel,
                        letterSpacing = 1.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ColorBadgeBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${animalesEnLote.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary
                        )
                    }
                }
            }

            // ── Sección 4: Lista de animales ──────────────────────────────
            if (animalesEnLote.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorCardBg)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🐄", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin animales asignados",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorSubtext,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Los animales asignados a este lote aparecerán aquí",
                                fontSize = 12.sp,
                                color = ColorPlaceholder,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(animalesEnLote) { animal ->
                    TarjetaAnimalCompacta(
                        animal = animal,
                        onClick = { onVerAnimal(animal.idArete) }
                    )
                }
            }
        }
    }

    // ── Diálogo: Eliminar lote ────────────────────────────────────────────────
    if (mostrarDialogoEliminar) {
        DialogoEliminarLote(
            lote = lote,
            estaVacio = estaVacio,
            onConfirmar = {
                viewModel.eliminarLote(lote) { exitoso, _ ->
                    if (exitoso) onVolver()
                }
            },
            onDismiss = { mostrarDialogoEliminar = false }
        )
    }

    // ── Diálogo: Mover ganado ─────────────────────────────────────────────────
    if (mostrarDialogoMover) {
        DialogoMoverGanado(
            loteOrigen = lote,
            animalesEnLote = animalesEnLote,
            todosLosLotes = todosLosLotes.filter { it.id != lote.id },
            onConfirmar = { loteDestino ->
                viewModel.moverGanado(loteOrigen = lote, loteDestino = loteDestino)
                mostrarDialogoMover = false
            },
            onDismiss = { mostrarDialogoMover = false }
        )
    }

    // ── Diálogo: Editar lote ──────────────────────────────────────────────────
    if (mostrarDialogoEditar) {
        DialogoEditarLote(
            lote = lote,
            onConfirmar = { nuevoNombre, nuevaCapacidad ->
                viewModel.actualizarLote(lote.copy(nombre = nuevoNombre, capacidadMaxima = nuevaCapacidad))
                mostrarDialogoEditar = false
            },
            onDismiss = { mostrarDialogoEditar = false }
        )
    }
}

// ─── Tarjeta de capacidad ────────────────────────────────────────────────────

@Composable
private fun TarjetaCapacidad(
    lote: Lote,
    progreso: Float,
    colorIndicador: Color,
    esCritico: Boolean,
    esAdvertencia: Boolean
) {
    val colorFondoEstado = when {
        esCritico     -> ColorErrorBg
        esAdvertencia -> ColorWarningBg
        else          -> ColorBadgeBg
    }
    val textoEstado = when {
        esCritico     -> "Lleno — sin espacio disponible"
        esAdvertencia -> "Casi lleno — capacidad limitada"
        else          -> "Con disponibilidad"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorFieldBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ocupación actual",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorLabel,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${lote.ocupacionActual}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorIndicador,
                            lineHeight = 42.sp
                        )
                        Text(
                            text = " / ${lote.capacidadMaxima}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorSubtext,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = "cabezas",
                        fontSize = 13.sp,
                        color = ColorLabel
                    )
                }

                // Indicador circular
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = ColorFieldBorder,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxSize(),
                        color = colorIndicador,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${(progreso * 100).toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorIndicador
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colorIndicador,
                trackColor = ColorFieldBorder
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorFondoEstado)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colorIndicador, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = textoEstado,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorIndicador
                    )
                }
            }
        }
    }
}

// ─── Tarjeta de acciones ─────────────────────────────────────────────────────

@Composable
private fun TarjetaAcciones(
    estaVacio: Boolean,
    onMoverGanado: () -> Unit,
    onEliminarLote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorFieldBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ACCIONES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ColorLabel,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mover ganado
                Button(
                    onClick = onMoverGanado,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "🔀", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mover ganado",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Eliminar lote
                OutlinedButton(
                    onClick = onEliminarLote,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (estaVacio) ColorError else ColorSubtext
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (estaVacio) ColorError else ColorFieldBorder
                    )
                ) {
                    Text(text = "🗑", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Eliminar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Aviso si no está vacío
            if (!estaVacio) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorWarningBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "⚠ El lote debe estar vacío para poder eliminarlo.",
                        fontSize = 11.sp,
                        color = ColorWarning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Tarjeta compacta de animal ──────────────────────────────────────────────

@Composable
private fun TarjetaAnimalCompacta(animal: Animal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorFieldBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(ColorBadgeBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐄", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${animal.tipo} ${animal.raza}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorText
                )
                Text(
                    text = "Arete: ${animal.idArete}",
                    fontSize = 12.sp,
                    color = ColorLabel
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${animal.peso} kg",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
                )
                Text(
                    text = "peso",
                    fontSize = 10.sp,
                    color = ColorSubtext
                )
            }
        }
    }
}

// ─── Diálogo: Eliminar lote ──────────────────────────────────────────────────

@Composable
private fun DialogoEliminarLote(
    lote: Lote,
    estaVacio: Boolean,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorBackground)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (estaVacio) "Eliminar lote" else "No se puede eliminar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (estaVacio)
                        "¿Estás seguro de que deseas eliminar el lote \"${lote.nombre}\"? Esta acción no se puede deshacer."
                    else
                        "El lote \"${lote.nombre}\" aún tiene ${lote.ocupacionActual} animal(es) asignado(s). Debes moverlos o retirarlos antes de eliminarlo.",
                    fontSize = 13.sp,
                    color = ColorSubtext,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                if (estaVacio) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar", color = ColorSubtext, fontSize = 13.sp)
                        }
                        Button(
                            onClick = onConfirmar,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                        ) {
                            Text("Eliminar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Text("Entendido", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Diálogo: Mover ganado ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoMoverGanado(
    loteOrigen: Lote,
    animalesEnLote: List<Animal>,
    todosLosLotes: List<Lote>,
    onConfirmar: (Lote) -> Unit,
    onDismiss: () -> Unit
) {
    var loteSeleccionado by remember { mutableStateOf<Lote?>(null) }
    var expandido by remember { mutableStateOf(false) }

    val capacidadSuficiente = loteSeleccionado?.let {
        (it.capacidadMaxima - it.ocupacionActual) >= animalesEnLote.size
    } ?: false

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorBackground)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Mover ganado",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Se moverán ${animalesEnLote.size} animal(es) desde \"${loteOrigen.nombre}\".",
                    fontSize = 13.sp,
                    color = ColorSubtext
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "LOTE DESTINO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorLabel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = !expandido }
                ) {
                    OutlinedTextField(
                        value = loteSeleccionado?.nombre ?: "Seleccionar lote...",
                        onValueChange = {},
                        readOnly = true,
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
                            unfocusedTextColor = if (loteSeleccionado == null) ColorPlaceholder else ColorText
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        todosLosLotes.forEach { lote ->
                            val espacioLibre = lote.capacidadMaxima - lote.ocupacionActual
                            val tieneEspacio = espacioLibre >= animalesEnLote.size

                            // SOLUCIÓN: Usamos un Row con modificador clickable para evitar errores del compilador
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = tieneEspacio) {
                                        loteSeleccionado = lote
                                        expandido = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lote.nombre,
                                    color = if (tieneEspacio) ColorText else ColorSubtext,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$espacioLibre libres",
                                    color = if (tieneEspacio) ColorPrimary else ColorError,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Aviso de validación de capacidad
                if (loteSeleccionado != null && !capacidadSuficiente) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorErrorBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "El lote destino no tiene suficiente capacidad para todos los animales.",
                            fontSize = 11.sp,
                            color = ColorError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = ColorSubtext, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { loteSeleccionado?.let { onConfirmar(it) } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = loteSeleccionado != null && capacidadSuficiente,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Text("Mover", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Diálogo: Editar lote ────────────────────────────────────────────────────

@Composable
private fun DialogoEditarLote(
    lote: Lote,
    onConfirmar: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(lote.nombre) }
    var capacidadStr by remember { mutableStateOf(lote.capacidadMaxima.toString()) }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorBackground)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Editar lote",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorText
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Nombre
                Text(
                    text = "NOMBRE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorLabel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = "" },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorInputBg,
                        unfocusedContainerColor = ColorInputBg,
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorFieldBorder,
                        focusedTextColor = ColorText,
                        unfocusedTextColor = ColorText
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Capacidad
                Text(
                    text = "CAPACIDAD MÁXIMA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorLabel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = capacidadStr,
                    onValueChange = { capacidadStr = it; error = "" },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorInputBg,
                        unfocusedContainerColor = ColorInputBg,
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorFieldBorder,
                        focusedTextColor = ColorText,
                        unfocusedTextColor = ColorText
                    )
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorErrorBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = error, fontSize = 11.sp, color = ColorError)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = ColorSubtext, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            val cap = capacidadStr.toIntOrNull()
                            if (nombre.isBlank() || cap == null || cap <= 0) {
                                error = "Ingresa un nombre y capacidad válida mayor a 0."
                            } else if (cap < lote.ocupacionActual) {
                                error = "La capacidad no puede ser menor a los animales actuales (${lote.ocupacionActual})."
                            } else {
                                onConfirmar(nombre.trim(), cap)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Text("Guardar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}