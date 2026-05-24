package com.equipo.miranchopro.interfaz.pantallas.tareas

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Tarea
import com.equipo.miranchopro.modelovista.TareasViewModel
import com.example.miranchopro.ui.componentes.DialogoAsignarTarea
import com.example.miranchopro.ui.componentes.TarjetaTarea

@Composable
fun PantallaTareas(
    navController: NavController,
    tareasViewModel: TareasViewModel = viewModel()
) {
    val listaTareas = tareasViewModel.listaTareas
    var mostrarDialogo by remember { mutableStateOf(false) }
    var tareaAEditar by remember { mutableStateOf<Tarea?>(null) }

    if (mostrarDialogo) {
        DialogoAsignarTarea(
            tareaExistente = tareaAEditar,
            onDismiss = {
                mostrarDialogo = false
                tareaAEditar = null
            },
            onConfirm = { nuevaTarea ->
                if (tareaAEditar != null) {
                    tareasViewModel.editarTarea(nuevaTarea)
                } else {
                    tareasViewModel.agregarTarea(nuevaTarea)
                }
                mostrarDialogo = false
                tareaAEditar = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    tareaAEditar = null
                    mostrarDialogo = true 
                },
                containerColor = Color(0xFF008577),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp).padding(8.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Añadir", modifier = Modifier.size(36.dp)) }
        }
    ) { padding ->
        val hoy = listaTareas.filter { it.fecha.isNullOrEmpty() }
        val proximas = listaTareas.filter { !it.fecha.isNullOrEmpty() }
        val hechas = hoy.count { it.estaHecha }

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
                        text = "Tareas",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1.5).sp,
                        lineHeight = 52.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "${hoy.size} asignadas para hoy",
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
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                        CajaResumen("${hoy.size}", "Total", Modifier.weight(1f))
                        CajaResumen("$hechas", "Hechas", Modifier.weight(1f), Color(0xFF00897B))
                        CajaResumen("${hoy.size - hechas}", "Pendientes", Modifier.weight(1f), Color(0xFFEF6C00))
                    }

                    Text("Hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    if (hoy.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Hoy no tienes tareas asignadas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }

                items(hoy, key = { it.id }) { tarea ->
                    TarjetaTarea(
                        tarea = tarea,
                        onTareaClick = {
                            tareaAEditar = it
                            mostrarDialogo = true
                        },
                        onToggleCompletada = {
                            tareasViewModel.toggleTareaCompletada(tarea)
                        },
                        onEliminar = { tareasViewModel.eliminarTarea(tarea) }
                    )
                }

                if (proximas.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Próximas tareas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(proximas, key = { it.id }) { tarea ->
                        TarjetaTarea(
                            tarea = tarea,
                            onTareaClick = {
                                tareaAEditar = it
                                mostrarDialogo = true
                            },
                            onToggleCompletada = {
                                tareasViewModel.toggleTareaCompletada(tarea)
                            },
                            onEliminar = { tareasViewModel.eliminarTarea(tarea) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CajaResumen(numero: String, etiqueta: String, modifier: Modifier, colorNumero: Color = Color.Black) {
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(numero, style = MaterialTheme.typography.headlineMedium, color = colorNumero, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
