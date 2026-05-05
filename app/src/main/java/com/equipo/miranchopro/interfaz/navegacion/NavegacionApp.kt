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
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaEditarAnimal
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaInventario
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaRegistrarAnimal
import com.equipo.miranchopro.interfaz.pantallas.login.LoginScreen
import com.equipo.miranchopro.interfaz.pantallas.registro.RegistroScreen
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
    val database = RanchoDatabase.getDatabase(context)

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
                onRegisterClick = { navController.navigate(Pantalla.Registro.ruta) }
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
                onIrALogin = { navController.popBackStack() }
            )
        }

        // --- SECCIÓN DE ADOLFO (INVENTARIO) ---
        composable(Pantalla.Inventario.ruta) {
            PantallaInventario(
                alSeleccionarAnimal = { idArete ->
                    navController.navigate(Pantalla.EditarAnimal.crearRuta(idArete))
                },
                alAgregarAnimal = {
                    navController.navigate(Pantalla.RegistrarAnimal.ruta)
                }
            )
        }

        composable(Pantalla.RegistrarAnimal.ruta) {
            PantallaRegistrarAnimal(
                alFinalizar = { navController.popBackStack() }
            )
        }

        composable(
            route = Pantalla.EditarAnimal.ruta,
            arguments = listOf(navArgument("idArete") { type = NavType.StringType })
        ) { entrada ->
            val idArete = entrada.arguments?.getString("idArete") ?: ""
            PantallaEditarAnimal(
                idArete = idArete,
                alVolver = { navController.popBackStack() }
            )
        }
    }
}