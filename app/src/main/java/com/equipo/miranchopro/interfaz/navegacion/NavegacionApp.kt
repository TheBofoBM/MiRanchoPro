package com.equipo.miranchopro.interfaz.navegacion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.interfaz.componentes.ShakeOverlay
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
import com.equipo.miranchopro.utils.ShakeDetector
import kotlinx.coroutines.launch

sealed class Pantalla(val ruta: String) {
    object Login : Pantalla("login")
    object Registro : Pantalla("registro")
    object Inventario : Pantalla("inventario")
    object RegistrarAnimal : Pantalla("registrar_animal?tipo={tipo}&idTemp={idTemp}") {
        fun crearRuta(tipo: String? = null, idTemp: String? = null): String {
            var r = "registrar_animal"
            val params = mutableListOf<String>()
            if (tipo != null) params.add("tipo=$tipo")
            if (idTemp != null) params.add("idTemp=$idTemp")
            if (params.isNotEmpty()) r += "?" + params.joinToString("&")
            return r
        }
    }
    object EditarAnimal : Pantalla("editar_animal/{idArete}") {
        fun crearRuta(idArete: String) = "editar_animal/$idArete"
    }
    object Inicio : Pantalla("inicio")
    object Salud : Pantalla("salud")
    object Lotes : Pantalla("lotes")
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
    val scope = rememberCoroutineScope()
    
    val database = remember { RanchoDatabase.getDatabase(context) }
    val repoAnimales = remember { AnimalRepository(database.animalDao()) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var mostrarShakeOverlay by remember { mutableStateOf(false) }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    
    val shakeDetector = remember { 
        ShakeDetector { 
            val rutaActual = navController.currentDestination?.route
            if (rutaActual != null && rutaActual != Pantalla.Login.ruta && rutaActual != Pantalla.Registro.ruta) {
                mostrarShakeOverlay = true 
            }
        } 
    }

    DisposableEffect(accelerometer) {
        if (accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(shakeDetector) }
    }

    val pantallasSinBarra = listOf(Pantalla.Login.ruta, Pantalla.Registro.ruta)
    val mostrarBarra = currentDestination?.route !in pantallasSinBarra

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (mostrarBarra) {
                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                        val items = listOf(
                            ItemNavegacion.Inicio, ItemNavegacion.Ganado, ItemNavegacion.Medico,
                            ItemNavegacion.Lotes, ItemNavegacion.Tareas, ItemNavegacion.Reportes
                        )
                        items.forEach { item ->
                            val esSeleccionado = currentDestination?.hierarchy?.any { it.route == item.ruta } == true
                            NavigationBarItem(
                                icon = { Icon(if (esSeleccionado) item.iconoSeleccionado else item.iconoNormal, item.titulo) },
                                label = { Text(item.titulo, fontSize = 10.sp) },
                                selected = esSeleccionado,
                                onClick = {
                                    navController.navigate(item.ruta) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF008577),
                                    selectedTextColor = Color(0xFF008577),
                                    indicatorColor = Color(0xFFE0F2F1)
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
                composable(Pantalla.Login.ruta) {
                    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(database.usuarioDao()) }
                    LoginScreen(viewModel = loginViewModel, onLoginExitoso = {
                        navController.navigate(Pantalla.Inventario.ruta) { popUpTo(Pantalla.Login.ruta) { inclusive = true } }
                    }, onRegisterClick = { navController.navigate(Pantalla.Registro.ruta) })
                }
                composable(Pantalla.Registro.ruta) {
                    val registroViewModel: RegistroViewModel = viewModel { RegistroViewModel(database.usuarioDao()) }
                    RegistroScreen(viewModel = registroViewModel, onRegistroExitoso = {
                        navController.navigate(Pantalla.Inventario.ruta) { popUpTo(Pantalla.Login.ruta) { inclusive = true } }
                    }, onIrALogin = { navController.popBackStack() })
                }
                
                composable(Pantalla.Inicio.ruta) { PantallaEnConstruccion("Inicio") }
                composable(Pantalla.Salud.ruta) { PantallaEnConstruccion("Médico") }
                composable(Pantalla.Lotes.ruta) { PantallaEnConstruccion("Lotes") }
                composable(Pantalla.Tareas.ruta) { PantallaEnConstruccion("Tareas") }
                composable(Pantalla.Reportes.ruta) { PantallaEnConstruccion("Reportes") }

                composable(Pantalla.Inventario.ruta) {
                    val invViewModel: InventarioViewModel = viewModel { InventarioViewModel(repoAnimales) }
                    PantallaInventario(
                        viewModel = invViewModel,
                        alSeleccionarAnimal = { idArete -> 
                            val animal = invViewModel.listaAnimales.find { it.idArete == idArete }
                            if (animal?.estado == "Pendiente") {
                                navController.navigate(Pantalla.RegistrarAnimal.crearRuta(idTemp = idArete))
                            } else {
                                navController.navigate(Pantalla.EditarAnimal.crearRuta(idArete))
                            }
                        },
                        alAgregarAnimal = { tipo -> navController.navigate(Pantalla.RegistrarAnimal.crearRuta(tipo)) }
                    )
                }
                composable(
                    route = Pantalla.RegistrarAnimal.ruta,
                    arguments = listOf(
                        navArgument("tipo") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("idTemp") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { entrada ->
                    val tipo = entrada.arguments?.getString("tipo")
                    val idTemp = entrada.arguments?.getString("idTemp")
                    val regViewModel: RegistrarAnimalViewModel = viewModel { RegistrarAnimalViewModel(repoAnimales) }
                    
                    LaunchedEffect(tipo, idTemp) {
                        if (idTemp != null) regViewModel.cargarParaCompletar(idTemp)
                        else if (tipo != null) regViewModel.tipo = tipo
                    }

                    PantallaRegistrarAnimal(viewModel = regViewModel, alFinalizar = { navController.popBackStack() })
                }
                composable(
                    route = Pantalla.EditarAnimal.ruta,
                    arguments = listOf(navArgument("idArete") { type = NavType.StringType })
                ) { entrada ->
                    val idArete = entrada.arguments?.getString("idArete") ?: ""
                    val editViewModel: EditarAnimalViewModel = viewModel { EditarAnimalViewModel(repoAnimales) }
                    PantallaEditarAnimal(idArete = idArete, viewModel = editViewModel, alVolver = { navController.popBackStack() })
                }
            }
        }

        if (mostrarShakeOverlay) {
            ShakeOverlay(
                onRegistrarNacimiento = {
                    mostrarShakeOverlay = false
                    scope.launch {
                        val resultado = repoAnimales.registrarNacimientoRapido()
                        resultado.onSuccess {
                            Toast.makeText(context, "Nacimiento registrado. ¡Suerte con la vaca!", Toast.LENGTH_LONG).show()
                        }.onFailure {
                            Toast.makeText(context, "Error al registrar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onRegistrarEnfermedad = {
                    mostrarShakeOverlay = false
                    navController.navigate(Pantalla.Salud.ruta)
                },
                onDismiss = { mostrarShakeOverlay = false }
            )
        }
    }
}

@Composable
fun PantallaEnConstruccion(nombre: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Módulo de $nombre (En desarrollo)", color = Color.Gray)
    }
}
