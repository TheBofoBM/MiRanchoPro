package com.equipo.miranchopro.interfaz.pantallas.configuracion
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.equipo.miranchopro.interfaz.tema.PreferenciasApp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(navController: NavController) {
    val context=LocalContext.current;val tema by PreferenciasApp.tema.collectAsState()
    Scaffold(topBar={TopAppBar(title={Text("Configuración")},navigationIcon={IconButton(onClick={navController.popBackStack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Volver")}})}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text("Apariencia",style=MaterialTheme.typography.titleLarge)
            listOf("sistema" to "Usar tema del teléfono","claro" to "Tema claro","oscuro" to "Tema oscuro").forEach { (valor,nombre) ->
                Row { RadioButton(selected=tema==valor,onClick={PreferenciasApp.elegir(context,valor)});TextButton(onClick={PreferenciasApp.elegir(context,valor)}){Text(nombre)} }
            }
            HorizontalDivider();Text("Información",style=MaterialTheme.typography.titleLarge)
            Text("Mi Rancho PRO · Versión 1.0");Text("Idioma: Español")
            Text("Consulta tus pendientes en Tareas y el seguimiento del ganado en Salud.")
        }
    }
}
