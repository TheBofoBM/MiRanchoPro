package com.equipo.miranchopro.interfaz.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.interfaz.pantallas.login.LoginScreen
import com.equipo.miranchopro.interfaz.pantallas.registro.RegistroScreen
import com.equipo.miranchopro.viewmodel.LoginViewModel
import com.equipo.miranchopro.viewmodel.RegistroViewModel

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Instanciamos la base de datos (Room se encarga de que solo exista una instancia)
    val database = RanchoDatabase.getDatabase(context)

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            // Creamos el ViewModel inyectándole el DAO de usuarios
            val loginViewModel: LoginViewModel = viewModel { LoginViewModel(database.usuarioDao()) }

            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    navController.navigate("lotes") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("registro")
                }
            )
        }

        composable("registro") {
            // Creamos el ViewModel inyectándole el DAO de usuarios
            val registroViewModel: RegistroViewModel = viewModel { RegistroViewModel(database.usuarioDao()) }

            RegistroScreen(
                viewModel = registroViewModel,
                onRegistroExitoso = {
                    navController.navigate("lotes") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIrALogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}