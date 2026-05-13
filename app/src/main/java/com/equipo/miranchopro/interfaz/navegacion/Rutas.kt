package com.equipo.miranchopro.interfaz.navegacion

sealed class Rutas(val ruta: String) {
    object Login : Rutas("login")
    object Registro : Rutas("registro")
    object Tareas : Rutas("tareas")
    object Inventario : Rutas("inventario")
    object Lotes : Rutas("lotes")
}
