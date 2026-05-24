package com.equipo.miranchopro.interfaz.pantallas.insumos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo.miranchopro.data.model.Insumo
import com.equipo.miranchopro.interfaz.componentes.DialogoInsumo
import com.equipo.miranchopro.viewmodel.InsumoViewModel

@Composable
fun PantallaInsumos(
    viewModel: InsumoViewModel
) {
    val insumos by viewModel.listaInsumos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var insumoAEditar by remember { mutableStateOf<Insumo?>(null) }
    var mostrarDialogoConsumo by remember { mutableStateOf<Insumo?>(null) }

    if (mostrarDialogo) {
        DialogoInsumo(
            insumoExistente = insumoAEditar,
            onDismiss = { 
                mostrarDialogo = false
                insumoAEditar = null
            },
            onConfirm = { nombre, tipo, cantidad, unidad ->
                if (insumoAEditar != null) {
                    // Lógica para actualizar si fuera necesario, por ahora solo agregar
                    viewModel.agregarInsumo(nombre, tipo, cantidad, unidad)
                } else {
                    viewModel.agregarInsumo(nombre, tipo, cantidad, unidad)
                }
                mostrarDialogo = false
                insumoAEditar = null
            }
        )
    }

    if (mostrarDialogoConsumo != null) {
        DialogoRegistrarConsumo(
            insumo = mostrarDialogoConsumo!!,
            onDismiss = { mostrarDialogoConsumo = null },
            onConfirm = { cantidad ->
                viewModel.registrarConsumo(mostrarDialogoConsumo!!, cantidad)
                mostrarDialogoConsumo = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    insumoAEditar = null
                    mostrarDialogo = true 
                },
                containerColor = Color(0xFF00897B),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Insumo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Inventario de Insumos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Control de alimentación y forrajes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (insumos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay insumos registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(insumos, key = { it.id }) { insumo ->
                        TarjetaInsumo(
                            insumo = insumo,
                            onRegistrarConsumo = { mostrarDialogoConsumo = insumo },
                            onAbastecer = { /* Lógica para añadir stock */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaInsumo(
    insumo: Insumo,
    onRegistrarConsumo: () -> Unit,
    onAbastecer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Color(0xFF00897B),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(insumo.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(insumo.tipo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(
                    "${insumo.cantidad} ${insumo.unidadMedida}",
                    fontWeight = FontWeight.Black,
                    color = if (insumo.cantidad <= insumo.stockMinimo) Color.Red else Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRegistrarConsumo) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Registrar Suministro")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAbastecer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Abastecer")
                }
            }
        }
    }
}

@Composable
fun DialogoRegistrarConsumo(
    insumo: Insumo,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var cantidad by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Suministro") },
        text = {
            Column {
                Text("¿Cuánto de ${insumo.nombre} se suministró?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) cantidad = it },
                    label = { Text("Cantidad (${insumo.unidadMedida})") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                cantidad.toDoubleOrNull()?.let { onConfirm(it) }
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
