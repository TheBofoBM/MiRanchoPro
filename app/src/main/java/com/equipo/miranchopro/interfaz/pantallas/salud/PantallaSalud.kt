package com.equipo.miranchopro.interfaz.pantallas.salud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.data.model.RegistroSalud
import com.equipo.miranchopro.modelovista.SaludViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSalud(
    viewModel: SaludViewModel,
    onNuevoRegistro: () -> Unit,
    onVerDetalleTratamiento: (Int) -> Unit
) {
    val registros by viewModel.registros.collectAsState()
    var textoBusqueda by remember { mutableStateOf("") }

    // Filtrado en tiempo real por Arete o Medicamento
    val registrosFiltrados = registros.filter {
        it.idArete.contains(textoBusqueda, ignoreCase = true) ||
                it.medicamento.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Historial Médico", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoRegistro,
                containerColor = Color(0xFF0E8A5A),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir registro médico")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- HEADER ESTILO WIREFRAME ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CardEstadistica(
                    titulo = "${registros.size}",
                    subtitulo = "Registros totales",
                    modifier = Modifier.weight(1f)
                )
                CardEstadistica(
                    titulo = "${registros.count { it.proximaFecha != null }}",
                    subtitulo = "Programados",
                    modifier = Modifier.weight(1f)
                )
            }

            // --- BARRA DE BÚSQUEDA ---
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por tag, tratamiento...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF0E8A5A)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- LISTA DE REGISTROS ---
            if (registrosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No se encontraron registros.", color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(registrosFiltrados) { registro ->
                        TarjetaMedicaWireframe(
                            registro = registro,
                            onClick = { onVerDetalleTratamiento(registro.id) } // <--- CAMBIO AQUÍ (pasamos el ID)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardEstadistica(titulo: String, subtitulo: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = titulo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
            Text(text = subtitulo, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
