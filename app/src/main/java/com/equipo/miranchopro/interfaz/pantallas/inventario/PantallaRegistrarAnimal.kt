package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

private val ColorTituloSeccion = Color(0xFF00897B)
private val ColorFondoPantalla = Color(0xFFFBFBFB)
private val ColorBotonRegistro = Color(0xFF00796B)
private val ColorIconoGris = Color(0xFF90A4AE)
private val ColorTextoEtiqueta = Color(0xFF9E9E9E)

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
        containerColor = ColorFondoPantalla,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (viewModel.esEdicionPendiente) "Nacimiento" else "Nuevo Registro",
                        color = Color(0xFFB0BEC5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = alFinalizar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFFB0BEC5))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // --- SECCIÓN 1: IDENTIFICACIÓN BÁSICA ---
            CardSeccionRegistro("Identificación Básica") {
                CampoFormulario("ID / Arete *", viewModel.idArete, { viewModel.idArete = it }, "Ej: A-102")
                CampoFormulario("Nombre", viewModel.nombre, { viewModel.nombre = it }, "Ej: Lucero")
                
                Text("Tipo de Animal", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { if (!viewModel.esEdicionPendiente) expandedTipo = !expandedTipo }
                ) {
                    OutlinedTextField(
                        value = viewModel.tipo,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { if (!viewModel.esEdicionPendiente) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledTextColor = Color.Gray,
                            focusedBorderColor = ColorTituloSeccion
                        ),
                        enabled = !viewModel.esEdicionPendiente
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
                Spacer(modifier = Modifier.height(16.dp))
                CampoFormulario("Raza", viewModel.raza, { viewModel.raza = it }, "Serrana")
            }

            // --- SECCIÓN 2: NACIMIENTO Y ORIGEN (ESTILO IMAGEN) ---
            CardSeccionRegistro("Nacimiento y Origen") {
                if (viewModel.esEdicionPendiente) {
                    // Vista de lectura automática según imagen
                    InfoItemLectura(
                        label = "Fecha de Nacimiento (Capturada)",
                        value = viewModel.fechaNacimiento,
                        icon = Icons.Outlined.CalendarMonth
                    )
                    InfoItemLectura(
                        label = "Hora de Nacimiento",
                        value = viewModel.horaNacimientoRegistrada,
                        icon = Icons.Outlined.AccessTime
                    )
                    InfoItemLectura(
                        label = "Origen",
                        value = "De parto (Automático)",
                        icon = Icons.Outlined.AutoAwesome
                    )
                    InfoItemLectura(
                        label = "Ubicación Sugerida",
                        value = "Lote recién nacidos",
                        icon = Icons.Outlined.LocationOn
                    )
                } else {
                    // Vista de edición manual para registros normales
                    CampoFormulario("Fecha de Nacimiento (dd/mm/aaaa) *", viewModel.fechaNacimiento, { viewModel.fechaNacimiento = it }, "01/01/2024")
                    CampoFormulario("Ubicación / Lote", viewModel.ubicacion, { viewModel.ubicacion = it }, "Ej: Lote A")
                }
            }

            // --- SECCIÓN 3: CARACTERÍSTICAS FÍSICAS ---
            CardSeccionRegistro("Características Físicas") {
                CampoFormulario("Peso Aproximado (kg) *", viewModel.peso, { viewModel.peso = it }, "Ej: 45.0", KeyboardType.Decimal)
                CampoFormulario("Color", viewModel.color, { viewModel.color = it }, "Ej: Café con manchas")
                CampoFormulario("Marcas", viewModel.marcas, { viewModel.marcas = it }, "Ej: Marca en oreja derecha")
                CampoFormulario("Características / Notas", viewModel.caracteristica, { viewModel.caracteristica = it }, "Ej: Muy activo")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTÓN PRINCIPAL ---
            Button(
                onClick = { viewModel.registrarAnimal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBotonRegistro),
                enabled = !viewModel.estaCargando
            ) {
                if (viewModel.estaCargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (viewModel.esEdicionPendiente) "GUARDAR REGISTRO" else "GUARDAR ANIMAL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun InfoItemLectura(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorIconoGris,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = ColorTextoEtiqueta
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun CardSeccionRegistro(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = titulo,
                color = ColorTituloSeccion,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            contenido()
        }
    }
}

@Composable
fun CampoFormulario(label: String, valor: String, onValueChange: (String) -> Unit, placeholder: String, k: KeyboardType = KeyboardType.Text, enabled: Boolean = true) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFCFD8DC)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = k),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTituloSeccion,
                unfocusedBorderColor = Color(0xFFECEFF1),
                disabledContainerColor = Color.White,
                disabledBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}
