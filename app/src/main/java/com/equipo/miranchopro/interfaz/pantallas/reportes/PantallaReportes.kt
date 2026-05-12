package com.equipo.miranchopro.interfaz.pantallas.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.viewmodel.ReporteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReportes(viewModel: ReporteViewModel) {
    val reporte by viewModel.reporte.collectAsState()
    val cargando by viewModel.estaCargando.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarReporte()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte de Inventario", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF008577))
            )
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF008577))
            }
        } else {
            reporte?.let { datos ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Resumen General", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Generado el: ${datos.fechaGeneracion}", fontSize = 12.sp, color = Color.Gray)
                    }

                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TarjetaEstadistica("Total Animales", datos.totalAnimales.toString(), Modifier.weight(1f))
                            TarjetaEstadistica("Peso Promedio", "${String.format("%.1f", datos.pesoPromedio)} kg", Modifier.weight(1f))
                        }
                    }

                    item {
                        Text("Distribución por Raza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                datos.conteoPorRaza.forEach { (raza, cantidad) ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(raza)
                                        Text(cantidad.toString(), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Últimos Animales Agregados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(datos.animalesRecientes.take(10)) { animal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Arete: ${animal.idArete}", fontWeight = FontWeight.Bold)
                                    Text(animal.raza, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${animal.peso} kg", color = Color(0xFF008577), fontWeight = FontWeight.Bold)
                                    val fecha = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(animal.fechaRegistro))
                                    Text(fecha, fontSize = 12.sp, color = Color.Gray)
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
fun TarjetaEstadistica(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo, fontSize = 12.sp, color = Color.Gray)
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF008577))
        }
    }
}
