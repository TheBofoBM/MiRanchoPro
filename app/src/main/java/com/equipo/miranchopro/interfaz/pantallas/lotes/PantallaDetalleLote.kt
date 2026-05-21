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
import com.equipo.miranchopro.modelovista.DetalleLoteViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleLote(
    lote: Lote,
    animalesEnLote: List<Animal>,
    todosLosLotes: List<Lote>,
    viewModel: DetalleLoteViewModel,
    onVolver: () -> Unit,
    onVerAnimal: (String) -> Unit
) {
    val ocupacionReal = animalesEnLote.size
    val progreso = if (lote.capacidadMaxima > 0) ocupacionReal.toFloat() / lote.capacidadMaxima else 0f
    val esCritico     = progreso >= 0.9f
    val esAdvertencia = progreso >= 0.7f && !esCritico
    val estaVacio     = ocupacionReal == 0

    val colorIndicador = when {
        esCritico     -> ColorError
        esAdvertencia -> ColorWarning
        else          -> ColorPrimary
    }

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoMover    by remember { mutableStateOf(false) }
    var mostrarDialogoEditar   by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(500), label = "")

    Scaffold(
        containerColor = ColorBackground,
        topBar = {
            TopAppBar(
                title = { Text(lote.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorText) },
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver", tint = ColorText) } },
                actions = {
                    IconButton(onClick = { mostrarDialogoEditar = true }) { Icon(Icons.Default.Edit, "Editar lote", tint = ColorPrimary) }
                    IconButton(onClick = { mostrarDialogoEliminar = true }, enabled = estaVacio) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = if (estaVacio) ColorError else ColorFieldBorder)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).alpha(alpha),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TarjetaCapacidad(
                    lote = lote, ocupacionReal = ocupacionReal, progreso = progreso,
                    colorIndicador = colorIndicador, esCritico = esCritico, esAdvertencia = esAdvertencia
                )
            }
            item {
                TarjetaAcciones(estaVacio = estaVacio, onMoverGanado = { mostrarDialogoMover = true }, onEliminarLote = { mostrarDialogoEliminar = true })
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ANIMALES ASIGNADOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel)
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(ColorBadgeBg).padding(horizontal = 10.dp, vertical = 3.dp)) {
                        Text("${animalesEnLote.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorPrimary)
                    }
                }
            }
            if (animalesEnLote.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ColorCardBg).padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🐄", fontSize = 32.sp)
                            Text("Sin animales asignados", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorSubtext)
                        }
                    }
                }
            } else {
                items(animalesEnLote) { animal ->
                    TarjetaAnimalCompacta(animal = animal, onClick = { onVerAnimal(animal.idArete) })
                }
            }
        }
    }

    if (mostrarDialogoEliminar) {
        DialogoEliminarLote(lote, estaVacio, onConfirmar = { viewModel.eliminarLote(lote) { _, _ -> onVolver() } }, onDismiss = { mostrarDialogoEliminar = false })
    }
    if (mostrarDialogoMover) {
        DialogoMoverGanado(
            loteOrigen = lote, animalesEnLote = animalesEnLote, todosLosLotes = todosLosLotes.filter { it.id != lote.id },
            onConfirmar = { loteDestino, seleccionados ->
                viewModel.enviarAnimales(loteDestino, seleccionados) { mostrarDialogoMover = false }
            },
            onDismiss = { mostrarDialogoMover = false }
        )
    }
    if (mostrarDialogoEditar) {
        DialogoEditarLote(lote, onConfirmar = { nombre, cap -> viewModel.actualizarLote(lote.copy(nombre = nombre, capacidadMaxima = cap)); mostrarDialogoEditar = false }, onDismiss = { mostrarDialogoEditar = false })
    }
}

@Composable
private fun TarjetaCapacidad(lote: Lote, ocupacionReal: Int, progreso: Float, colorIndicador: Color, esCritico: Boolean, esAdvertencia: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ColorCardBg), border = androidx.compose.foundation.BorderStroke(1.dp, ColorFieldBorder)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Ocupación actual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$ocupacionReal", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = colorIndicador, lineHeight = 42.sp)
                        Text(" / ${lote.capacidadMaxima}", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = ColorSubtext, modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = ColorFieldBorder, strokeWidth = 8.dp)
                    CircularProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxSize(), color = colorIndicador, strokeWidth = 8.dp)
                    Text("${(progreso * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorIndicador)
                }
            }
        }
    }
}

@Composable
private fun TarjetaAcciones(estaVacio: Boolean, onMoverGanado: () -> Unit, onEliminarLote: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ColorCardBg), border = androidx.compose.foundation.BorderStroke(1.dp, ColorFieldBorder)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ACCIONES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel, modifier = Modifier.padding(bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onMoverGanado, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)) {
                    Text("🔀 Mover ganado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onEliminarLote, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = if (estaVacio) ColorError else ColorSubtext)) {
                    Text("🗑 Eliminar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TarjetaAnimalCompacta(animal: Animal, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ColorCardBg), border = androidx.compose.foundation.BorderStroke(1.dp, ColorFieldBorder)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(38.dp).background(ColorBadgeBg, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text("🐄", fontSize = 18.sp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${animal.tipo} ${animal.raza}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorText)
                Text("Arete: ${animal.idArete}", fontSize = 12.sp, color = ColorLabel)
            }
            Text("${animal.peso} kg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorPrimary)
        }
    }
}

@Composable
private fun DialogoEliminarLote(lote: Lote, estaVacio: Boolean, onConfirmar: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (estaVacio) "Eliminar lote" else "No se puede eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                if (estaVacio) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(onClick = onConfirmar, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ColorError)) { Text("Eliminar") }
                    }
                } else {
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Entendido") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoMoverGanado(loteOrigen: Lote, animalesEnLote: List<Animal>, todosLosLotes: List<Lote>, onConfirmar: (Lote, List<Animal>) -> Unit, onDismiss: () -> Unit) {
    var loteSeleccionado by remember { mutableStateOf<Lote?>(null) }
    var expandido by remember { mutableStateOf(false) }
    var animalesSeleccionados by remember { mutableStateOf(setOf<Animal>()) }

    val espacioLibre = loteSeleccionado?.let { it.capacidadMaxima - it.ocupacionActual } ?: 0
    val capacidadSuficiente = loteSeleccionado != null && animalesSeleccionados.size <= espacioLibre

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Mover ganado", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("1. LOTE DESTINO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel)
                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                    OutlinedTextField(
                        value = loteSeleccionado?.nombre ?: "Seleccionar lote...", onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        todosLosLotes.forEach { lote ->
                            val libres = lote.capacidadMaxima - lote.ocupacionActual
                            DropdownMenuItem(text = { Text("${lote.nombre} ($libres libres)") }, onClick = { loteSeleccionado = lote; expandido = false }, enabled = libres > 0)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("2. SELECCIONA ANIMALES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorLabel)
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp).fillMaxWidth()) {
                    items(animalesEnLote) { animal ->
                        val isChecked = animalesSeleccionados.contains(animal)
                        Row(modifier = Modifier.fillMaxWidth().clickable { animalesSeleccionados = if (isChecked) animalesSeleccionados - animal else animalesSeleccionados + animal }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isChecked, onCheckedChange = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Arete: ${animal.idArete} (${animal.peso} kg)", fontSize = 13.sp)
                        }
                    }
                }
                if (loteSeleccionado != null && !capacidadSuficiente) Text("¡Espacio excedido!", color = Color.Red, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(onClick = { loteSeleccionado?.let { onConfirmar(it, animalesSeleccionados.toList()) } }, modifier = Modifier.weight(1f), enabled = loteSeleccionado != null && animalesSeleccionados.isNotEmpty() && capacidadSuficiente, colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)) { Text("Mover") }
                }
            }
        }
    }
}

@Composable
private fun DialogoEditarLote(lote: Lote, onConfirmar: (String, Int) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf(lote.nombre) }
    var capacidadStr by remember { mutableStateOf(lote.capacidadMaxima.toString()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = capacidadStr, onValueChange = { capacidadStr = it }, label = { Text("Capacidad") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(onClick = { onConfirmar(nombre, capacidadStr.toIntOrNull() ?: 0) }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                }
            }
        }
    }
}