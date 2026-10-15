package com.equipo.miranchopro.interfaz.pantallas.tareas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.*

@Composable
fun PantallaAgenda(nav: NavController,vm: PanelRanchoViewModel,usuario: Usuario?) {
    val animales by vm.animales.collectAsState();val tareas by vm.tareas.collectAsState();val vacunas by vm.vacunas.collectAsState();val gestaciones by vm.gestaciones.collectAsState()
    var tipo by rememberSaveable { mutableStateOf("Todos") };var horizonte by rememberSaveable { mutableStateOf("30 días") }
    val hoy=AgendaRancho.inicioDia(System.currentTimeMillis());val limite=AgendaRancho.sumarDias(hoy,horizonte.substringBefore(" ").toInt()+1)
    val agenda=AgendaRancho.construir(animales,tareas,vacunas,gestaciones,usuario).filter { (tipo=="Todos" || it.tipo==tipo) && it.fecha<limite }
    MarcoRancho("Calendario de pendientes",{nav.popBackStack()}) {
        item { SelectorFormulario("Mostrar",tipo,listOf("Todos","Tarea","Salud","Parto"),{tipo=it});SelectorFormulario("Próximos",horizonte,listOf("7 días","30 días","90 días","365 días"),{horizonte=it}) }
        item { OutlinedButton(onClick={nav.navigate(Pantalla.Configuracion.ruta)},modifier=Modifier.fillMaxWidth()){Text("Configurar recordatorios")} }
        if(tareas.any { !it.estaHecha && it.fecha.isNullOrBlank() && (usuario?.rol?.equals("Administrador",true)==true || it.responsable==usuario?.correo) }) item { OutlinedButton(onClick={nav.navigate(Pantalla.Tareas.ruta)},modifier=Modifier.fillMaxWidth()){Text("También tienes tareas sin fecha · Ver tareas")} }
        if(agenda.isEmpty()) item { TarjetaDato("Sin pendientes para este periodo","Las tareas con fecha, próximas aplicaciones y gestaciones activas aparecerán aquí.") }
        items(agenda,key={it.id}) { p ->
            ElevatedCard(onClick={nav.navigate(p.animalId?.let { Pantalla.FichaAnimal.crearRuta(it) } ?: Pantalla.Tareas.ruta)},modifier=Modifier.fillMaxWidth(),colors=CardDefaults.elevatedCardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text(p.titulo,style=MaterialTheme.typography.titleMedium)
                    Text("${Validaciones.fechaTexto(p.fecha)} · ${if(p.fecha<hoy) "Pendiente de revisar" else if(AgendaRancho.inicioDia(p.fecha)==hoy) "Hoy" else "Próximo"}",color=MaterialTheme.colorScheme.primary)
                    Text(p.detalle,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    if(p.tipo=="Salud") TextButton(onClick={vm.atenderRecordatorio(p.id.removePrefix("v-"))},enabled=!vm.guardando){Text("Marcar aviso como atendido")}
                }
            }
        }
        item { EstadoOperacion(vm);Text("Atender un aviso lo retira del calendario. Registra la aplicación realizada en Salud para conservarla en el historial.",style=MaterialTheme.typography.bodySmall) }
    }
}
