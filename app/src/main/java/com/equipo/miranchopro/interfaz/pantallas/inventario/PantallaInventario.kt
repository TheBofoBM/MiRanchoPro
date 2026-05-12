package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Medicamento
import com.equipo.miranchopro.interfaz.componentes.DialogoMedicamento
import com.equipo.miranchopro.interfaz.componentes.TarjetaMedicamento
import com.equipo.miranchopro.interfaz.pantallas.tareas.BarraNavegacion
import com.equipo.miranchopro.modelovista.MedicamentoViewModel

@Composable
fun PantallaInventario(
    navController: NavController,
    viewModel: MedicamentoViewModel = viewModel()
) {
    val listaMedicamentos = viewModel.listaMedicamentos
    var mostrarDialogo by remember { mutableStateOf(false) }
    var medicamentoAEditar by remember { mutableStateOf<Medicamento?>(null) }

    if (mostrarDialogo) {
        DialogoMedicamento(
            medicamentoExistente = medicamentoAEditar,
            onDismiss = {
                mostrarDialogo = false
                medicamentoAEditar = null
            },
            onConfirm = { medicamento ->
                if (medicamentoAEditar != null) {
                    viewModel.editarMedicamento(medicamento)
                } else {
                    viewModel.agregarMedicamento(medicamento)
                }
                mostrarDialogo = false
                medicamentoAEditar = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    medicamentoAEditar = null
                    mostrarDialogo = true
                },
                containerColor = Color(0xFF00897B),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Medicamento")
            }
        },
        bottomBar = { BarraNavegacion(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Inventario de Medicamentos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${listaMedicamentos.size} productos en total",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(listaMedicamentos, key = { it.id }) { medicamento ->
                    TarjetaMedicamento(
                        medicamento = medicamento,
                        onEdit = {
                            medicamentoAEditar = medicamento
                            mostrarDialogo = true
                        },
                        onDelete = {
                            viewModel.eliminarMedicamento(medicamento)
                        }
                    )
                }
            }
        }
    }
}
