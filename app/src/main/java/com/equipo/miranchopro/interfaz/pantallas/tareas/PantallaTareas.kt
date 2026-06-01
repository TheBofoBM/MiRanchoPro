package com.equipo.miranchopro.interfaz.pantallas.tareas

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.model.Tarea
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.modelovista.TareasViewModel
import com.equipo.miranchopro.viewmodel.TrabajadoresViewModel
import com.equipo.miranchopro.interfaz.componentes.DialogoAsignarTarea
import com.equipo.miranchopro.interfaz.componentes.TarjetaTarea
import com.equipo.miranchopro.interfaz.navegacion.Pantalla

@Composable
fun PantallaTareas(
    navController: NavController,
    tareasViewModel: TareasViewModel,
    usuarioActual: Usuario? = null,
    trabajadoresViewModel: TrabajadoresViewModel = run {
        val context = LocalContext.current.applicationContext
        viewModel {
            TrabajadoresViewModel(RanchoDatabase.getDatabase(context).usuarioDao())
        }
    }
) {
    val listaTareasOriginal by tareasViewModel.listaTareas.collectAsState()
    val listaTrabajadores by trabajadoresViewModel.listaTrabajadores.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var tareaAEditar by remember { mutableStateOf<Tarea?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }

    // Filtrado por rol: Trabajadores solo ven lo asignado a ellos
    val listaTareas = remember(listaTareasOriginal, usuarioActual) {
        if (usuarioActual?.rol?.uppercase() == "TRABAJADOR") {
            listaTareasOriginal.filter { it.responsable == usuarioActual.correo }
        } else {
            listaTareasOriginal
        }
    }

    if (mostrarDialogo) {
        DialogoAsignarTarea(
            tareaExistente = tareaAEditar,
            listaTrabajadores = listaTrabajadores.map { it.correo },
            onDismiss = {
                mostrarDialogo = false
                tareaAEditar = null
            },
            onConfirm = { nuevaTarea: Tarea ->
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
            // Solo Administradores pueden crear tareas
            if (usuarioActual?.rol?.uppercase() != "TRABAJADOR") {
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
        }
    ) { padding ->
        // Clasificación de tareas
        val hoy = listaTareas.filter { it.fecha.isNullOrEmpty() }
        val proximas = listaTareas.filter { !it.fecha.isNullOrEmpty() }
        
        // Conteos actualizados correctamente basándose en la lista filtrada y estado de DB
        val totalCount = listaTareas.size
        val hechasCount = listaTareas.count { it.estaHecha }
        val pendientesCount = totalCount - hechasCount

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
                        .padding(start = 24.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tareas",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = if (usuarioActual?.rol?.uppercase() == "TRABAJADOR") "Tus asignaciones" else "${hoy.size} asignadas para hoy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00BFA5)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(Icons.Default.AccountCircle, "Menú", tint = Color.White, modifier = Modifier.size(30.dp))
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
                                text = { Text("Clima Detallado") },
                                onClick = { 
                                    menuExpandido = false
                                    navController.navigate(Pantalla.ClimaDetallado.ruta)
                                },
                                leadingIcon = { Icon(Icons.Default.Cloud, null) }
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                        CajaResumen("$totalCount", "Total", Modifier.weight(1f))
                        CajaResumen("$hechasCount", "Hechas", Modifier.weight(1f), Color(0xFF00897B))
                        CajaResumen("$pendientesCount", "Pendientes", Modifier.weight(1f), Color(0xFFEF6C00))
                    }
                }

                if (hoy.isNotEmpty()) {
                    item { Text("Para hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(hoy, key = { it.id }) { tarea ->
                        TarjetaTarea(
                            tarea = tarea,
                            onTareaClick = { seleccionada ->
                                if (usuarioActual?.rol?.uppercase() != "TRABAJADOR") {
                                    tareaAEditar = seleccionada
                                    mostrarDialogo = true
                                }
                            },
                            onToggleCompletada = { tareasViewModel.toggleTareaCompletada(tarea) },
                            onEliminar = if (usuarioActual?.rol?.uppercase() != "TRABAJADOR") ({ tareasViewModel.eliminarTarea(tarea) }) else null
                        )
                    }
                }

                if (proximas.isNotEmpty()) {
                    item { Text("Próximas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                    items(proximas, key = { it.id }) { tarea ->
                        TarjetaTarea(
                            tarea = tarea,
                            onTareaClick = { seleccionada ->
                                if (usuarioActual?.rol?.uppercase() != "TRABAJADOR") {
                                    tareaAEditar = seleccionada
                                    mostrarDialogo = true
                                }
                            },
                            onToggleCompletada = { tareasViewModel.toggleTareaCompletada(tarea) },
                            onEliminar = if (usuarioActual?.rol?.uppercase() != "TRABAJADOR") ({ tareasViewModel.eliminarTarea(tarea) }) else null
                        )
                    }
                }

                if (listaTareas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay tareas asignadas", color = Color.Gray)
                        }
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
