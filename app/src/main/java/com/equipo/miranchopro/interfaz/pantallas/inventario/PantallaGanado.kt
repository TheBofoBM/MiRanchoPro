package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.AgendaRancho

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGanado(nav: NavController,vm: PanelRanchoViewModel) {
    val animales by vm.animales.collectAsState();val enfermedades by vm.enfermedades.collectAsState()
    var busqueda by rememberSaveable { mutableStateOf("") };var filtros by rememberSaveable { mutableStateOf(false) }
    var categoria by rememberSaveable { mutableStateOf("Todas") };var sexo by rememberSaveable { mutableStateOf("Todos") }
    var cancelarId by rememberSaveable { mutableStateOf<String?>(null) }
    cancelarId?.let { id -> ConfirmarEliminacion("el nacimiento temporal sin completar",{cancelarId=null},{vm.cancelarPendiente(id);cancelarId=null}) }
    var lote by rememberSaveable { mutableStateOf("Todos los lotes") };var estado by rememberSaveable { mutableStateOf("Activos") };var edad by rememberSaveable { mutableStateOf("Todas las edades") };var salud by rememberSaveable { mutableStateOf("Todos") }
    val enfermos=enfermedades.filter { it.estado=="Activo" }.map { it.idAnimal }.toSet()
    LaunchedEffect(animales,estado) { if(estado=="Pendiente" && animales.isNotEmpty() && animales.none { it.estado=="Pendiente" }) estado="Activos" }
    val haceUnAno=java.util.Calendar.getInstance().apply { add(java.util.Calendar.YEAR,-1) }.timeInMillis
    val lista=animales.filter { a ->
        (estado=="Todos" || (estado=="Activos" && a.estado !in listOf("Baja","Pendiente")) || a.estado==estado) &&
        (categoria=="Todas" || a.tipo==categoria) && (sexo=="Todos" || (a.tipo in listOf("Vaca","Vaquilla","Becerra"))==(sexo=="Hembra")) &&
        (lote=="Todos los lotes" || a.ubicacion==lote) && (salud=="Todos" || (a.idArete in enfermos)==(salud=="Con enfermedad activa")) &&
        (edad=="Todas las edades" || (edad=="Nacimiento desconocido" && a.fechaNacimiento==null) || (edad=="Menores de un año" && a.fechaNacimiento?.let { it>haceUnAno }==true) || (edad=="Un año o más" && a.fechaNacimiento?.let { it<=haceUnAno }==true)) &&
        listOf(a.idArete,a.nombre,a.raza).any { it.contains(busqueda.trim(),true) }
    }.sortedBy { it.idArete.lowercase() }
    Scaffold(topBar={TopAppBar(title={Column { Text("Ganado");Text("${lista.size} de ${animales.size} registros",style=MaterialTheme.typography.bodySmall) }},actions={IconButton(onClick={nav.navigate(Pantalla.Agenda.ruta)}){Icon(Icons.Default.CalendarMonth,"Abrir calendario")}})},floatingActionButton={ExtendedFloatingActionButton(onClick={nav.navigate(Pantalla.RegistrarAnimal.crearRuta())},icon={Icon(Icons.Default.Add,null)},text={Text("Registrar animal")})}) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(start=20.dp,end=20.dp,top=12.dp,bottom=100.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            val pendientes=animales.count { it.estado=="Pendiente" }
            if(pendientes>0) item {
                OutlinedButton(onClick={estado="Pendiente";categoria="Todas";sexo="Todos";lote="Todos los lotes";edad="Todas las edades";salud="Todos";busqueda="";filtros=false},modifier=Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PendingActions,null);Spacer(Modifier.width(8.dp));Text("Nacimientos por completar: $pendientes")
                }
            }
            item { EstadoOperacion(vm) }
            item { OutlinedTextField(busqueda,{busqueda=it},label={Text("Buscar arete, nombre o raza")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,modifier=Modifier.fillMaxWidth());TextButton(onClick={filtros=!filtros}){Icon(Icons.Default.FilterList,null);Text(if(filtros) "Ocultar filtros" else "Filtrar ganado")} }
            if(filtros) {
                item { SelectorFormulario("Categoría",categoria,listOf("Todas","Vaca","Toro","Becerro","Becerra","Novillo","Vaquilla"),{categoria=it}) }
                item { SelectorFormulario("Sexo",sexo,listOf("Todos","Hembra","Macho"),{sexo=it}) }
                item { SelectorFormulario("Lote",lote,listOf("Todos los lotes")+animales.map { it.ubicacion }.distinct().sorted(),{lote=it}) }
                item { SelectorFormulario("Estado",estado,listOf("Activos","Todos","Baja","Pendiente"),{estado=it}) }
                item { SelectorFormulario("Edad",edad,listOf("Todas las edades","Menores de un año","Un año o más","Nacimiento desconocido"),{edad=it}) }
                item { SelectorFormulario("Salud",salud,listOf("Todos","Con enfermedad activa","Sin enfermedad activa"),{salud=it});OutlinedButton(onClick={categoria="Todas";sexo="Todos";lote="Todos los lotes";estado="Activos";edad="Todas las edades";salud="Todos";busqueda=""},modifier=Modifier.fillMaxWidth()){Text("Limpiar filtros")} }
            }
            if(lista.isEmpty()) item { TarjetaDato(if(animales.isEmpty()) "Registra tu primer animal" else "Sin coincidencias",if(animales.isEmpty()) "Comienza con su arete, peso y categoría. Puedes añadir una fotografía." else "Ajusta los filtros o la búsqueda para encontrar otros registros.") }
            items(lista,key={it.idArete}) { a ->
                ElevatedCard(onClick={nav.navigate(if(a.estado=="Pendiente") Pantalla.RegistrarAnimal.crearRuta(idTemp=a.idArete) else Pantalla.FichaAnimal.crearRuta(a.idArete))},modifier=Modifier.fillMaxWidth(),colors=CardDefaults.elevatedCardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                        if(a.fotoPath!=null) AsyncImage(model=a.fotoPath,contentDescription=null,modifier=Modifier.size(64.dp),contentScale=ContentScale.Crop)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            Text(if(a.estado=="Pendiente") "Nacimiento por completar" else "#${a.idArete}",style=MaterialTheme.typography.titleMedium)
                            if(a.nombre.isNotBlank()) Text(a.nombre,maxLines=2)
                            Text("${a.tipo} · ${a.peso} kg\n${a.ubicacion}",color=MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if(a.idArete in enfermos) "${a.estado} · Enfermedad activa" else a.estado,color=MaterialTheme.colorScheme.primary)
                            if(a.estado=="Pendiente") TextButton(onClick={cancelarId=a.idArete},enabled=!vm.guardando){Text("Cancelar registro temporal")}
                        }
                        Icon(Icons.Default.ChevronRight,null)
                    }
                }
            }
        }
    }
}
