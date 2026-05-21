package com.equipo.miranchopro.interfaz.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import com.equipo.miranchopro.modelovista.DetalleLoteViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository

// Imports para Lotes
import com.equipo.miranchopro.data.repository.LoteRepository
import com.equipo.miranchopro.interfaz.pantallas.lotes.PantallaLotes
import com.equipo.miranchopro.interfaz.pantallas.lotes.PantallaRegistrarLote
import com.equipo.miranchopro.interfaz.pantallas.lotes.PantallaDetalleLote
import com.equipo.miranchopro.viewmodel.LotesViewModel

import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaEditarAnimal
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaInventario
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaRegistrarAnimal
import com.equipo.miranchopro.interfaz.pantallas.login.LoginScreen
import com.equipo.miranchopro.interfaz.pantallas.registro.RegistroScreen
import com.equipo.miranchopro.modelovista.EditarAnimalViewModel
import com.equipo.miranchopro.modelovista.InventarioViewModel
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
import com.equipo.miranchopro.viewmodel.LoginViewModel
import com.equipo.miranchopro.viewmodel.RegistroViewModel

sealed class Pantalla(val ruta: String) {
    object Login : Pantalla("login")
    object Registro : Pantalla("registro")
    object Inventario : Pantalla("inventario")
    object RegistrarAnimal : Pantalla("registrar_animal")
    object EditarAnimal : Pantalla("editar_animal/{idArete}") {
        fun crearRuta(idArete: String) = "editar_animal/$idArete"
    }
    object Inicio : Pantalla("inicio")
    object Salud : Pantalla("salud")
    object Lotes : Pantalla("lotes")

    // Rutas para las funciones de lotes
    object RegistrarLote : Pantalla("registrar_lote")
    object DetalleLote : Pantalla("detalle_lote/{idLote}") {
        fun crearRuta(idLote: Int) = "detalle_lote/$idLote"
    }

    object Tareas : Pantalla("tareas")
    object Reportes : Pantalla("reportes")
}

sealed class ItemNavegacion(
    val ruta: String,
    val titulo: String,
    val iconoNormal: ImageVector,
    val iconoSeleccionado: ImageVector
) {
    object Inicio : ItemNavegacion(Pantalla.Inicio.ruta, "Inicio", Icons.Outlined.Home, Icons.Filled.Home)
    object Ganado : ItemNavegacion(Pantalla.Inventario.ruta, "Ganado", Icons.Outlined.Agriculture, Icons.Filled.Agriculture)
    object Medico : ItemNavegacion(Pantalla.Salud.ruta, "Médico", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite)
    object Lotes : ItemNavegacion(Pantalla.Lotes.ruta, "Lotes", Icons.Outlined.GridView, Icons.Filled.GridView)
    object Tareas : ItemNavegacion(Pantalla.Tareas.ruta, "Tareas", Icons.Outlined.Assignment, Icons.Filled.Assignment)
    object Reportes : ItemNavegacion(Pantalla.Reportes.ruta, "Reportes", Icons.Outlined.Assessment, Icons.Filled.Assessment)
}

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = RanchoDatabase.getDatabase(context)

    val repoAnimales = AnimalRepository(database.animalDao())
    val repoLotes = LoteRepository(database.loteDao())

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Mostrar barra solo si no estamos en Login o Registro
    val pantallasSinBarra = listOf(Pantalla.Login.ruta, Pantalla.Registro.ruta)
    val mostrarBarra = currentDestination?.route !in pantallasSinBarra

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                ) {
                    val items = listOf(
                        ItemNavegacion.Inicio,
                        ItemNavegacion.Ganado,
                        ItemNavegacion.Medico,
                        ItemNavegacion.Lotes,
                        ItemNavegacion.Tareas,
                        ItemNavegacion.Reportes
                    )
                    items.forEach { item ->
                        // Lógica para mantener seleccionado el icono correcto incluso en sub-pantallas
                        val esSeleccionado = currentDestination?.hierarchy?.any { it.route == item.ruta } == true ||
                                (item == ItemNavegacion.Ganado && (currentDestination?.route == Pantalla.RegistrarAnimal.ruta || currentDestination?.route?.startsWith("editar_animal") == true)) ||
                                (item == ItemNavegacion.Lotes && (currentDestination?.route == Pantalla.RegistrarLote.ruta || currentDestination?.route?.startsWith("detalle_lote") == true))

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (esSeleccionado) item.iconoSeleccionado else item.iconoNormal,
                                    contentDescription = item.titulo
                                )
                            },
                            label = { Text(item.titulo, fontSize = 10.sp) },
                            selected = esSeleccionado,
                            onClick = {
                                navController.navigate(item.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF008577),
                                selectedTextColor = Color(0xFF008577),
                                indicatorColor = Color(0xFFE0F2F1),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Pantalla.Login.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            // AUTH
            composable(Pantalla.Login.ruta) {
                val loginViewModel: LoginViewModel = viewModel { LoginViewModel(database.usuarioDao()) }
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginExitoso = {
                        navController.navigate(Pantalla.Inicio.ruta) {
                            popUpTo(Pantalla.Login.ruta) { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate(Pantalla.Registro.ruta) }
                )
            }

            composable(Pantalla.Registro.ruta) {
                val registroViewModel: RegistroViewModel = viewModel { RegistroViewModel(database.usuarioDao()) }
                RegistroScreen(
                    viewModel = registroViewModel,
                    onRegistroExitoso = {
                        navController.navigate(Pantalla.Inicio.ruta) {
                            popUpTo(Pantalla.Login.ruta) { inclusive = true }
                        }
                    },
                    onIrALogin = { navController.popBackStack() }
                )
            }

            // PRINCIPALES
            composable(Pantalla.Inicio.ruta) { PantallaEnConstruccion("Inicio") }
            composable(Pantalla.Salud.ruta) { PantallaEnConstruccion("Médico") }
            composable(Pantalla.Tareas.ruta) { PantallaEnConstruccion("Tareas") }
            composable(Pantalla.Reportes.ruta) { PantallaEnConstruccion("Reportes") }

            // INVENTARIO
            composable(Pantalla.Inventario.ruta) {
                val invViewModel: InventarioViewModel = viewModel { InventarioViewModel(repoAnimales) }
                PantallaInventario(
                    viewModel = invViewModel,
                    alSeleccionarAnimal = { idArete -> navController.navigate(Pantalla.EditarAnimal.crearRuta(idArete)) },
                    alAgregarAnimal = { navController.navigate(Pantalla.RegistrarAnimal.ruta) }
                )
            }
            composable(Pantalla.RegistrarAnimal.ruta) {
                val regViewModel: RegistrarAnimalViewModel = viewModel { RegistrarAnimalViewModel(repoAnimales, repoLotes) }
                PantallaRegistrarAnimal(viewModel = regViewModel, alFinalizar = { navController.popBackStack() })
            }
            composable(
                route = Pantalla.EditarAnimal.ruta,
                arguments = listOf(navArgument("idArete") { type = NavType.StringType })
            ) { entrada ->
                val idArete = entrada.arguments?.getString("idArete") ?: ""
                val editViewModel: EditarAnimalViewModel = viewModel { EditarAnimalViewModel(repoAnimales, repoLotes) }
                PantallaEditarAnimal(idArete = idArete, viewModel = editViewModel, alVolver = { navController.popBackStack() })
            }

            // MÓDULO DE LOTES
            composable(Pantalla.Lotes.ruta) {
                val lotesViewModel: LotesViewModel = viewModel { LotesViewModel(repoLotes, repoAnimales) }
                PantallaLotes(
                    viewModel = lotesViewModel,
                    onAgregarLote = {
                        navController.navigate(Pantalla.RegistrarLote.ruta)
                    },
                    onVerDetalle = { idLote ->
                        navController.navigate(Pantalla.DetalleLote.crearRuta(idLote))
                    }
                )
            }

            composable(Pantalla.RegistrarLote.ruta) {
                val lotesViewModel: LotesViewModel = viewModel { LotesViewModel(repoLotes, repoAnimales) }
                PantallaRegistrarLote(
                    viewModel = lotesViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }

            // DETALLE DEL LOTE (Implementación limpia)
            composable(
                route = Pantalla.DetalleLote.ruta,
                arguments = listOf(navArgument("idLote") { type = NavType.IntType })
            ) { entrada ->
                val idLote = entrada.arguments?.getInt("idLote") ?: 0

                // 1. Instanciamos el ViewModel que maneja la lógica de detalle
                val detalleLoteViewModel: DetalleLoteViewModel = viewModel {
                    DetalleLoteViewModel(repoAnimales, repoLotes)
                }

                // 2. Instanciamos el ViewModel de Lotes para obtener la lista general y el lote específico
                val lotesViewModel: LotesViewModel = viewModel {LotesViewModel(repoLotes, repoAnimales) }

                // Observamos los datos
                val lotes by lotesViewModel.lotes.collectAsState()
                val animalesEnLote by detalleLoteViewModel.animalesEnLote.collectAsState()
                val todosLosLotes by lotesViewModel.lotes.collectAsState()

                // Buscamos el lote actual
                val loteSeleccionado = lotes.find { it.id == idLote }

                // 3. Cargamos los datos del lote cuando el lote sea encontrado
                LaunchedEffect(loteSeleccionado) {
                    loteSeleccionado?.let {
                        detalleLoteViewModel.cargarDatosLote(it.nombre)
                    }
                }

                // 4. Renderizamos la pantalla solo si el lote existe
                if (loteSeleccionado != null) {
                    PantallaDetalleLote(
                        lote = loteSeleccionado,
                        animalesEnLote = animalesEnLote,
                        todosLosLotes = todosLosLotes,
                        viewModel = detalleLoteViewModel, // <-- CORRECCIÓN: Se pasa lotesViewModel para que coincida con la vista
                        onVolver = { navController.popBackStack() },
                        onVerAnimal = { idAnimal ->
                            navController.navigate(Pantalla.EditarAnimal.crearRuta(idAnimal))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaEnConstruccion(nombre: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Módulo de $nombre (En desarrollo)", color = Color.Gray)
    }
}