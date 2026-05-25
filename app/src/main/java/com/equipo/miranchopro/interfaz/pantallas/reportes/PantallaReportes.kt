package com.equipo.miranchopro.interfaz.pantallas.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.viewmodel.ReporteViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaReportes(navController: NavController, viewModel: ReporteViewModel) {
    val reporte by viewModel.reporte.collectAsState()
    val cargando by viewModel.estaCargando.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarReporte()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Reportes",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Estadísticas del rancho",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00BFA5)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(Icons.Default.AccountCircle, "Menú", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                onClick = { 
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Perfil.ruta)
                                },
                                leadingIcon = { Icon(Icons.Default.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = { 
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Configuracion.ruta)
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = { 
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Login.ruta) {
                                        popUpTo(0)
                                    }
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                            )
                        }
                    }
                }
            }

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF008577))
                }
            } else {
                reporte?.let { datos ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("RESUMEN DE INVENTARIO", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 2.sp)
                        }

                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TarjetaEstadisticaLujo("Total Animales", datos.totalAnimales.toString(), Modifier.weight(1f))
                                TarjetaEstadisticaLujo("Peso Promedio", "${String.format("%.1f", datos.pesoPromedio)} kg", Modifier.weight(1f))
                            }
                        }

                        item {
                            Text("DISTRIBUCIÓN POR RAZA", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(20.dp)) {
                                    datos.conteoPorRaza.forEach { (raza, cantidad) ->
                                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(raza, fontWeight = FontWeight.Medium)
                                            Text(cantidad.toString(), fontWeight = FontWeight.Bold, color = Color(0xFF008577))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("REGISTROS RECIENTES", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 2.sp)
                        }

                        items(datos.animalesRecientes.take(5)) { animal ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("#${animal.idArete}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text(animal.raza, color = Color.Gray, fontSize = 14.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${animal.peso} kg", color = Color(0xFF008577), fontWeight = FontWeight.Bold)
                                        val fecha = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(animal.fechaRegistro))
                                        Text(fecha, fontSize = 12.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaEstadisticaLujo(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.Start) {
            Text(titulo, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
        }
    }
}
