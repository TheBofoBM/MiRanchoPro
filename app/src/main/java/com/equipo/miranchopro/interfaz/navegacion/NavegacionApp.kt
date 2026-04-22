package com.equipo.miranchopro.interfaz.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaEditarAnimal
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaInventario
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaRegistrarAnimal

sealed class Pantalla(val ruta: String) {
    object Inventario : Pantalla("inventario")
    object RegistrarAnimal : Pantalla("registrar_animal")
    object EditarAnimal : Pantalla("editar_animal/{idArete}") {
        fun crearRuta(idArete: String) = "editar_animal/$idArete"
    }
}

@Composable
fun NavegacionApp() {
    val controladorNavegacion = rememberNavController()

    NavHost(
        navController = controladorNavegacion,
        startDestination = Pantalla.Inventario.ruta
    ) {
        composable(route = Pantalla.Inventario.ruta) {
            PantallaInventario(
                alSeleccionarAnimal = { idArete ->
                    controladorNavegacion.navigate(Pantalla.EditarAnimal.crearRuta(idArete))
                },
                alAgregarAnimal = {
                    controladorNavegacion.navigate(Pantalla.RegistrarAnimal.ruta)
                }
            )
        }

        composable(route = Pantalla.RegistrarAnimal.ruta) {
            PantallaRegistrarAnimal(
                alFinalizar = {
                    controladorNavegacion.popBackStack()
                }
            )
        }

        composable(
            route = Pantalla.EditarAnimal.ruta,
            arguments = listOf(navArgument("idArete") { type = NavType.StringType })
        ) { entrada ->
            val idArete = entrada.arguments?.getString("idArete") ?: ""
            PantallaEditarAnimal(
                idArete = idArete,
                alVolver = { controladorNavegacion.popBackStack() }
            )
        }
    }
}
