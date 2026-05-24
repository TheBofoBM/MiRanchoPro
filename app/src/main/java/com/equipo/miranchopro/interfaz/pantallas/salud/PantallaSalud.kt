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
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.modelovista.SaludViewModel

enum class VistaSalud {
    CATEGORIAS,
    LISTA_MEDICAMENTOS,
    LISTA_VACUNACION,
    LISTA_ENFERMEDADES,
    LISTA_PENDIENTES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSalud(
    viewModel: SaludViewModel
) {
    var vistaActual by remember { mutableStateOf(VistaSalud.CATEGORIAS) }
    
    var mostrarDialogoMedicamento by remember { mutableStateOf(false) }
    var medicamentoAEditar by remember { mutableStateOf<Medicamento?>(null) }
    var mostrarDialogoVacunacion by remember { mutableStateOf(false) }
    var mostrarDialogoEnfermedad by remember { mutableStateOf(false) }
    var enfermedadACompletar by remember { mutableStateOf<Enfermedad?>(null) }

    val medicamentos by viewModel.listaMedicamentos.collectAsState()
    val vacunaciones by viewModel.listaVacunaciones.collectAsState()
    val enfermedades by viewModel.listaEnfermedades.collectAsState()
    
    val pendientes = enfermedades.filter { it.estado == "Pendiente" }
    val activas = enfermedades.filter { it.estado == "Activo" || it.estado == "Recuperado" }

    // Diálogo para completar alerta pendiente (reutilizamos DialogoEnfermedad con datos)
    if (enfermedadACompletar != null) {
        DialogoEnfermedad(
            onDismiss = { enfermedadACompletar = null },
            onConfirm = { datosNuevos ->
                viewModel.actualizarEnfermedad(datosNuevos.copy(id = enfermedadACompletar!!.id, estado = "Activo"))
                enfermedadACompletar = null
            }
        )
    }

    if (mostrarDialogoMedicamento) {
        DialogoMedicamento(
            medicamentoExistente = medicamentoAEditar,
            onDismiss = { mostrarDialogoMedicamento = false; medicamentoAEditar = null },
            onConfirm = { viewModel.agregarMedicamento(it); mostrarDialogoMedicamento = false }
        )
    }

    if (mostrarDialogoVacunacion) {
        DialogoVacunacion(
            onDismiss = { mostrarDialogoVacunacion = false },
            onConfirm = { viewModel.registrarVacunacion(it); mostrarDialogoVacunacion = false }
        )
    }

    if (mostrarDialogoEnfermedad) {
        DialogoEnfermedad(
            onDismiss = { mostrarDialogoEnfermedad = false },
            onConfirm = { viewModel.registrarEnfermedad(it); mostrarDialogoEnfermedad = false }
        )
    }

    BackHandler(enabled = vistaActual != VistaSalud.CATEGORIAS) {
        vistaActual = VistaSalud.CATEGORIAS
    }

    Scaffold(
        floatingActionButton = {
            if (vistaActual != VistaSalud.CATEGORIAS && vistaActual != VistaSalud.LISTA_PENDIENTES) {
                FloatingActionButton(
                    onClick = {
                        when(vistaActual) {
                            VistaSalud.LISTA_MEDICAMENTOS -> { medicamentoAEditar = null; mostrarDialogoMedicamento = true }
                            VistaSalud.LISTA_VACUNACION -> mostrarDialogoVacunacion = true
                            VistaSalud.LISTA_ENFERMEDADES -> mostrarDialogoEnfermedad = true
                            else -> {}
                        }
                    },
                    containerColor = Color(0xFF008577),
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, null) }
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
                Column(modifier = Modifier.statusBarsPadding().padding(24.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (vistaActual != VistaSalud.CATEGORIAS) {
                            IconButton(onClick = { vistaActual = VistaSalud.CATEGORIAS }) {
                                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                            }
                        }
                        Text(
                            text = when(vistaActual) {
                                VistaSalud.CATEGORIAS -> "Salud Animal"
                                VistaSalud.LISTA_MEDICAMENTOS -> "Medicinas"
                                VistaSalud.LISTA_VACUNACION -> "Vacunas"
                                VistaSalud.LISTA_ENFERMEDADES -> "Historial"
                                VistaSalud.LISTA_PENDIENTES -> "Pendientes"
                            },
                            fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White
                        )
                    }
                    Text(
                        text = if (pendientes.isNotEmpty()) "${pendientes.size} alertas por completar" else "Estado médico del rancho",
                        color = if (pendientes.isNotEmpty()) Color(0xFFE53935) else Color(0xFF00BFA5),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                when (vistaActual) {
                    VistaSalud.CATEGORIAS -> {
                        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (pendientes.isNotEmpty()) {
                                item {
                                    Text("ALERTAS POR COMPLETAR", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red, letterSpacing = 2.sp)
                                }
                                item {
                                    CardCategoriaLujo(
                                        titulo = "Casos del Sensor",
                                        subtitulo = "${pendientes.size} reportes sin detalle",
                                        colorFondo = Color(0xFFFFEBEE),
                                        icono = Icons.Default.NewReleases,
                                        colorIcono = Color.Red,
                                        onClick = { vistaActual = VistaSalud.LISTA_PENDIENTES }
                                    )
                                }
                            }
                            item {
                                Text("SECCIONES MÉDICAS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 2.sp)
                            }
                            item {
                                CardCategoriaLujo(
                                    titulo = "Medicamentos",
                                    subtitulo = "Stock: ${medicamentos.sumOf { it.stock }} unidades",
                                    icono = Icons.Default.MedicalServices,
                                    colorIcono = Color(0xFF2196F3),
                                    onClick = { vistaActual = VistaSalud.LISTA_MEDICAMENTOS }
                                )
                            }
                            item {
                                CardCategoriaLujo(
                                    titulo = "Vacunación",
                                    subtitulo = "${vacunaciones.size} dosis aplicadas",
                                    icono = Icons.Default.Vaccines,
                                    colorIcono = Color(0xFF4CAF50),
                                    onClick = { vistaActual = VistaSalud.LISTA_VACUNACION }
                                )
                            }
                            item {
                                CardCategoriaLujo(
                                    titulo = "Enfermedades",
                                    subtitulo = "${activas.count { it.estado == "Activo" }} casos activos",
                                    icono = Icons.Default.Sick,
                                    colorIcono = Color(0xFFE53935),
                                    onClick = { vistaActual = VistaSalud.LISTA_ENFERMEDADES }
                                )
                            }
                        }
                    }
                    VistaSalud.LISTA_PENDIENTES -> {
                        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(pendientes) { alerta ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { enfermedadACompletar = alerta },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PendingActions, null, tint = Color.Red)
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text("Alerta del Sensor", fontWeight = FontWeight.Bold)
                                            Text("Detectada: ${alerta.fechaDeteccion}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text("Completar", color = Color(0xFF008577), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    VistaSalud.LISTA_MEDICAMENTOS -> SeccionListaMedicamentos(medicamentos, viewModel) { medicamentoAEditar = it; mostrarDialogoMedicamento = true }
                    VistaSalud.LISTA_VACUNACION -> SeccionListaVacunacion(vacunaciones)
                    VistaSalud.LISTA_ENFERMEDADES -> SeccionListaEnfermedades(activas) { enf, estado -> viewModel.actualizarEstadoEnfermedad(enf, estado) }
                }
            }
        }
    }
}

@Composable
fun SeccionListaMedicamentos(lista: List<Medicamento>, viewModel: SaludViewModel, alEditar: (Medicamento) -> Unit) {
    if (lista.isEmpty()) EmptyStateSalud("No hay medicamentos") else {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(lista) { medicamento -> TarjetaMedicamento(medicamento = medicamento, onEdit = { alEditar(medicamento) }, onDelete = { viewModel.eliminarMedicamento(medicamento) }) }
        }
    }
}

@Composable
fun SeccionListaVacunacion(lista: List<Vacunacion>) {
    if (lista.isEmpty()) EmptyStateSalud("No hay vacunas") else {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(lista) { vacuna ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFE8F5E9), shape = CircleShape, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Vaccines, null, tint = Color(0xFF4CAF50), modifier = Modifier.padding(12.dp)) }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) { Text(vacuna.nombreVacuna, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Animal: #${vacuna.idAnimal}", color = Color.Gray) }
                        Column(horizontalAlignment = Alignment.End) { Text(vacuna.fechaAplicacion, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50)); Text("Dosis: ${vacuna.dosis}", fontSize = 12.sp, color = Color.Gray) }
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionListaEnfermedades(lista: List<Enfermedad>, alCambiarEstado: (Enfermedad, String) -> Unit) {
    if (lista.isEmpty()) EmptyStateSalud("No hay reportes") else {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(lista) { enfermedad ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = if(enfermedad.estado == "Activo") Color(0xFFFFEBEE) else Color(0xFFE8F5E9), shape = CircleShape, modifier = Modifier.size(40.dp)) { Icon(if(enfermedad.estado == "Activo") Icons.Default.Warning else Icons.Default.CheckCircle, null, tint = if(enfermedad.estado == "Activo") Color.Red else Color(0xFF4CAF50), modifier = Modifier.padding(10.dp)) }
                            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(enfermedad.nombre, fontWeight = FontWeight.Black, fontSize = 20.sp); Text("Animal: #${enfermedad.idAnimal}", color = Color.Gray) }
                            Badge(containerColor = if(enfermedad.estado == "Activo") Color.Red else Color(0xFF4CAF50)) { Text(enfermedad.estado.uppercase(), color = Color.White, modifier = Modifier.padding(horizontal = 8.dp)) }
                        }
                        Spacer(Modifier.height(16.dp)); Text("Síntomas: ${enfermedad.sintomas}", fontSize = 14.sp); Text("Tratamiento: ${enfermedad.tratamiento}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF008577))
                        if (enfermedad.estado == "Activo") { Button(onClick = { alCambiarEstado(enfermedad, "Recuperado") }, modifier = Modifier.padding(top = 16.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(12.dp)) { Text("Marcar como Recuperado") } }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateSalud(mensaje: String) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(mensaje, color = Color.Gray) } }

@Composable
fun CardCategoriaLujo(
    titulo: String, subtitulo: String, colorFondo: Color = Color.White,
    icono: androidx.compose.ui.graphics.vector.ImageVector, colorIcono: Color = Color(0xFF008577), onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = colorFondo), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).background(colorIcono.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icono, null, tint = colorIcono, modifier = Modifier.size(28.dp)) }
            Spacer(modifier = Modifier.width(20.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black); Text(text = subtitulo, color = Color.Gray, fontSize = 14.sp) }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
