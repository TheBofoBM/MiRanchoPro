package com.equipo.miranchopro.interfaz.pantallas.lotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.viewmodel.LotesViewModel

@Composable
fun PantallaLotes(
    navController: NavController,
    viewModel: LotesViewModel,
    onAgregarLote: () -> Unit,
    onVerDetalle: (Int) -> Unit
) {
    val listaLotes by viewModel.lotes.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarLote,
                containerColor = Color(0xFF008577),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp).padding(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Lote", modifier = Modifier.size(36.dp))
            }
        }
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = paddingValores.calculateBottomPadding())
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
                            text = "Lotes",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "${listaLotes.size} áreas activas",
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

            if (listaLotes.isNotEmpty()) {
                ResumenLotesRapido(listaLotes)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaLotes) { lote ->
                    TarjetaLoteLujo(
                        lote = lote,
                        onClick = { onVerDetalle(lote.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenLotesRapido(lotes: List<Lote>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val totalCabezas = lotes.sumOf { it.ocupacionActual }
        val totalCapacidad = lotes.sumOf { it.capacidadMaxima }
        
        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(12.dp)) {
                Text("OCUPACIÓN TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("$totalCabezas / $totalCapacidad", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF008577))
            }
        }
    }
}

@Composable
fun TarjetaLoteLujo(lote: Lote, onClick: () -> Unit) {
    val progreso = if (lote.capacidadMaxima > 0) lote.ocupacionActual.toFloat() / lote.capacidadMaxima else 0f
    val colorProgreso = if (progreso >= 0.9f) Color.Red else if (progreso >= 0.7f) Color(0xFFF57C00) else Color(0xFF008577)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(colorProgreso.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("🐄")
                }
                Spacer(Modifier.width(8.dp))
                Text(lote.nombre, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("${lote.ocupacionActual}/${lote.capacidadMaxima}", fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("Cabezas", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = colorProgreso,
                trackColor = Color(0xFFEEEEEE)
            )
        }
    }
}
