package com.equipo.miranchopro.interfaz.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.repository.AnimalRepository
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
}

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Instanciamos la base de datos y el repositorio real
    val database = RanchoDatabase.getDatabase(context)
    val repoAnimales = AnimalRepository(database.animalDao())

    NavHost(
        navController = navController,
        startDestination = Pantalla.Login.ruta
    ) {
        // --- SECCIÓN DE EDWIN (AUTH) ---
        composable(Pantalla.Login.ruta) {
            val loginViewModel: LoginViewModel = viewModel { LoginViewModel(database.usuarioDao()) }
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    navController.navigate(Pantalla.Inventario.ruta) {
                        popUpTo(Pantalla.Login.ruta) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Pantalla.Registro.ruta)
                }
            )
        }

        composable(Pantalla.Registro.ruta) {
            val registroViewModel: RegistroViewModel = viewModel { RegistroViewModel(database.usuarioDao()) }
            RegistroScreen(
                viewModel = registroViewModel,
                onRegistroExitoso = {
                    navController.navigate(Pantalla.Inventario.ruta) {
                        popUpTo(Pantalla.Login.ruta) { inclusive = true }
                    }
                },
                onIrALogin = {
                    navController.popBackStack()
                }
            )
        }

        // --- SECCIÓN DE ADOLFO (INVENTARIO REAL) ---
        composable(Pantalla.Inventario.ruta) {
            // Inyectamos el repositorio en el ViewModel
            val invViewModel: InventarioViewModel = viewModel { InventarioViewModel(repoAnimales) }
            PantallaInventario(
                viewModel = invViewModel,
                alSeleccionarAnimal = { idArete ->
                    navController.navigate(Pantalla.EditarAnimal.crearRuta(idArete))
                },
                alAgregarAnimal = {
                    navController.navigate(Pantalla.RegistrarAnimal.ruta)
                }
            )
        }

        composable(Pantalla.RegistrarAnimal.ruta) {
            val regViewModel: RegistrarAnimalViewModel = viewModel { RegistrarAnimalViewModel(repoAnimales) }
            PantallaRegistrarAnimal(
                viewModel = regViewModel,
                alFinalizar = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Pantalla.EditarAnimal.ruta,
            arguments = listOf(navArgument("idArete") { type = NavType.StringType })
        ) { entrada ->
            val idArete = entrada.arguments?.getString("idArete") ?: ""
            val editViewModel: EditarAnimalViewModel = viewModel { EditarAnimalViewModel(repoAnimales) }
            PantallaEditarAnimal(
                idArete = idArete,
                viewModel = editViewModel,
                alVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}