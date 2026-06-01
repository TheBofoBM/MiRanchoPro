package com.equipo.miranchopro.interfaz.pantallas.inventario

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

private val ForestGreen = Color(0xFF004D40)
private val EmeraldPrimary = Color(0xFF00897B)
private val ColorFondo = Color(0xFFF8F9FA)
private val ColorGrisSuave = Color(0xFFF1F4F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarAnimal(
    viewModel: RegistrarAnimalViewModel = viewModel(),
    alFinalizar: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedOrigen by remember { mutableStateOf(false) }
    var mostrarDialogoFoto by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.fotoUri = uri }

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

    if (mostrarDialogoFoto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoFoto = false },
            title = { Text("Fotografía del animal", fontWeight = FontWeight.Bold) },
            text = { Text("Selecciona una imagen de tu galería para adjuntarla al registro.") },
            confirmButton = {
                Button(
                    onClick = { 
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        mostrarDialogoFoto = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) { Text("Abrir Galería") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoFoto = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = ColorFondo,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER CURVO ESTILO PREMIUM ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 60.dp))
                    .background(Brush.verticalGradient(listOf(ForestGreen, EmeraldPrimary)))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    IconButton(
                        onClick = alFinalizar,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                    Text(
                        text = if (viewModel.esEdicionPendiente) "Completar" else "Registro",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = if (viewModel.esEdicionPendiente) "Nacimiento detectado" else "Nuevo animal en inventario",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-30).dp)
            ) {
                // --- SECCIÓN: FOTO ---
                CardRegistroModerno("Identificación Visual") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(ColorGrisSuave)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                            .clickable { mostrarDialogoFoto = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.fotoUri != null || viewModel.fotoPath != null) {
                            AsyncImage(
                                model = viewModel.fotoUri ?: viewModel.fotoPath,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.fotoUri = null; viewModel.fotoPath = null },
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            ) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = EmeraldPrimary, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Agregar Foto", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- SECCIÓN 1: DATOS BÁSICOS ---
                CardRegistroModerno("Información Principal") {
                    CampoPremium("ID / Arete *", viewModel.idArete, { viewModel.idArete = it }, "Ej: A-102")
                    CampoPremium("Nombre", viewModel.nombre, { viewModel.nombre = it }, "Ej: Lucero")
                    
                    Text("Tipo de Animal", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedTipo,
                        onExpandedChange = { if (!viewModel.esEdicionPendiente) expandedTipo = !expandedTipo }
                    ) {
                        OutlinedTextField(
                            value = viewModel.tipo, onValueChange = {}, readOnly = true,
                            trailingIcon = { if(!viewModel.esEdicionPendiente) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ColorGrisSuave,
                                unfocusedContainerColor = ColorGrisSuave,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            enabled = !viewModel.esEdicionPendiente
                        )
                        ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                            viewModel.tiposDisponibles.forEach { opcion ->
                                DropdownMenuItem(text = { Text(opcion) }, onClick = { viewModel.tipo = opcion; expandedTipo = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoPremium("Raza", viewModel.raza, { viewModel.raza = it }, "Serrana")
                }

                // --- SECCIÓN 2: NACIMIENTO ---
                CardRegistroModerno("Origen y Nacimiento") {
                    if (viewModel.esEdicionPendiente) {
                        ItemLecturaElegante("Fecha Capturada", viewModel.fechaNacimiento, Icons.Outlined.CalendarMonth)
                        ItemLecturaElegante("Hora de Nacimiento", viewModel.horaNacimientoRegistrada, Icons.Outlined.AccessTime)
                        ItemLecturaElegante("Lote Sugerido", "Lote recién nacidos", Icons.Outlined.LocationOn)
                    } else {
                        CampoPremium("Fecha de Nacimiento *", viewModel.fechaNacimiento, { viewModel.fechaNacimiento = it }, "dd/mm/aaaa")
                        CampoPremium("Ubicación / Lote", viewModel.ubicacion, { viewModel.ubicacion = it }, "Ej: Lote A")
                    }
                }

                // --- SECCIÓN 3: CARACTERÍSTICAS ---
                CardRegistroModerno("Físico y Notas") {
                    CampoPremium("Peso Aproximado (kg) *", viewModel.peso, { viewModel.peso = it }, "Ej: 45.0", KeyboardType.Decimal)
                    CampoPremium("Color", viewModel.color, { viewModel.color = it }, "Ej: Café con manchas")
                    CampoPremium("Marcas", viewModel.marcas, { viewModel.marcas = it }, "Señas particulares")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.registrarAnimal(context) },
                    modifier = Modifier.fillMaxWidth().height(60.dp).shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    enabled = !viewModel.estaCargando
                ) {
                    if (viewModel.estaCargando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("GUARDAR REGISTRO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                    }
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun CardRegistroModerno(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = titulo.uppercase(), color = EmeraldPrimary, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(20.dp))
            contenido()
        }
    }
}

@Composable
fun ItemLecturaElegante(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(EmeraldPrimary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
        }
    }
}

@Composable
fun CampoPremium(label: String, valor: String, onValueChange: (String) -> Unit, placeholder: String, k: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238), modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = valor, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = k),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorGrisSuave,
                unfocusedContainerColor = ColorGrisSuave,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color(0xFF263238),
                unfocusedTextColor = Color(0xFF263238)
            )
        )
    }
}
