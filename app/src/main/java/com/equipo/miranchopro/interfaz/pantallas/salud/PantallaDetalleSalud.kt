package com.equipo.miranchopro.interfaz.pantallas.salud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.modelovista.SaludViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleSalud(
    idRegistro: Int,
    viewModel: SaludViewModel,
    onVolver: () -> Unit
) {
    val registros by viewModel.registros.collectAsState()
    val registro = registros.find { it.id == idRegistro }
    val sdf = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Tratamiento", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (registro == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Registro no encontrado", color = Color.Gray)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Cabecera (Icono y Medicamento)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (registro.tipo == "Vacuna") "💉" else "💊", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = registro.medicamento, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E8A5A))
                    Text(text = registro.tipo, fontSize = 14.sp, color = Color(0xFF2C3E50))
                }
            }

            // Tarjeta de Información General
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetalleFila("Animal (Tag)", registro.idArete)
                    Divider(color = Color(0xFFE0E0E0))
                    DetalleFila("Veterinario", registro.veterinario)
                }
            }

            // Tarjeta de Fechas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetalleFila("Fecha de aplicación", sdf.format(Date(registro.fecha)))

                    if (registro.proximaFecha != null) {
                        Divider(color = Color(0xFFE0E0E0))
                        DetalleFila("Próxima cita", sdf.format(Date(registro.proximaFecha)), Color(0xFFF57C00))
                    }
                }
            }

            // Tarjeta de Notas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notas adicionales", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(registro.notas, fontSize = 15.sp, color = Color(0xFF2C3E50))
                }
            }
        }
    }
}

@Composable
fun DetalleFila(titulo: String, valor: String, valorColor: Color = Color(0xFF2C3E50)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, fontSize = 13.sp, color = Color.Gray)
        Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valorColor)
    }
}