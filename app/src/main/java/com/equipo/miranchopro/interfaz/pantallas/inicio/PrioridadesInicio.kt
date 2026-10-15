package com.equipo.miranchopro.interfaz.pantallas.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.*

@Composable
fun PrioridadesInicio(vm: PanelRanchoViewModel,usuario: Usuario?,nav: NavController) {
    val animales by vm.animales.collectAsState();val tareas by vm.tareas.collectAsState();val vacunas by vm.vacunas.collectAsState();val gestaciones by vm.gestaciones.collectAsState()
    val insumos by vm.insumos.collectAsState();val medicamentos by vm.medicamentos.collectAsState()
    val agenda=AgendaRancho.construir(animales,tareas,vacunas,gestaciones,usuario)
    val limite=AgendaRancho.sumarDias(AgendaRancho.inicioDia(System.currentTimeMillis()),8)
    val proximos=agenda.filter { it.fecha<limite }
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        if(animales.isEmpty()) {
            ElevatedCard(Modifier.fillMaxWidth(),colors=CardDefaults.elevatedCardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    TituloSeccion("Primeros pasos en tu rancho")
                    Text("1. Crea un lote para organizar al ganado.\n2. Registra tu primer animal.\n3. Añade su primer evento de salud o pesaje.")
                    if(usuario?.rol?.equals("Administrador",true)==true) OutlinedButton(onClick={nav.navigate(Pantalla.RegistrarLote.ruta)},modifier=Modifier.fillMaxWidth()){Text("Crear primer lote")}
                    Button(onClick={nav.navigate(Pantalla.RegistrarAnimal.crearRuta())},modifier=Modifier.fillMaxWidth()){Text("Registrar primer animal")}
                    TextButton(onClick={nav.navigate(Pantalla.Ayuda.ruta)}){Text("Ver guía de uso")}
                }
            }
        }
        TituloSeccion("Lo que necesita atención")
        val nacimientosPendientes=animales.count { it.estado=="Pendiente" }
        if(nacimientosPendientes>0) OutlinedButton(onClick={nav.navigate(Pantalla.Inventario.ruta)},modifier=Modifier.fillMaxWidth()){Text("Nacimientos pendientes: $nacimientosPendientes · Completar")}
        if(proximos.isEmpty()) TarjetaDato("Sin pendientes próximos","Consulta el calendario para planear las siguientes semanas.")
        proximos.take(3).forEach { p -> ElevatedCard(onClick={nav.navigate(p.animalId?.let { Pantalla.FichaAnimal.crearRuta(it) } ?: Pantalla.Tareas.ruta)},modifier=Modifier.fillMaxWidth(),colors=CardDefaults.elevatedCardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) { Text(p.titulo,style=MaterialTheme.typography.titleMedium);Text("${Validaciones.fechaTexto(p.fecha)} · ${p.detalle}",color=MaterialTheme.colorScheme.onSurfaceVariant) }
        } }
        OutlinedButton(onClick={nav.navigate(Pantalla.Agenda.ruta)},modifier=Modifier.fillMaxWidth()){Text("Ver calendario completo (${agenda.size})")}
        val hoy=Validaciones.finDeHoy()
        val alertas=insumos.count { it.cantidad<=it.stockMinimo || it.fechaCaducidad?.let { f -> f<hoy }==true } + medicamentos.count { it.stock<=it.stockMinimo || it.fechaCaducidad?.let { f -> f<hoy }==true }
        if(alertas>0) OutlinedButton(onClick={nav.navigate(Pantalla.Movimientos.ruta)},modifier=Modifier.fillMaxWidth()){Text("$alertas productos requieren revisar existencias o caducidad")}
    }
}

@Composable fun PantallaAyuda(nav: NavController) {
    MarcoRancho("Guía de uso",{nav.popBackStack()}) {
        item { TarjetaDato("1 · Organiza el rancho","En Más → Lotes crea espacios con capacidad. Después asigna los animales desde su ficha.") }
        item { TarjetaDato("2 · Registra el ganado","En Ganado añade arete, categoría y peso. El nacimiento puede permanecer desconocido. Los filtros ayudan a localizar animales.") }
        item { TarjetaDato("3 · Da seguimiento","Abre un animal para registrar pesajes, consultar salud y ver reproducción. Al registrar un parto se enlazan madre y crías.") }
        item { TarjetaDato("4 · Planea tu semana","Añade fechas a las tareas y próximas aplicaciones. El calendario reúne esos pendientes y el seguimiento de partos.") }
        item { TarjetaDato("5 · Cuida las existencias","En Movimientos registra entradas o consumos con motivo y consulta quién los registró. Configura mínimos y caducidad por producto.") }
        item { TarjetaDato("6 · Protege tu información","Desde Configuración crea un respaldo y guárdalo fuera del teléfono. Necesitarás la contraseña del respaldo para recuperarlo.") }
    }
}
