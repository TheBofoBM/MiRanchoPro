package com.equipo.miranchopro.interfaz.pantallas.mas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.interfaz.navegacion.Pantalla

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMas(nav: NavController, usuario: Usuario?) {
    Scaffold(topBar={TopAppBar(title={Text("Más opciones")})}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { Text("Mi Rancho PRO",style=MaterialTheme.typography.headlineSmall) }
            item { Text(usuario?.correo ?: "",style=MaterialTheme.typography.bodyMedium) }
            if(usuario?.rol?.equals("Administrador",true)==true) {
                item { Acceso("Reportes","Resumen de animales activos",Icons.Default.Assessment){nav.navigate(Pantalla.Reportes.ruta)} }
                item { Acceso("Equipo","Administrar empleados",Icons.Default.People){nav.navigate(Pantalla.Trabajadores.ruta)} }
                item { Acceso("Lotes","Capacidad y movimientos",Icons.Default.Fence){nav.navigate(Pantalla.Lotes.ruta)} }
            }
            item { Acceso("Insumos","Existencias y consumo",Icons.Default.Inventory2){nav.navigate(Pantalla.Insumos.ruta)} }
            item { Acceso("Mi perfil","Cuenta y contraseña",Icons.Default.Person){nav.navigate(Pantalla.Perfil.ruta)} }
            item { Acceso("Configuración","Apariencia e información",Icons.Default.Settings){nav.navigate(Pantalla.Configuracion.ruta)} }
            item { OutlinedButton(onClick={nav.navigate(Pantalla.Login.ruta){popUpTo(0){inclusive=true}}},modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")} }
        }
    }
}
@Composable
private fun Acceso(titulo: String, detalle: String, icono: ImageVector, abrir: () -> Unit) {
    ElevatedCard(onClick=abrir,modifier=Modifier.fillMaxWidth()) {
        ListItem(headlineContent={Text(titulo)},supportingContent={Text(detalle)},leadingContent={Icon(icono,null)},trailingContent={Icon(Icons.Default.ChevronRight,null)})
    }
}
