package com.equipo.miranchopro.interfaz.pantallas.lotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.viewmodel.LotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLotes(
    viewModel: LotesViewModel,
    onAgregarLote: () -> Unit,
    onVerDetalle: (Int) -> Unit // Asumiendo que el ID del lote es un Int
) {
    // Observamos el estado de la base de datos en tiempo real
    val listaLotes by viewModel.lotes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Lotes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarLote,
                containerColor = Color(0xFF006DFF), // Color base de tu marca
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Nuevo Lote")
            }
        }
    ) { paddingValores ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaLotes) { lote ->
                TarjetaLote(lote = lote, onClick = { onVerDetalle(lote.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaLote(lote: Lote, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = lote.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lógica para el indicador visual de capacidad (CU-15)
            val progreso = if (lote.capacidadMaxima > 0) {
                lote.ocupacionActual.toFloat() / lote.capacidadMaxima
            } else {
                0f
            }

            Text(
                text = "${lote.ocupacionActual} / ${lote.capacidadMaxima} Cabezas",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (progreso >= 0.9f) Color(0xFFD32F2F) else Color(0xFF006DFF), // Rojo si está al 90%+
                trackColor = Color.LightGray
            )
        }
    }
}