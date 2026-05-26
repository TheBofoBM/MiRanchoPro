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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(bottom = relleno.calculateBottomPadding())
        ) {
            // --- ENCABEZADO ESTILO PERSONALIZADO (IMAGEN) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    IconButton(
                        onClick = alFinalizar,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(
                        text = if (viewModel.esEdicionPendiente) "Nacimiento" else "Nuevo Registro",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Formulario de registro",
                        fontSize = 14.sp,
                        color = Color(0xFF00BFA5),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                
                // --- CARD DE HORA CAPTURADA ---
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF008577), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Hora capturada", fontSize = 12.sp, color = Color(0xFF00796B))
                            Text(viewModel.horaNacimientoRegistrada, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                // --- FORMULARIO ---
                Text("Tag / Identificador (Arete)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                CampoEntrada(viewModel.idArete, { viewModel.idArete = it }, "Ej: A-102")

                Text("Nombre (Opcional)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                CampoEntrada(viewModel.nombre, { viewModel.nombre = it }, "Ej: Lucero")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Raza", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        CampoEntrada(viewModel.raza, { viewModel.raza = it }, "Serrana")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Peso (kg)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        CampoEntrada(viewModel.peso, { viewModel.peso = it }, "Ej: 35.5", KeyboardType.Decimal)
                    }
                }

                if (!viewModel.esEdicionPendiente) {
                    Text("Tipo de Animal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    SelectorTipo(viewModel)

                    Text("Fecha de Nacimiento", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    CampoEntrada(viewModel.fechaNacimiento, { viewModel.fechaNacimiento = it }, "dd/mm/aaaa")
                    
                    Text("Origen", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    SelectorOrigen(viewModel)

                    Text("Ubicación / Lote", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    CampoEntrada(viewModel.ubicacion, { viewModel.ubicacion = it }, "Ej: Lote A")
                }

                Text("Color", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                CampoEntrada(viewModel.color, { viewModel.color = it }, "Ej: Café")

                Text("Marcas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                CampoEntrada(viewModel.marcas, { viewModel.marcas = it }, "Ej: Marca oreja")

                Text("Características", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                CampoEntrada(viewModel.caracteristica, { viewModel.caracteristica = it }, "Ej: Notas físicas")

                Spacer(modifier = Modifier.height(32.dp))

                // --- BOTÓN PRINCIPAL (ESTILO IMAGEN) ---
                Button(
                    onClick = { viewModel.registrarAnimal() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008577))
                ) {
                    Text("Guardar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CampoEntrada(valor: String, onValueChange: (String) -> Unit, placeholder: String, k: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray) },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = k),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF008577),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorTipo(viewModel: RegistrarAnimalViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = viewModel.tipo,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            viewModel.tiposDisponibles.forEach { opcion ->
                DropdownMenuItem(text = { Text(opcion) }, onClick = { viewModel.tipo = opcion; expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorOrigen(viewModel: RegistrarAnimalViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = viewModel.origen,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            viewModel.origenesDisponibles.forEach { opcion ->
                DropdownMenuItem(text = { Text(opcion) }, onClick = { viewModel.origen = opcion; expanded = false })
            }
        }
    }
}
