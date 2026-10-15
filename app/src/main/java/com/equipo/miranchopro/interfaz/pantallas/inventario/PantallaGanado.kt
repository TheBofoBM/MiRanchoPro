package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.Validaciones

private val seccionesGanado = listOf(
    "Vaca" to "Vacas", "Toro" to "Toros", "Becerro" to "Becerros",
    "Becerra" to "Becerras", "Novillo" to "Novillos", "Vaquilla" to "Vaquillas"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGanado(nav: NavController, vm: PanelRanchoViewModel) {
    val animales by vm.animales.collectAsState()
    val enfermedades by vm.enfermedades.collectAsState()
    var busqueda by rememberSaveable { mutableStateOf("") }
    var filtros by rememberSaveable { mutableStateOf(false) }
    var mostrarListado by rememberSaveable { mutableStateOf(false) }
    var categoria by rememberSaveable { mutableStateOf("Todas") }
    var sexo by rememberSaveable { mutableStateOf("Todos") }
    var lote by rememberSaveable { mutableStateOf("Todos los lotes") }
    var estado by rememberSaveable { mutableStateOf("Activos") }
    var edad by rememberSaveable { mutableStateOf("Todas las edades") }
    var salud by rememberSaveable { mutableStateOf("Todos") }
    var cancelarId by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollSecciones = rememberLazyListState()
    val scrollListado = rememberLazyListState()
    val enSecciones = !mostrarListado && busqueda.isBlank()
    val activos = animales.filter { it.estado !in listOf("Baja", "Pendiente") }
    val pendientes = animales.count { it.estado == "Pendiente" }
    val enfermos = enfermedades.filter { it.estado == "Activo" }.map { it.idAnimal }.toSet()

    fun limpiarFiltros() {
        categoria = "Todas"; sexo = "Todos"; lote = "Todos los lotes"
        estado = "Activos"; edad = "Todas las edades"; salud = "Todos"; busqueda = ""
    }
    fun volverASecciones() {
        limpiarFiltros(); filtros = false; mostrarListado = false
    }
    fun abrirListado(tipo: String = "Todas", nuevoEstado: String = "Activos") {
        limpiarFiltros(); categoria = tipo; estado = nuevoEstado; filtros = false; mostrarListado = true
    }
    BackHandler(enabled = !enSecciones) { volverASecciones() }
    cancelarId?.let { id ->
        ConfirmarEliminacion("el nacimiento temporal sin completar", { cancelarId = null }, {
            vm.cancelarPendiente(id); cancelarId = null
        })
    }
    LaunchedEffect(animales, estado) {
        if (estado == "Pendiente" && animales.isNotEmpty() && pendientes == 0) estado = "Activos"
    }
    LaunchedEffect(categoria, estado, mostrarListado) { scrollListado.scrollToItem(0) }
    val haceUnAno = java.util.Calendar.getInstance().apply { add(java.util.Calendar.YEAR, -1) }.timeInMillis
    val lista = animales.filter { a ->
        (estado == "Todos" || (estado == "Activos" && a.estado !in listOf("Baja", "Pendiente")) || a.estado == estado) &&
            (categoria == "Todas" || a.tipo == categoria) &&
            (sexo == "Todos" || (a.tipo in listOf("Vaca", "Vaquilla", "Becerra")) == (sexo == "Hembra")) &&
            (lote == "Todos los lotes" || a.ubicacion == lote) &&
            (salud == "Todos" || (a.idArete in enfermos) == (salud == "Con enfermedad activa")) &&
            (edad == "Todas las edades" || (edad == "Nacimiento desconocido" && a.fechaNacimiento == null) ||
                (edad == "Menores de un año" && a.fechaNacimiento?.let { it > haceUnAno } == true) ||
                (edad == "Un año o más" && a.fechaNacimiento?.let { it <= haceUnAno } == true)) &&
            listOf(a.idArete, a.nombre, a.raza).any { it.contains(busqueda.trim(), true) }
    }.sortedBy { it.idArete.lowercase() }
    val titulo = when {
        enSecciones -> "Ganado"
        estado == "Pendiente" -> "Pendientes"
        estado == "Baja" -> "Dados de baja"
        categoria != "Todas" -> seccionesGanado.firstOrNull { it.first == categoria }?.second ?: categoria
        else -> "Todo el ganado"
    }

    Scaffold(
        topBar = {
            Surface(shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                    if (!enSecciones) IconButton(onClick = ::volverASecciones) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a secciones")
                    }
                    Column(Modifier.weight(1f)) {
                        Text(titulo, style = MaterialTheme.typography.headlineSmall,modifier=Modifier.semantics { heading() })
                        Text(if (enSecciones) "${activos.size} ${if(activos.size==1) "animal activo" else "animales activos"} en tu rancho" else "${lista.size} ${if(lista.size==1) "registro" else "registros"} en esta vista", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { nav.navigate(Pantalla.Agenda.ruta) }) { Icon(Icons.Default.CalendarMonth, "Abrir calendario") }
                }
            }
        },
        floatingActionButton = {
            if (estado !in listOf("Pendiente", "Baja")) FloatingActionButton(
                onClick = { nav.navigate(Pantalla.RegistrarAnimal.crearRuta(tipo = categoria.takeUnless { it == "Todas" })) },
                shape = CircleShape, modifier = Modifier.size(64.dp)
            ) { Icon(Icons.Default.Add, "Registrar animal", modifier=Modifier.size(30.dp)) }
        }
    ) { p ->
        LazyColumn(
            Modifier.fillMaxSize().padding(p).testTag("contenido-ganado"),
            state = if (enSecciones) scrollSecciones else scrollListado,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "operacion") { EstadoOperacion(vm) }
            if (pendientes > 0 && estado != "Pendiente") item(key = "pendientes") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("REGISTROS POR COMPLETAR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.semantics { heading() })
                    CardCategoriaLujo(
                        titulo = "Nacimientos pendientes", subtitulo = "$pendientes ${if(pendientes==1) "registro rápido" else "registros rápidos"} · Toca para completar",
                        colorFondo = Color(0xFFFFF8E1), icono = Icons.Default.NewReleases, colorIcono = Color(0xFFFFA000),
                        onClick = { abrirListado(nuevoEstado = "Pendiente") }
                    )
                }
            }
            item(key = "busqueda") {
                Column {
                    OutlinedTextField(
                        busqueda, { busqueda = it }, label = { Text("Buscar ganado") }, placeholder = { Text("Arete, nombre o raza") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = { if (busqueda.isNotEmpty()) IconButton(onClick = { busqueda = "" }) { Icon(Icons.Default.Close, "Limpiar búsqueda") } },
                        shape = RoundedCornerShape(16.dp), singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = { mostrarListado = true; filtros = !filtros }) {
                        Icon(Icons.Default.FilterList, null); Spacer(Modifier.width(8.dp))
                        Text(if (filtros) "Ocultar filtros" else "Filtrar ganado")
                    }
                }
            }
            if (enSecciones) {
                item(key = "secciones-titulo") {
                    Text("SECCIONES DEL RANCHO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.semantics { heading() })
                }
                items(seccionesGanado, key = { "categoria-${it.first}" }) { (tipo, nombre) ->
                    val cantidad = activos.count { it.tipo == tipo }
                    CardCategoriaLujo(titulo = nombre, subtitulo = if (cantidad == 1) "1 animal" else "$cantidad animales", onClick = { abrirListado(tipo) })
                }
                item(key = "todos") {
                    CardCategoriaLujo("Todo el ganado", "${activos.size} ${if(activos.size==1) "animal activo" else "animales activos"}", icono = Icons.AutoMirrored.Filled.ViewList, onClick = { abrirListado() })
                }
                item(key = "bajas") {
                    val bajas = animales.count { it.estado == "Baja" }
                    CardCategoriaLujo("Dados de baja", "$bajas registros · Histórico", icono = Icons.Default.History, onClick = { abrirListado(nuevoEstado = "Baja") })
                }
            } else {
                if (filtros) {
                    item { SelectorFormulario("Categoría", categoria, listOf("Todas") + seccionesGanado.map { it.first }, { categoria = it }) }
                    item { SelectorFormulario("Sexo", sexo, listOf("Todos", "Hembra", "Macho"), { sexo = it }) }
                    item { SelectorFormulario("Lote", lote, listOf("Todos los lotes") + animales.map { it.ubicacion }.distinct().sorted(), { lote = it }) }
                    item { SelectorFormulario("Estado", estado, listOf("Activos", "Todos", "Baja", "Pendiente"), { estado = it }) }
                    item { SelectorFormulario("Edad", edad, listOf("Todas las edades", "Menores de un año", "Un año o más", "Nacimiento desconocido"), { edad = it }) }
                    item {
                        SelectorFormulario("Salud", salud, listOf("Todos", "Con enfermedad activa", "Sin enfermedad activa"), { salud = it })
                        OutlinedButton(onClick = ::limpiarFiltros, modifier = Modifier.fillMaxWidth()) { Text("Limpiar filtros") }
                    }
                }
                if (lista.isEmpty()) item {
                    TarjetaDato(if (animales.isEmpty()) "Registra tu primer animal" else "Sin coincidencias", if (animales.isEmpty()) "Comienza con su arete, peso y categoría. Puedes añadir una fotografía." else "Ajusta los filtros o la búsqueda para encontrar otros registros.")
                }
                items(lista, key = { "animal-${it.idArete}" }) { a ->
                    if (a.estado == "Pendiente") {
                        TarjetaNacimientoPendiente(a, !vm.guardando, { nav.navigate(Pantalla.RegistrarAnimal.crearRuta(idTemp = a.idArete)) }, { cancelarId = a.idArete })
                    } else {
                        ElevatedCard(
                            onClick = { nav.navigate(Pantalla.FichaAnimal.crearRuta(a.idArete)) }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (a.fotoPath != null) AsyncImage(model = a.fotoPath, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("#${a.idArete}", style = MaterialTheme.typography.titleMedium)
                                    if (a.nombre.isNotBlank()) Text(a.nombre, maxLines = 2)
                                    Text("${a.tipo} · ${a.peso} kg\n${a.ubicacion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(if (a.idArete in enfermos) "${a.estado} · Enfermedad activa" else a.estado, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaNacimientoPendiente(animal: Animal, enabled: Boolean, completar: () -> Unit, cancelar: () -> Unit) {
    OutlinedCard(onClick = completar, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.NewReleases, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(Modifier.weight(1f)) {
                    Text("Registro rápido", style = MaterialTheme.typography.titleMedium)
                    Text("Nacimiento por completar", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(Validaciones.fechaTexto(animal.fechaRegistro), style = MaterialTheme.typography.bodyMedium)
            }
            if (!animal.horaNacimiento.isNullOrBlank()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Registrado a las ${animal.horaNacimiento}", style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Completa el arete y el peso para añadir este nacimiento a tu ganado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilledTonalButton(onClick = completar, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.EditNote, null); Spacer(Modifier.width(8.dp)); Text("Completar registro")
            }
            TextButton(onClick = cancelar, enabled = enabled) {
                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(8.dp))
                Text("Cancelar registro temporal", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
