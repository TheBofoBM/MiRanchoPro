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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

private val ForestGreen = Color(0xFF004D40)
private val EmeraldPrimary: Color @Composable get() = MaterialTheme.colorScheme.primary
private val ColorFondo: Color @Composable get() = MaterialTheme.colorScheme.background
private val ColorGrisSuave: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarAnimal(
    viewModel: RegistrarAnimalViewModel = viewModel(),
    alFinalizar: () -> Unit = {}
) {
    val context = LocalContext.current
    val claveBorrador=LocalRutaBorrador.current+"|animal-alta"
    var terminado by remember { mutableStateOf(false) }
    if(!terminado && (!LocalRutaBorrador.current.contains("TEMP-") || viewModel.esEdicionPendiente)) BorradorAnimal(claveBorrador,viewModel.camposBorrador(),viewModel::restaurarBorrador)
    val snackbarHostState = remember { SnackbarHostState() }
    var expandedTipo by remember { mutableStateOf(false) }
    var mostrarDialogoOpcionesFoto by remember { mutableStateOf(false) }
    var mostrarConfirmacionCamara by remember { mutableStateOf(false) }
    var tempUriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) { runCatching { context.contentResolver.takePersistableUriPermission(uri,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) };viewModel.fotoUri = uri } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            mostrarConfirmacionCamara = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is RegistrarAnimalViewModel.EventoUI.Exito -> {
                    terminado=true;Borradores.borrar(context,claveBorrador)
                    snackbarHostState.showSnackbar(evento.mensaje)
                    alFinalizar()
                }
                is RegistrarAnimalViewModel.EventoUI.Error -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    // Diálogo para elegir entre Cámara o Galería
    if (mostrarDialogoOpcionesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOpcionesFoto = false },
            title = { Text("Adjuntar fotografía", fontWeight = FontWeight.Bold) },
            text = { Text("Selecciona el origen de la imagen para el animal.") },
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
                        viewModel.fotoUri=tempUriCamara
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text(if(viewModel.esEdicionPendiente) "Completar nacimiento" else "Registrar animal") },
            navigationIcon = { IconButton(onClick=alFinalizar) { Icon(Icons.Default.ArrowBack,"Volver") } }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("Identificación",style=MaterialTheme.typography.titleMedium)
            Text("Los campos con * son obligatorios. Puedes dejar el nacimiento vacío si lo desconoces.",style=MaterialTheme.typography.bodyMedium)
            MensajeFormulario(viewModel.mensajeError)
            Text("El borrador se conserva en este teléfono al salir.",style=MaterialTheme.typography.bodySmall)
            TextButton(onClick={terminado=true;Borradores.borrar(context,claveBorrador);viewModel.limpiarCampos();alFinalizar()},enabled=!viewModel.estaCargando){Text("Descartar borrador y salir")}
            CampoFormulario("Arete *",viewModel.idArete,{viewModel.idArete=it},viewModel.erroresCampos["arete"],"Ej. A-102",enfocar=viewModel.erroresCampos.keys.firstOrNull()=="arete")
            CampoFormulario("Nombre (opcional)",viewModel.nombre,{viewModel.nombre=it},ejemplo="Ej. Lucero")
            val hembra=viewModel.tipo in listOf("Vaca","Vaquilla","Becerra")
            SelectorFormulario("Sexo",if(hembra) "Hembra" else "Macho",listOf("Hembra","Macho"),{sexo->viewModel.tipo=if(sexo=="Hembra") "Becerra" else "Becerro"})
            SelectorFormulario("Categoría",viewModel.tipo,viewModel.tiposDisponibles.filter{if(hembra) it in listOf("Vaca","Vaquilla","Becerra") else it in listOf("Toro","Novillo","Becerro")},{viewModel.tipo=it})
            CampoFormulario("Raza",viewModel.raza,{viewModel.raza=it})
            CampoFormulario("Peso en kg *",viewModel.peso,{viewModel.peso=it},viewModel.erroresCampos["peso"],"Ej. 45,5",KeyboardType.Decimal,enfocar=viewModel.erroresCampos.keys.firstOrNull()=="peso")
            CampoFormulario("Nacimiento (opcional)",viewModel.fechaNacimiento,{viewModel.fechaNacimiento=it},viewModel.erroresCampos["fecha"],"dd/mm/aaaa",enfocar=viewModel.erroresCampos.keys.firstOrNull()=="fecha")
            SelectorFormulario("Origen",viewModel.origen,viewModel.origenesDisponibles,{viewModel.origen=it})
            SelectorFormulario("Ubicación / lote",viewModel.ubicacion,viewModel.listaLotes,{viewModel.ubicacion=it})
            CampoFormulario("Color (opcional)",viewModel.color,{viewModel.color=it})
            CampoFormulario("Marcas (opcional)",viewModel.marcas,{viewModel.marcas=it})
            Text("Fotografía opcional",style=MaterialTheme.typography.titleMedium)
            if(viewModel.fotoUri!=null || viewModel.fotoPath!=null) {
                AsyncImage(model=viewModel.fotoUri ?: viewModel.fotoPath,contentDescription="Fotografía del animal",modifier=Modifier.fillMaxWidth().height(140.dp),contentScale=ContentScale.Crop)
                TextButton(onClick={viewModel.fotoUri=null;viewModel.fotoPath=null}){Text("Quitar fotografía")}
            }
            OutlinedButton(onClick={mostrarDialogoOpcionesFoto=true},modifier=Modifier.fillMaxWidth()){Text("Agregar o cambiar fotografía")}
            MensajeFormulario(viewModel.mensajeError)
            Button(onClick={viewModel.registrarAnimal(context)},enabled=!viewModel.estaCargando,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) {
                if(viewModel.estaCargando) CircularProgressIndicator(Modifier.size(24.dp)) else Text("Guardar animal")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorLoteDropdown(loteSeleccionado: String, onLoteChange: (String) -> Unit, listaLotes: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = loteSeleccionado, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorGrisSuave,
                unfocusedContainerColor = ColorGrisSuave,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listaLotes.forEach { lote ->
                DropdownMenuItem(text = { Text(lote) }, onClick = { onLoteChange(lote); expanded = false })
            }
        }
    }
}

@Composable
fun CardRegistroModerno(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = titulo.uppercase(), color = EmeraldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.5.sp)
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
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = k),
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorGrisSuave,
                unfocusedContainerColor = ColorGrisSuave,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
