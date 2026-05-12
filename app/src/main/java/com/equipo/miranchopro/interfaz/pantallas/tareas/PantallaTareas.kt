package com.equipo.miranchopro.interfaz.pantallas.tareas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Tarea
import com.equipo.miranchopro.interfaz.navegacion.Rutas
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
                containerColor = Color(0xFF00897B),
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Añadir") }
        },
        bottomBar = { BarraNavegacion(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                val hoy = listaTareas.filter { it.fecha.isNullOrEmpty() }
                val hechas = hoy.count { it.estaHecha }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rancho Ganado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tareas del Equipo", style = MaterialTheme.typography.titleLarge)
                Text("${hoy.size} tareas para hoy", color = Color.Gray)

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    CajaResumen("${hoy.size}", "Total", Modifier.weight(1f))
                    CajaResumen("$hechas", "Hechas", Modifier.weight(1f), Color(0xFF00897B))
                    CajaResumen("${hoy.size - hechas}", "Pendientes", Modifier.weight(1f), Color(0xFFEF6C00))
                }

                Text("Hoy (${hoy.size})", style = MaterialTheme.typography.titleMedium)
            }

            items(listaTareas.filter { it.fecha.isNullOrEmpty() }, key = { it.id }) { tarea ->
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

            val proximas = listaTareas.filter { !it.fecha.isNullOrEmpty() }
            if (proximas.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Próximas tareas", style = MaterialTheme.typography.titleMedium)
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

@Composable
fun CajaResumen(numero: String, etiqueta: String, modifier: Modifier, colorNumero: Color = Color.Black) {
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(numero, style = MaterialTheme.typography.headlineMedium, color = colorNumero, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun BarraNavegacion(navController: NavController) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Home, "Inicio") }, label = { Text("Inicio") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Pets, "Ganado") }, label = { Text("Ganado") })
        NavigationBarItem(
            selected = false, 
            onClick = { navController.navigate(Rutas.Inventario.ruta) }, 
            icon = { Icon(Icons.Default.FavoriteBorder, "Médico") }, 
            label = { Text("Médico") }
        )
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.GridView, "Lotes") }, label = { Text("Lotes") })
        NavigationBarItem(
            selected = true, 
            onClick = { navController.navigate(Rutas.Tareas.ruta) }, 
            icon = { Icon(Icons.Outlined.PeopleOutline, "Tareas") }, 
            label = { Text("Tareas") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00897B), 
                selectedTextColor = Color(0xFF00897B), 
                indicatorColor = Color(0xFFE0F2F1)
            )
        )
    }
}
