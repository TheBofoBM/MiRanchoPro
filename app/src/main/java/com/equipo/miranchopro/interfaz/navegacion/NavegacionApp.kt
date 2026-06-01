package com.equipo.miranchopro.interfaz.navegacion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
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
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.LoteRepository
import com.equipo.miranchopro.data.repository.SaludRepository
import com.equipo.miranchopro.data.repository.TareaRepository
import com.equipo.miranchopro.domain.usecase.GenerarReporteUseCase
import com.equipo.miranchopro.interfaz.componentes.ShakeOverlay
import com.equipo.miranchopro.interfaz.pantallas.inicio.PantallaInicio
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaEditarAnimal
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaInventario
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaRegistrarAnimal
import com.equipo.miranchopro.interfaz.pantallas.login.LoginScreen
import com.equipo.miranchopro.interfaz.pantallas.lotes.PantallaLotes
import com.equipo.miranchopro.interfaz.pantallas.lotes.PantallaDetalleLote
import com.equipo.miranchopro.interfaz.pantallas.lotes.PantallaRegistrarLote
import com.equipo.miranchopro.interfaz.pantallas.registro.RegistroScreen
import com.equipo.miranchopro.interfaz.pantallas.reportes.PantallaReportes
import com.equipo.miranchopro.interfaz.pantallas.salud.PantallaSalud
import com.equipo.miranchopro.interfaz.pantallas.tareas.PantallaTareas
import com.equipo.miranchopro.interfaz.pantallas.trabajadores.PantallaTrabajadores
import com.equipo.miranchopro.interfaz.pantallas.perfil.PantallaPerfil
import com.equipo.miranchopro.interfaz.pantallas.configuracion.PantallaConfiguracion
import com.equipo.miranchopro.interfaz.pantallas.clima.PantallaClimaDetallado
import com.equipo.miranchopro.interfaz.pantallas.insumos.PantallaInsumos
import com.equipo.miranchopro.modelovista.*
import com.equipo.miranchopro.utils.ShakeDetector
import com.equipo.miranchopro.viewmodel.*
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
    object RegistrarLote : Pantalla("registrar_lote")
    object DetalleLote : Pantalla("detalle_lote/{idLote}") {
        fun crearRuta(idLote: Int) = "detalle_lote/$idLote"
    }
    object Tareas : Pantalla("tareas")
    object Reportes : Pantalla("reportes")
    object Trabajadores : Pantalla("trabajadores")
    object Perfil : Pantalla("perfil")
    object Configuracion : Pantalla("configuracion")
    object Insumos : Pantalla("insumos")
    object ClimaDetallado : Pantalla("clima_detallado")
}

sealed class ItemNavegacion(
    val ruta: String,
    val titulo: String,
    val iconoNormal: ImageVector,
    val iconoSeleccionado: ImageVector
) {
    object Inicio : ItemNavegacion(Pantalla.Inicio.ruta, "Inicio", Icons.Outlined.Home, Icons.Filled.Home)
    object Ganado : ItemNavegacion(Pantalla.Inventario.ruta, "Ganado", Icons.Outlined.Agriculture, Icons.Filled.Agriculture)
    object Medico : ItemNavegacion(Pantalla.Salud.ruta, "Médico", Icons.Outlined.MedicalServices, Icons.Filled.MedicalServices)
    object Tareas : ItemNavegacion(Pantalla.Tareas.ruta, "Tareas", Icons.AutoMirrored.Outlined.ListAlt, Icons.AutoMirrored.Filled.ListAlt)
    object Reportes : ItemNavegacion(Pantalla.Reportes.ruta, "Reportes", Icons.Outlined.Assessment, Icons.Filled.Assessment)
    object Equipo : ItemNavegacion(Pantalla.Trabajadores.ruta, "Equipo", Icons.Outlined.People, Icons.Filled.People)
}

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val database = remember { RanchoDatabase.getDatabase(context) }
    val repoAnimales = remember { AnimalRepository(database.animalDao()) }
    val repoLotes = remember { LoteRepository(database.loteDao()) }
    val repoSalud = remember { SaludRepository(database.medicamentoDao(), database.vacunacionDao(), database.enfermedadDao()) }
    val repoTareas = remember { TareaRepository(database.tareaDao()) }

    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (mostrarBarra) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        val items = listOf(
                            ItemNavegacion.Inicio,
                            ItemNavegacion.Ganado,
                            ItemNavegacion.Medico,
                            ItemNavegacion.Tareas,
                            ItemNavegacion.Reportes,
                            ItemNavegacion.Equipo
                        )
                        items.forEach { item ->
                            val esSeleccionado = currentDestination?.hierarchy?.any { it.route == item.ruta } == true

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
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                composable(Pantalla.Login.ruta) {
                    val loginViewModel: LoginViewModel = viewModel { LoginViewModel(database.usuarioDao()) }
                    LoginScreen(
                        onLoginExitoso = { user ->
                            usuarioActual = user
                            navController.navigate(Pantalla.Inicio.ruta) { popUpTo(Pantalla.Login.ruta) { inclusive = true } }
                        }, 
                        onRegisterClick = { navController.navigate(Pantalla.Registro.ruta) },
                        onForgotPassword = { /* Implementar recuperación */ },
                        viewModel = loginViewModel
                    )
                }
                
                composable(Pantalla.Registro.ruta) {
                    val registroViewModel: RegistroViewModel = viewModel { RegistroViewModel(database.usuarioDao()) }
                    RegistroScreen(viewModel = registroViewModel, onRegistroExitoso = {
                        navController.navigate(Pantalla.Inicio.ruta) { popUpTo(Pantalla.Login.ruta) { inclusive = true } }
                    }, onIrALogin = { navController.popBackStack() } )
                }
                
                composable(Pantalla.Inicio.ruta) {
                    val invViewModel: InventarioViewModel = viewModel { InventarioViewModel(repoAnimales) }
                    val saludViewModel: SaludViewModel = viewModel { SaludViewModel(repoSalud) }
                    val tarViewModel: TareasViewModel = viewModel { TareasViewModel(repoTareas) }
                    val climaViewModel: ClimaViewModel = viewModel()
                    PantallaInicio(
                        navController = navController, 
                        inventarioViewModel = invViewModel, 
                        saludViewModel = saludViewModel,
                        usuarioActual = usuarioActual,
                        tareasViewModel = tarViewModel,
                        climaViewModel = climaViewModel
                    )
                }
                
                composable(Pantalla.Tareas.ruta) {
                    val tareasViewModel: TareasViewModel = viewModel { TareasViewModel(repoTareas) }
                    val trabViewModel: TrabajadoresViewModel = viewModel { TrabajadoresViewModel(database.usuarioDao()) }
                    PantallaTareas(
                        navController = navController, 
                        tareasViewModel = tareasViewModel, 
                        trabajadoresViewModel = trabViewModel,
                        usuarioActual = usuarioActual
                    )
                }
                
                composable(Pantalla.Salud.ruta) {
                    val saludViewModel: SaludViewModel = viewModel { SaludViewModel(repoSalud) }
                    PantallaSalud(navController = navController, viewModel = saludViewModel)
                }
                
                composable(Pantalla.Reportes.ruta) {
                    val useCase = GenerarReporteUseCase(repoAnimales)
                    val reporteViewModel: ReporteViewModel = viewModel { ReporteViewModel(useCase) }
                    PantallaReportes(navController = navController, viewModel = reporteViewModel)
                }
                
                composable(Pantalla.Trabajadores.ruta) {
                    val trabViewModel: TrabajadoresViewModel = viewModel { TrabajadoresViewModel(database.usuarioDao()) }
                    PantallaTrabajadores(navController = navController, viewModel = trabViewModel)
                }
                
                composable(Pantalla.Insumos.ruta) {
                    val insumoViewModel: InsumoViewModel = viewModel { InsumoViewModel(database.insumoDao()) }
                    PantallaInsumos(navController = navController, viewModel = insumoViewModel)
                }

                composable(Pantalla.ClimaDetallado.ruta) {
                    val climaViewModel: ClimaViewModel = viewModel()
                    PantallaClimaDetallado(navController = navController, climaViewModel = climaViewModel)
                }

                composable(Pantalla.Inventario.ruta) {
                    val invViewModel: InventarioViewModel = viewModel { InventarioViewModel(repoAnimales) }
                    PantallaInventario(navController = navController, viewModel = invViewModel, alSeleccionarAnimal = { id -> 
                        val animal = invViewModel.listaAnimales.find { it.idArete == id }
                        if (animal?.estado == "Pendiente") navController.navigate(Pantalla.RegistrarAnimal.crearRuta(idTemp = id))
                        else navController.navigate(Pantalla.EditarAnimal.crearRuta(id))
                    }, alAgregarAnimal = { tipo -> navController.navigate(Pantalla.RegistrarAnimal.crearRuta(tipo)) })
                }
                
                composable(Pantalla.Lotes.ruta) {
                    val lotesViewModel: LotesViewModel = viewModel { LotesViewModel(repoLotes) }
                    PantallaLotes(
                        navController = navController,
                        viewModel = lotesViewModel,
                        onAgregarLote = { navController.navigate(Pantalla.RegistrarLote.ruta) },
                        onVerDetalle = { idLote -> navController.navigate(Pantalla.DetalleLote.crearRuta(idLote)) }
                    )
                }
                
                composable(Pantalla.RegistrarLote.ruta) {
                    val lotesViewModel: LotesViewModel = viewModel { LotesViewModel(repoLotes) }
                    PantallaRegistrarLote(viewModel = lotesViewModel, onVolver = { navController.popBackStack() })
                }
                
                composable(
                    route = Pantalla.DetalleLote.ruta,
                    arguments = listOf(navArgument("idLote") { type = NavType.IntType })
                ) { backStackEntry ->
                    val idLote = backStackEntry.arguments?.getInt("idLote") ?: 0
                    val lotesViewModel: LotesViewModel = viewModel { LotesViewModel(repoLotes) }
                    val invViewModel: InventarioViewModel = viewModel { InventarioViewModel(repoAnimales) }
                    val lotesState by lotesViewModel.lotes.collectAsState()
                    val lote = lotesState.find { it.id == idLote }
                    val animales = invViewModel.listaAnimales.filter { it.ubicacion == lote?.nombre }
                    
                    if (lote != null) {
                        PantallaDetalleLote(
                            lote = lote,
                            animalesEnLote = animales,
                            todosLosLotes = lotesState,
                            viewModel = lotesViewModel,
                            onVolver = { navController.popBackStack() },
                            onVerAnimal = { idArete -> navController.navigate(Pantalla.EditarAnimal.crearRuta(idArete)) }
                        )
                    }
                }
                
                composable(Pantalla.Perfil.ruta) {
                    PantallaPerfil(navController = navController)
                }
                
                composable(Pantalla.Configuracion.ruta) {
                    PantallaConfiguracion(navController = navController)
                }
                
                composable(
                    route = Pantalla.RegistrarAnimal.ruta, 
                    arguments = listOf(
                        navArgument("tipo") { type = NavType.StringType; nullable = true }, 
                        navArgument("idTemp") { type = NavType.StringType; nullable = true }
                    )
                ) { entrada ->
                    val regViewModel: RegistrarAnimalViewModel = viewModel { RegistrarAnimalViewModel(repoAnimales) }
                    LaunchedEffect(entrada.arguments) {
                        entrada.arguments?.getString("idTemp")?.let { regViewModel.cargarParaCompletar(it) } 
                            ?: entrada.arguments?.getString("tipo")?.let { regViewModel.tipo = it ?: "Becerro" }
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

        AnimatedVisibility(
            visible = mostrarShakeOverlay,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            ShakeOverlay(
                onRegistrarNacimiento = {
                    mostrarShakeOverlay = false
                    scope.launch {
                        repoAnimales.registrarNacimientoRapido().onSuccess {
                            Toast.makeText(context, "Nacimiento registrado instantáneamente", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onRegistrarEnfermedad = {
                    mostrarShakeOverlay = false
                    scope.launch {
                        repoSalud.registrarAlertaEnfermedadRapida().onSuccess {
                            Toast.makeText(context, "Alerta de salud registrada como pendiente", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onDismiss = { mostrarShakeOverlay = false }
            )
        }
    }
}
