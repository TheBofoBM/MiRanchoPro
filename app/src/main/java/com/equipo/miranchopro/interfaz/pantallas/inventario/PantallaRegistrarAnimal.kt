package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarAnimal(
    viewModel: RegistrarAnimalViewModel = viewModel(),
    alFinalizar: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedOrigen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is RegistrarAnimalViewModel.EventoUI.Exito -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                    alFinalizar()
                }
                is RegistrarAnimalViewModel.EventoUI.Error -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (viewModel.esEdicionPendiente) "Completar Registro" else "Nuevo Registro",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = alFinalizar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Sección de Identificación
            CardFormulario("Identificación Básica") {
                CampoTexto("ID / Arete *", viewModel.idArete, { viewModel.idArete = it }, "Ej: A-102")
                CampoTexto("Nombre", viewModel.nombre, { viewModel.nombre = it }, "Ej: Lucero")
                
                Text("Tipo de Animal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = !expandedTipo }
                ) {
                    OutlinedTextField(
                        value = viewModel.tipo,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                        viewModel.tiposDisponibles.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = { viewModel.tipo = opcion; expandedTipo = false }
                            )
                        }
                    }
                }
                CampoTexto("Raza", viewModel.raza, { viewModel.raza = it }, "Ej: Serrana")
            }

            // Sección de Nacimiento y Edad
            CardFormulario("Nacimiento y Origen") {
                if (viewModel.esEdicionPendiente) {
                    InfoItem("Fecha de Nacimiento (Capturada)", viewModel.fechaNacimiento, Icons.Default.CalendarToday)
                    InfoItem("Hora de Nacimiento", viewModel.horaNacimientoRegistrada, Icons.Default.AccessTime)
                    InfoItem("Origen", "De parto (Automático)", Icons.Default.AutoAwesome)
                    InfoItem("Ubicación Sugerida", "Lote recién nacidos", Icons.Default.LocationOn)
                } else {
                    CampoTexto("Fecha de Nacimiento (dd/mm/aaaa) *", viewModel.fechaNacimiento, { viewModel.fechaNacimiento = it }, "01/01/2024")
                    
                    Text("Origen del Animal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedOrigen,
                        onExpandedChange = { expandedOrigen = !expandedOrigen }
                    ) {
                        OutlinedTextField(
                            value = viewModel.origen,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOrigen) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedOrigen, onDismissRequest = { expandedOrigen = false }) {
                            viewModel.origenesDisponibles.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = { viewModel.origen = opcion; expandedOrigen = false }
                                )
                            }
                        }
                    }
                    CampoTexto("Ubicación / Lote", viewModel.ubicacion, { viewModel.ubicacion = it }, "Ej: Lote A")
                }
            }

            // Datos Físicos
            CardFormulario("Características Físicas") {
                CampoTexto("Peso Aproximado (kg) *", viewModel.peso, { viewModel.peso = it }, "Ej: 45.0", KeyboardType.Decimal)
                CampoTexto("Color", viewModel.color, { viewModel.color = it }, "Ej: Café con manchas")
                CampoTexto("Marcas", viewModel.marcas, { viewModel.marcas = it }, "Ej: Marca en oreja derecha")
                CampoTexto("Características / Notas", viewModel.caracteristica, { viewModel.caracteristica = it }, "Ej: Muy activo", lineas = 3)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.registrarAnimal() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008577)),
                enabled = !viewModel.estaCargando
            ) {
                if (viewModel.estaCargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (viewModel.esEdicionPendiente) "COMPLETAR REGISTRO" else "GUARDAR ANIMAL", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CardFormulario(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(titulo, color = Color(0xFF008577), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            contenido()
        }
    }
}

@Composable
fun CampoTexto(label: String, valor: String, onValueChange: (String) -> Unit, p: String, k: KeyboardType = KeyboardType.Text, lineas: Int = 1) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            placeholder = { Text(p, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = k),
            maxLines = lineas,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF008577),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Composable
fun InfoItem(label: String, valor: String, icono: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth()
    ) {
        Icon(icono, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}
