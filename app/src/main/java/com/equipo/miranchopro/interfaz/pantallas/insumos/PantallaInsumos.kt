package com.equipo.miranchopro.interfaz.pantallas.insumos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Insumo
import com.equipo.miranchopro.interfaz.componentes.DialogoInsumo
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.viewmodel.InsumoViewModel

@Composable
fun PantallaInsumos(
    navController: NavController,
    viewModel: InsumoViewModel
) {
    val insumos by viewModel.listaInsumos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var insumoAEditar by remember { mutableStateOf<Insumo?>(null) }
    var insumoAAbastecer by remember { mutableStateOf<Insumo?>(null) }
    var mostrarDialogoConsumo by remember { mutableStateOf<Insumo?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        DialogoInsumo(
            insumoExistente = insumoAEditar,
            onDismiss = { 
                mostrarDialogo = false
                insumoAEditar = null
            },
            onConfirm = { nombre, tipo, cantidad, unidad ->
                viewModel.agregarInsumo(nombre, tipo, cantidad, unidad)
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

    if (insumoAAbastecer != null) {
        DialogoAbastecerInsumo(
            insumo = insumoAAbastecer!!,
            onDismiss = { insumoAAbastecer = null },
            onConfirm = { cantidad ->
                viewModel.abastecerInsumo(insumoAAbastecer!!, cantidad)
                insumoAAbastecer = null
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
                containerColor = Color(0xFF008577),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp).padding(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(36.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Insumos",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Alimentación y forrajes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00BFA5)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(Icons.Default.AccountCircle, "Menú", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                onClick = { 
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Perfil.ruta)
                                },
                                leadingIcon = { Icon(Icons.Default.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = { 
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Configuracion.ruta)
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = { 
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Login.ruta) {
                                        popUpTo(0)
                                    }
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                            )
                        }
                    }
                }
            }

            if (insumos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay insumos registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(insumos, key = { it.id }) { insumo ->
                        TarjetaInsumo(
                            insumo = insumo,
                            onRegistrarConsumo = { mostrarDialogoConsumo = insumo },
                            onAbastecer = { insumoAAbastecer = insumo },
                            onDelete = { viewModel.eliminarInsumo(insumo) }
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
    onAbastecer: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
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
                        tint = Color(0xFF008577),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(insumo.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(insumo.tipo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${insumo.cantidad} ${insumo.unidadMedida}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (insumo.cantidad <= 10) Color.Red else Color.Black
                    )
                    if (insumo.cantidad <= 10) {
                        Text("¡Stock Bajo!", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.LightGray)
                }
                Row {
                    TextButton(onClick = onRegistrarConsumo) {
                        Text("Gastar", color = Color(0xFF008577))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAbastecer,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008577)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Abastecer")
                    }
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
                Text("¿Cuánto de ${insumo.nombre} se utilizó?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) cantidad = it },
                    label = { Text("Cantidad a descontar (${insumo.unidadMedida})") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                cantidad.toDoubleOrNull()?.let { onConfirm(it) }
            }) {
                Text("Descontar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoAbastecerInsumo(
    insumo: Insumo,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var cantidad by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abastecer Inventario") },
        text = {
            Column {
                Text("¿Cuánto de ${insumo.nombre} se compró/recibió?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) cantidad = it },
                    label = { Text("Cantidad a añadir (${insumo.unidadMedida})") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                cantidad.toDoubleOrNull()?.let { onConfirm(it) }
            }) {
                Text("Sumar al stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
