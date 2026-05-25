package com.equipo.miranchopro.interfaz.pantallas.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.modelovista.InventarioViewModel
import com.equipo.miranchopro.modelovista.TareasViewModel
import com.equipo.miranchopro.modelovista.SaludViewModel

@Composable
fun PantallaInicio(
    navController: NavController,
    inventarioViewModel: InventarioViewModel,
    saludViewModel: SaludViewModel,
    tareasViewModel: TareasViewModel = viewModel()
) {
    val totalAnimales = inventarioViewModel.listaAnimales.size
    val tareasPendientes = tareasViewModel.listaTareas.count { !it.estaHecha }
    val medicamentos by saludViewModel.listaMedicamentos.collectAsState()
    val medicamentosBajoStock = medicamentos.count { it.stock < 5 }
    var menuExpandido by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mi Rancho",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRO",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00BFA5),
                            letterSpacing = (-1).sp
                        )
                    }
                    Text(
                        text = "Panel de Control",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }

                Box {
                    IconButton(onClick = { menuExpandido = true }) {
                        Icon(Icons.Default.AccountCircle, "Menú", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = { menuExpandido = false; navController.navigate(Pantalla.Perfil.ruta) },
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = { menuExpandido = false; navController.navigate(Pantalla.Configuracion.ruta) },
                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = { 
                                menuExpandido = false
                                navController.navigate(Pantalla.Login.ruta) { popUpTo(0) }
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("ESTADO ACTUAL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 1.5.sp) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardResumenInicio("Animales", totalAnimales.toString(), Icons.Default.Pets, Color(0xFF008577), Modifier.weight(1f)) { navController.navigate(Pantalla.Inventario.ruta) }
                    CardResumenInicio("Tareas", tareasPendientes.toString(), Icons.Default.ListAlt, Color(0xFFFFA000), Modifier.weight(1f)) { navController.navigate(Pantalla.Tareas.ruta) }
                }
            }
            if (medicamentosBajoStock > 0) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red); Spacer(modifier = Modifier.width(12.dp))
                            Text("$medicamentosBajoStock medicamentos con stock bajo", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
            item { Text("ACCESO RÁPIDO", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 1.5.sp) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemAccesoRapido("Inventario de Ganado", "Ver y registrar animales", Icons.Default.Agriculture, Color(0xFF4CAF50)) { navController.navigate(Pantalla.Inventario.ruta) }
                    ItemAccesoRapido("Salud Animal", "Control médico y stock", Icons.Default.MedicalServices, Color(0xFF2196F3)) { navController.navigate(Pantalla.Salud.ruta) }
                    ItemAccesoRapido("Gestión de Lotes", "Ubicación de animales", Icons.Default.GridView, Color(0xFF9C27B0)) { navController.navigate(Pantalla.Lotes.ruta) }
                    ItemAccesoRapido("Reportes", "Estadísticas del rancho", Icons.Default.Assessment, Color(0xFF607D8B)) { navController.navigate(Pantalla.Reportes.ruta) }
                }
            }
        }
    }
}

@Composable
fun CardResumenInicio(titulo: String, valor: String, icono: ImageVector, colorIcono: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(36.dp).background(colorIcono.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text(titulo, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ItemAccesoRapido(titulo: String, subtitulo: String, icono: ImageVector, color: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(icono, null, tint = color, modifier = Modifier.size(22.dp)) }
            Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp) ; Text(subtitulo, color = Color.Gray, fontSize = 12.sp) }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}
