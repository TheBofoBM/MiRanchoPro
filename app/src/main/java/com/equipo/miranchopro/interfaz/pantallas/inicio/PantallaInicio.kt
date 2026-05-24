package com.equipo.miranchopro.interfaz.pantallas.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Encabezado Estilo Lujo
        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 40.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Mi Rancho",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 52.sp
                )
                Text(
                    text = "PRO",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00BFA5),
                    letterSpacing = (-1.5).sp,
                    lineHeight = 52.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Panel de Control",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "ESTADO ACTUAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardResumenInicio(
                        titulo = "Animales",
                        valor = totalAnimales.toString(),
                        icono = Icons.Default.Pets,
                        colorIcono = Color(0xFF008577),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Pantalla.Inventario.ruta) }
                    )
                    CardResumenInicio(
                        titulo = "Tareas",
                        valor = tareasPendientes.toString(),
                        icono = Icons.Default.ListAlt,
                        colorIcono = Color(0xFFFFA000),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Pantalla.Tareas.ruta) }
                    )
                }
            }

            if (medicamentosBajoStock > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$medicamentosBajoStock medicamentos con stock bajo",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ACCESO RÁPIDO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemAccesoRapido(
                        titulo = "Inventario de Ganado",
                        subtitulo = "Ver y registrar animales",
                        icono = Icons.Default.Agriculture,
                        color = Color(0xFF4CAF50),
                        onClick = { navController.navigate(Pantalla.Inventario.ruta) }
                    )
                    ItemAccesoRapido(
                        titulo = "Salud y Medicamentos",
                        subtitulo = "Control médico y stock",
                        icono = Icons.Default.MedicalServices,
                        color = Color(0xFF2196F3),
                        onClick = { navController.navigate(Pantalla.Salud.ruta) }
                    )
                    ItemAccesoRapido(
                        titulo = "Gestión de Lotes",
                        subtitulo = "Ubicación de animales",
                        icono = Icons.Default.GridView,
                        color = Color(0xFF9C27B0),
                        onClick = { navController.navigate(Pantalla.Lotes.ruta) }
                    )
                    ItemAccesoRapido(
                        titulo = "Reportes",
                        subtitulo = "Estadísticas del rancho",
                        icono = Icons.Default.Assessment,
                        color = Color(0xFF607D8B),
                        onClick = { navController.navigate(Pantalla.Reportes.ruta) }
                    )
                }
            }
        }
    }
}

@Composable
fun CardResumenInicio(
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colorIcono.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = valor, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text(text = titulo, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ItemAccesoRapido(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitulo, color = Color.Gray, fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
