package com.equipo.miranchopro.interfaz.pantallas.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.interfaz.pantallas.inventario.GraficaPesos
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaEstadisticas(nav: NavController,vm: PanelRanchoViewModel) {
    val animales by vm.animales.collectAsState();val pesos by vm.pesajes.collectAsState();val enfermedades by vm.enfermedades.collectAsState()
    val movimientos by vm.movimientos.collectAsState();val vacunas by vm.vacunas.collectAsState();val partos by vm.partos.collectAsState()
    var periodo by rememberSaveable { mutableStateOf("30 días") };var lote by rememberSaveable { mutableStateOf("Todos los lotes") };var animal by rememberSaveable { mutableStateOf("") }
    val inicio=if(periodo=="Todo el historial") 0L else AgendaRancho.sumarDias(System.currentTimeMillis(),-periodo.substringBefore(" ").toInt())
    val seleccion=animales.filter { lote=="Todos los lotes" || it.ubicacion==lote };val ids=seleccion.map { it.idArete }.toSet();val activos=seleccion.filter { it.estado !in listOf("Baja","Pendiente") }
    val nacimientos=seleccion.filter { it.estado!="Pendiente" && it.fechaNacimiento?.let { fecha -> fecha>=inicio && fecha<=Validaciones.finDeHoy() }==true }
    val frecuencia=enfermedades.filter { it.idAnimal in ids && (Validaciones.fecha(it.fechaDeteccion) ?: -1)>=inicio }.groupingBy { it.nombre.trim().lowercase() }.eachCount().entries.sortedByDescending { it.value }
    val consumos=movimientos.filter { it.tipo=="Consumo" && it.fecha>=inicio }.groupBy { it.unidad }.mapValues { (_,registros) -> registros.sumOf { -it.cantidad } }
    MarcoRancho("Estadísticas",{nav.popBackStack()}) {
        item { SelectorFormulario("Periodo",periodo,listOf("7 días","30 días","90 días","365 días","Todo el historial"),{periodo=it}) }
        item { SelectorFormulario("Lote actual",lote,listOf("Todos los lotes")+animales.map { it.ubicacion }.distinct().sorted(),{lote=it;animal=""});Text("El filtro agrupa animales por su lote actual.",style=MaterialTheme.typography.bodySmall) }
        item { TarjetaDato("Ganado activo · ${activos.size}","Peso actual promedio: ${activos.filter { it.peso.isFinite() && it.peso>0 }.map { it.peso }.average().takeIf { it.isFinite() }?.let { String.format(Locale.getDefault(),"%.1f kg",it) } ?: "Sin datos"}") }
        item { TarjetaDato("En el periodo seleccionado","${nacimientos.size} animales nacidos con fecha conocida\n${partos.count { it.madreId in ids && it.fecha>=inicio }} partos registrados\n${vacunas.count { it.idAnimal in ids && (Validaciones.fecha(it.fechaAplicacion) ?: -1)>=inicio }} aplicaciones registradas") }
        item { TituloSeccion("Nacimientos por mes") }
        val meses=nacimientos.groupingBy { SimpleDateFormat("yyyy-MM",Locale.ROOT).format(Date(it.fechaNacimiento!!)) }.eachCount().entries.sortedBy { it.key }
        if(meses.isEmpty()) item { Text("No hay nacimientos con fecha conocida en este periodo.") }
        items(meses,key={it.key}) { Text("${it.key} · ${it.value} nacimientos") }
        item { TituloSeccion("Evolución de peso por animal") }
        val opciones=seleccion.filter { a -> pesos.any { it.animalId==a.idArete && it.fecha>=inicio } }.map { it.idArete }.sorted()
        if(opciones.isEmpty()) item { Text("Registra pesajes desde la ficha del animal para ver su evolución.") }
        else item {
            SelectorFormulario("Arete",animal.ifBlank { "Seleccionar animal" },opciones,{animal=it})
            val propios=pesos.filter { it.animalId==animal && it.fecha>=inicio }
            if(propios.isNotEmpty()) GraficaPesos(propios)
        }
        item { TituloSeccion("Problemas de salud registrados") }
        if(frecuencia.isEmpty()) item { Text("No hay registros de enfermedad en este periodo.") }
        items(frecuencia,key={it.key}) { TarjetaDato(it.key,"${it.value} registros") }
        item { TituloSeccion("Consumo del rancho") ;Text("Inventario general; no se atribuye al lote seleccionado.",style=MaterialTheme.typography.bodySmall) }
        if(consumos.isEmpty()) item { Text("Todavía no hay consumos registrados en el periodo.") }
        items(consumos.entries.toList(),key={it.key}) { Text("${String.format(Locale.getDefault(),"%.2f",it.value)} ${it.key}") }
    }
}
