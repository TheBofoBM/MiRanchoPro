package com.equipo.miranchopro.interfaz.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.equipo.miranchopro.interfaz.pantallas.inventario.PantallaInventario
import com.equipo.miranchopro.interfaz.pantallas.tareas.PantallaTareas

@Composable
fun NavegacionApp() {
    val controladorNavegacion = rememberNavController()

    NavHost(
        navController = controladorNavegacion,
        startDestination = Rutas.Tareas.ruta
    ) {
        composable(Rutas.Tareas.ruta) {
            PantallaTareas(controladorNavegacion)
        }
        composable(Rutas.Inventario.ruta) {
            PantallaInventario(controladorNavegacion)
        }
    }
}
