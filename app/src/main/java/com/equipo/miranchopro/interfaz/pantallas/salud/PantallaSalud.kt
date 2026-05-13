package com.equipo.miranchopro.interfaz.pantallas.salud

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.data.model.Medicamento
import com.equipo.miranchopro.interfaz.componentes.DialogoMedicamento
import com.equipo.miranchopro.interfaz.componentes.TarjetaMedicamento
import com.equipo.miranchopro.modelovista.MedicamentoViewModel

enum class VistaSalud {
    CATEGORIAS,
    LISTA_MEDICAMENTOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSalud(
    viewModel: MedicamentoViewModel = viewModel()
) {
    var vistaActual by remember { mutableStateOf(VistaSalud.CATEGORIAS) }
    var mostrarDialogoMedicamento by remember { mutableStateOf(false) }
    var medicamentoAEditar by remember { mutableStateOf<Medicamento?>(null) }

    if (mostrarDialogoMedicamento) {
        DialogoMedicamento(
            medicamentoExistente = medicamentoAEditar,
            onDismiss = {
                mostrarDialogoMedicamento = false
                medicamentoAEditar = null
            },
            onConfirm = { medicamento ->
                if (medicamentoAEditar != null) {
                    viewModel.editarMedicamento(medicamento)
                } else {
                    viewModel.agregarMedicamento(medicamento)
                }
                mostrarDialogoMedicamento = false
                medicamentoAEditar = null
            }
        )
    }

    // Regresar a categorías con el botón físico de atrás
    BackHandler(enabled = vistaActual != VistaSalud.CATEGORIAS) {
        vistaActual = VistaSalud.CATEGORIAS
    }

    Scaffold(
        floatingActionButton = {
            if (vistaActual == VistaSalud.LISTA_MEDICAMENTOS) {
                FloatingActionButton(
                    onClick = {
                        medicamentoAEditar = null
                        mostrarDialogoMedicamento = true
                    },
                    containerColor = Color(0xFF008577),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp).padding(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(36.dp))
                }
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = relleno.calculateBottomPadding())
        ) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (vistaActual != VistaSalud.CATEGORIAS) {
                            IconButton(
                                onClick = { vistaActual = VistaSalud.CATEGORIAS },
                                modifier = Modifier.padding(end = 8.dp).size(48.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }
                        Text(
                            text = if (vistaActual == VistaSalud.CATEGORIAS) "Salud Animal" else "Medicinas",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1.5).sp,
                            lineHeight = 52.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (vistaActual == VistaSalud.CATEGORIAS) "Control médico del rancho" 
                               else "${viewModel.listaMedicamentos.size} productos en inventario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00BFA5)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                when (vistaActual) {
                    VistaSalud.CATEGORIAS -> SeccionCategoriasSalud(viewModel) {
                        vistaActual = VistaSalud.LISTA_MEDICAMENTOS
                    }
                    VistaSalud.LISTA_MEDICAMENTOS -> SeccionListaMedicamentos(viewModel) { med ->
                        medicamentoAEditar = med
                        mostrarDialogoMedicamento = true
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionCategoriasSalud(viewModel: MedicamentoViewModel, alVerMedicamentos: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "GESTIÓN MÉDICA",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.LightGray,
                letterSpacing = 2.sp
            )
        }
        
        item {
            val stockTotal = viewModel.listaMedicamentos.sumOf { it.stock }
            CardCategoriaLujo(
                titulo = "Medicamentos",
                subtitulo = "Stock: $stockTotal unidades",
                icono = Icons.Default.MedicalServices,
                colorIcono = Color(0xFF2196F3),
                onClick = alVerMedicamentos
            )
        }

        item {
            CardCategoriaLujo(
                titulo = "Vacunación",
                subtitulo = "Seguimiento de dosis",
                icono = Icons.Default.Vaccines,
                colorIcono = Color(0xFF4CAF50),
                onClick = { /* Próximamente */ }
            )
        }

        item {
            CardCategoriaLujo(
                titulo = "Enfermedades",
                subtitulo = "Reportes y control",
                icono = Icons.Default.Sick,
                colorIcono = Color(0xFFE53935),
                onClick = { /* Próximamente */ }
            )
        }
    }
}

@Composable
fun SeccionListaMedicamentos(viewModel: MedicamentoViewModel, alEditar: (Medicamento) -> Unit) {
    if (viewModel.listaMedicamentos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay medicamentos registrados", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(viewModel.listaMedicamentos) { medicamento ->
                TarjetaMedicamento(
                    medicamento = medicamento,
                    onEdit = { alEditar(medicamento) },
                    onDelete = { viewModel.eliminarMedicamento(medicamento) }
                )
            }
        }
    }
}

@Composable
fun CardCategoriaLujo(
    titulo: String, 
    subtitulo: String, 
    colorFondo: Color = Color.White,
    icono: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Folder,
    colorIcono: Color = Color(0xFF008577),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colorIcono.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Text(text = subtitulo, color = Color.Gray, fontSize = 14.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
