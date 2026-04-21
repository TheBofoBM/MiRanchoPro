package com.example.miranchopro.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miranchopro.ui.screens.EditAnimalScreen

sealed class Screen(val route: String) {
    object EditAnimal : Screen("edit_animal/{idArete}") {
        fun createRoute(idArete: String) = "edit_animal/$idArete"
    }
    // Agrega más pantallas aquí en el futuro
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.EditAnimal.route
    ) {
        composable(
            route = Screen.EditAnimal.route,
            arguments = listOf(navArgument("idArete") { type = NavType.StringType })
        ) { backStackEntry ->
            val idArete = backStackEntry.arguments?.getString("idArete") ?: ""
            EditAnimalScreen(
                idArete = idArete,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
