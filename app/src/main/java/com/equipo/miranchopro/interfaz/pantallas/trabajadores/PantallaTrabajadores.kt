package com.equipo.miranchopro.interfaz.pantallas.trabajadores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.interfaz.componentes.DialogoTrabajador
import com.equipo.miranchopro.viewmodel.TrabajadoresViewModel

@Composable
fun PantallaTrabajadores(
    viewModel: TrabajadoresViewModel
) {
    val trabajadores by viewModel.listaTrabajadores.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var trabajadorAEditar by remember { mutableStateOf<Usuario?>(null) }

    if (mostrarDialogo) {
        DialogoTrabajador(
            trabajadorExistente = trabajadorAEditar,
            onDismiss = { 
                mostrarDialogo = false
                trabajadorAEditar = null
            },
            onConfirm = { correo, contrasena ->
                if (trabajadorAEditar != null) {
                    viewModel.actualizarTrabajador(trabajadorAEditar!!.copy(contrasena = contrasena))
                } else {
                    viewModel.agregarTrabajador(correo, contrasena)
                }
                mostrarDialogo = false
                trabajadorAEditar = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    trabajadorAEditar = null
                    mostrarDialogo = true 
                },
                containerColor = Color(0xFF008577),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp).padding(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Alta", modifier = Modifier.size(36.dp))
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
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 40.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Equipo",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1.5).sp,
                        lineHeight = 52.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "${trabajadores.size} trabajadores registrados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00BFA5)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "PERSONAL DEL RANCHO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Gray,
                        letterSpacing = 2.sp
                    )
                }

                if (trabajadores.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No hay trabajadores registrados", color = Color.Gray)
                        }
                    }
                } else {
                    items(trabajadores, key = { it.correo }) { trabajador ->
                        TarjetaTrabajadorLujo(
                            trabajador = trabajador,
                            onEdit = {
                                trabajadorAEditar = trabajador
                                mostrarDialogo = true
                            },
                            onDelete = { viewModel.darDeBajaTrabajador(trabajador) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaTrabajadorLujo(
    trabajador: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF008577).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF008577), modifier = Modifier.size(28.dp))
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trabajador.correo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = trabajador.rol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF008577))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE53935))
                }
            }
        }
    }
}
