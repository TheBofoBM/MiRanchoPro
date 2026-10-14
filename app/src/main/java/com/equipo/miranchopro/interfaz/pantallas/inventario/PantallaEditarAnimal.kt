package com.equipo.miranchopro.interfaz.pantallas.inventario

import com.equipo.miranchopro.interfaz.componentes.*
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
private val ColorIconoGris: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val ColorTextoEtiqueta: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
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
                is EditarAnimalViewModel.EventoUI.EmbarazoRegistrado -> {
                    snackbarHostState.showSnackbar("Gestación registrada con éxito")
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
                        tempUriCamara?.let { viewModel.confirmarFotoCamara(it) }
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

    // Diálogo para registrar gestación
    if (viewModel.mostrarDialogoRegistroEmbarazo) {
        AlertDialog(
            onDismissRequest = { viewModel.mostrarDialogoRegistroEmbarazo = false },
            title = { Text("Registrar Gestación", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa la fecha de monta o inseminación. La estimación suma 283 días y puede variar.")
                    OutlinedTextField(
                        value = viewModel.fechaInseminacionSeleccionada,
                        onValueChange = { viewModel.fechaInseminacionSeleccionada = it },
                        label = { Text("Fecha (dd/mm/aaaa)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    MensajeFormulario(viewModel.mensajeError)
                    Text("Tipo de cruce:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = viewModel.tipoMating == "Monta Natural", onClick = { viewModel.tipoMating = "Monta Natural" })
                        Text("Monta Natural")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = viewModel.tipoMating == "Inseminación", onClick = { viewModel.tipoMating = "Inseminación" })
                        Text("Inseminación Artificial")
                    }
                }
            },
            confirmButton = {
                Button(enabled = !viewModel.estaCargando, onClick = { viewModel.registrarGestacion() }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)) {
                    Text("Calcular y Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.mostrarDialogoRegistroEmbarazo = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFBFBFB),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ficha del Animal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.mostrarDialogoBaja = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Dar de baja", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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

            MensajeFormulario(viewModel.mensajeError)
            // --- SECCIÓN: FOTO ---
            CardSeccionEditar("Foto del Animal") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
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
                        ) { Icon(Icons.Default.Close, "Quitar fotografía", tint = Color.White) }
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    }
                }
            }

            // --- SECCIÓN: GESTACIÓN ---
            if (viewModel.tipo in listOf("Vaca", "Vaquilla", "Becerra")) {
                CardSeccionEditar("Seguimiento de Gestación") {
                    if (viewModel.embarazoActivo != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = ColorTituloSeccion)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Fecha Probable de Parto", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(viewModel.fechaProbablePartoFormateada ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ColorTituloSeccion)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Fecha estimada a partir de la monta o inseminación; el nacimiento puede ocurrir antes o después.",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        TextButton(onClick={viewModel.cerrarGestacion("Finalizado")},enabled=!viewModel.estaCargando){Text("Marcar gestación finalizada")}
                        TextButton(onClick={viewModel.cerrarGestacion("Interrumpido")},enabled=!viewModel.estaCargando){Text("Marcar gestación interrumpida")}
                    } else {
                        Button(
                            onClick = { viewModel.mostrarDialogoRegistroEmbarazo = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTituloSeccion),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar Nueva Gestación")
                        }
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

            // --- SECCIÓN: UBICACIÓN (EDITABLE CON DROPDOWN) ---
            CardSeccionEditar("Ubicación Actual") {
                Text("Lote Registrado", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                // Esta función se toma de PantallaRegistrarAnimal.kt para evitar duplicidad
                SelectorLoteDropdown(
                    loteSeleccionado = viewModel.ubicacion,
                    onLoteChange = { nuevaUbicacion -> viewModel.ubicacion = nuevaUbicacion },
                    listaLotes = viewModel.listaLotes
                )
            }

            // --- SECCIÓN: NACIMIENTO Y ORIGEN ---
            CardSeccionEditar("Nacimiento y Origen") {
                SelectorFormulario("Origen", viewModel.origen, listOf("De parto","Comprada"), {viewModel.origen=it})
                InfoItemLecturaFicha("Fecha de Registro en App", viewModel.fechaRegistroFormateada, Icons.Outlined.AppRegistration)
                InfoItemLecturaFicha("Hora de Nacimiento", viewModel.horaNacimiento, Icons.Outlined.AccessTime)
            }

            // --- SECCIÓN: DATOS EDITABLES ---
            CardSeccionEditar("Características y Fechas") {
                CampoFormulario("Nacimiento (opcional)",viewModel.fechaNacimiento,{viewModel.fechaNacimiento=it},viewModel.erroresCampos["fecha"],"dd/mm/aaaa")
                CampoFormulario("Peso en kg *",viewModel.peso,{viewModel.peso=it},viewModel.erroresCampos["peso"],"Ej. 45,5",KeyboardType.Decimal)
                CampoEditable("Color", viewModel.color, { viewModel.color = it })
                CampoEditable("Marcas / Señas", viewModel.marcas, { viewModel.marcas = it })
                CampoEditable("Características / Notas", viewModel.caracteristica, { viewModel.caracteristica = it })
            }

            MensajeFormulario(viewModel.mensajeError)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.actualizarAnimal(context) },
                enabled = !viewModel.estaCargando,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun CampoEditable(label: String, valor: String, onValueChange: (String) -> Unit, k: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = k),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
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
