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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.equipo.miranchopro.modelovista.EditarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

private val ColorTituloSeccion = Color(0xFF00897B)
private val ColorIconoGris = Color(0xFF90A4AE)
private val ColorTextoEtiqueta = Color(0xFF9E9E9E)
private val EmeraldPrimary = Color(0xFF00897B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarAnimal(
    idArete: String,
    viewModel: EditarAnimalViewModel = viewModel(),
    alVolver: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogoOpcionesFoto by remember { mutableStateOf(false) }
    var mostrarConfirmacionCamara by remember { mutableStateOf(false) }
    var tempUriCamara by remember { mutableStateOf<Uri?>(null) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.fotoUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            mostrarConfirmacionCamara = true
        }
    }

    LaunchedEffect(idArete) {
        viewModel.cargarAnimal(idArete)
    }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is EditarAnimalViewModel.EventoUI.Exito -> {
                    snackbarHostState.showSnackbar("Animal actualizado correctamente")
                    alVolver()
                }
                is EditarAnimalViewModel.EventoUI.BajaExitosa -> {
                    snackbarHostState.showSnackbar("Animal dado de baja")
                    alVolver()
                }
                is EditarAnimalViewModel.EventoUI.Error -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    // Diálogo para elegir entre Cámara o Galería
    if (mostrarDialogoOpcionesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOpcionesFoto = false },
            title = { Text("Actualizar fotografía", fontWeight = FontWeight.Bold) },
            text = { Text("Selecciona el origen de la nueva imagen.") },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val uri = viewModel.crearUriTemporalCamara(context)
                            tempUriCamara = uri
                            cameraLauncher.launch(uri)
                            mostrarDialogoOpcionesFoto = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar Foto")
                    }
                    Button(
                        onClick = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            mostrarDialogoOpcionesFoto = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir de Galería")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoOpcionesFoto = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de confirmación después de tomar la foto
    if (mostrarConfirmacionCamara) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCamara = false },
            title = { Text("¿Aceptar fotografía?", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = tempUriCamara,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmarFotoCamara()
                        mostrarConfirmacionCamara = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val uri = viewModel.crearUriTemporalCamara(context)
                        tempUriCamara = uri
                        cameraLauncher.launch(uri)
                        mostrarConfirmacionCamara = false
                    }
                ) { Text("Tomar de nuevo") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFBFBFB),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ficha del Animal", fontWeight = FontWeight.Bold, color = Color(0xFFB0BEC5)) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFFB0BEC5))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.mostrarDialogoBaja = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Dar de baja", tint = Color.Red)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // --- SECCIÓN: FOTO ---
            CardSeccionEditar("Foto del Animal") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                        .clickable { mostrarDialogoOpcionesFoto = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.fotoUri != null || viewModel.fotoPath != null) {
                        AsyncImage(
                            model = viewModel.fotoUri ?: viewModel.fotoPath,
                            contentDescription = "Foto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.fotoUri = null; viewModel.fotoPath = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) { Icon(Icons.Default.Close, null, tint = Color.White) }
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    }
                }
            }

            // --- SECCIÓN: IDENTIFICACIÓN (LECTURA) ---
            CardSeccionEditar("Identificación") {
                InfoItemLecturaFicha("ID / Arete", viewModel.idArete, Icons.Outlined.Tag)
                InfoItemLecturaFicha("Nombre", viewModel.nombre, Icons.Outlined.Badge)
                InfoItemLecturaFicha("Tipo", viewModel.tipo, Icons.Outlined.Pets)
                InfoItemLecturaFicha("Raza", viewModel.raza, Icons.Outlined.Category)
                InfoItemLecturaFicha("Edad Calculada", viewModel.edad, Icons.Outlined.Cake)
            }

            // --- SECCIÓN: NACIMIENTO Y ORIGEN ---
            CardSeccionEditar("Nacimiento y Origen") {
                InfoItemLecturaFicha("Ubicación / Lote", viewModel.ubicacion, Icons.Outlined.LocationOn)
                InfoItemLecturaFicha("Origen", viewModel.origen, Icons.Outlined.AutoAwesome)
                InfoItemLecturaFicha("Fecha de Registro", viewModel.fechaNacimiento, Icons.Outlined.CalendarToday)
                InfoItemLecturaFicha("Hora de Nacimiento", viewModel.horaNacimiento, Icons.Outlined.AccessTime)
            }

            // --- SECCIÓN: DATOS EDITABLES ---
            CardSeccionEditar("Características Físicas") {
                CampoEditable("Peso (kg)", viewModel.peso, { viewModel.peso = it }, KeyboardType.Decimal)
                CampoEditable("Color", viewModel.color, { viewModel.color = it })
                CampoEditable("Marcas / Señas", viewModel.marcas, { viewModel.marcas = it })
                CampoEditable("Características / Notas", viewModel.caracteristica, { viewModel.caracteristica = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.actualizarAnimal(context) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
            ) {
                if (viewModel.estaCargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("ACTUALIZAR REGISTRO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (viewModel.mostrarDialogoBaja) {
        DialogoBajaAnimal(viewModel)
    }
}

@Composable
fun CardSeccionEditar(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = titulo, color = ColorTituloSeccion, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))
            contenido()
        }
    }
}

@Composable
private fun InfoItemLecturaFicha(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ColorIconoGris, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = ColorTextoEtiqueta)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun CampoEditable(label: String, valor: String, onValueChange: (String) -> Unit, k: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = k),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = ColorTituloSeccion,
                unfocusedBorderColor = Color(0xFF9E9E9E),
                cursorColor = ColorTituloSeccion
            )
        )
    }
}

@Composable
fun DialogoBajaAnimal(viewModel: com.equipo.miranchopro.modelovista.EditarAnimalViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.mostrarDialogoBaja = false },
        title = { Text("Dar de baja animal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Seleccione el motivo:")
                val motivos = listOf("Vendido", "Muerto", "Agregado accidentalmente")
                motivos.forEach { motivo ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.motivoBaja = motivo }) {
                        RadioButton(selected = (viewModel.motivoBaja == motivo), onClick = { viewModel.motivoBaja = motivo })
                        Text(motivo)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.confirmarBaja() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Confirmar")
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.mostrarDialogoBaja = false }) { Text("Volver") } }
    )
}
