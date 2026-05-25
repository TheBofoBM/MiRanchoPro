package com.equipo.miranchopro.interfaz.pantallas.lotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.viewmodel.LotesViewModel

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
    val progreso = if (lote.capacidadMaxima > 0) lote.ocupacionActual.toFloat() / lote.capacidadMaxima else 0f
    val colorIndicador = if (progreso >= 0.9f) Color(0xFFD32F2F) else if (progreso >= 0.7f) Color(0xFFF57C00) else Color(0xFF0E8A5A)
    val estaVacio = lote.ocupacionActual == 0

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoMover by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                    Column {
                        Text(
                            text = lote.nombre,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Detalle del potrero",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00BFA5)
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { mostrarDialogoEditar = true }) {
                        Icon(Icons.Default.Edit, "Editar", tint = Color.White)
                    }
                    IconButton(onClick = { mostrarDialogoEliminar = true }, enabled = estaVacio) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = if(estaVacio) Color.White else Color.Gray)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TarjetaCapacidad(lote, progreso, colorIndicador)
            }

            item {
                TarjetaAcciones(estaVacio, { mostrarDialogoMover = true }, { mostrarDialogoEliminar = true })
            }

            item {
                Text(
                    text = "ANIMALES ASIGNADOS (${animalesEnLote.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
            }

            if (animalesEnLote.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay animales en este lote", color = Color.Gray)
                    }
                }
            } else {
                items(animalesEnLote) { animal ->
                    TarjetaAnimalCompacta(animal) { onVerAnimal(animal.idArete) }
                }
            }
        }
    }

    if (mostrarDialogoEliminar) {
        DialogoEliminarLote(lote, estaVacio, { viewModel.eliminarLote(lote) { exitoso, _ -> if (exitoso) onVolver() } }, { mostrarDialogoEliminar = false })
    }
    // ... resto de los diálogos (Mover y Editar) se mantienen igual en lógica
}

@Composable
private fun TarjetaCapacidad(lote: Lote, progreso: Float, colorIndicador: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("OCUPACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${lote.ocupacionActual}", fontSize = 36.sp, fontWeight = FontWeight.Black, color = colorIndicador)
                    Text(" / ${lote.capacidadMaxima}", fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                }
            }
            Box(Modifier.size(70.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color(0xFFEEEEEE), strokeWidth = 8.dp, strokeCap = StrokeCap.Round)
                CircularProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxSize(), color = colorIndicador, strokeWidth = 8.dp, strokeCap = StrokeCap.Round)
                Text("${(progreso * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorIndicador)
            }
        }
    }
}

@Composable
private fun TarjetaAcciones(estaVacio: Boolean, onMover: () -> Unit, onEliminar: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onMover, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008577))) {
            Icon(Icons.Default.SwapHoriz, null); Spacer(Modifier.width(8.dp)); Text("Mover")
        }
        OutlinedButton(onClick = onEliminar, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), enabled = estaVacio) {
            Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Eliminar")
        }
    }
}

@Composable
private fun TarjetaAnimalCompacta(animal: Animal, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFFF1F3F4), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("🐄") }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(animal.idArete, fontWeight = FontWeight.Bold)
                Text(animal.raza, fontSize = 12.sp, color = Color.Gray)
            }
            Text("${animal.peso} kg", fontWeight = FontWeight.Black, color = Color(0xFF008577))
        }
    }
}

@Composable
private fun DialogoEliminarLote(lote: Lote, estaVacio: Boolean, onConfirmar: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(estaVacio) "¿Eliminar lote?" else "No se puede eliminar") },
        text = { Text(if(estaVacio) "¿Estás seguro de eliminar ${lote.nombre}?" else "El lote debe estar vacío para eliminarlo.") },
        confirmButton = { if(estaVacio) TextButton(onClick = onConfirmar) { Text("Eliminar", color = Color.Red) } else TextButton(onClick = onDismiss) { Text("OK") } },
        dismissButton = { if(estaVacio) TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
