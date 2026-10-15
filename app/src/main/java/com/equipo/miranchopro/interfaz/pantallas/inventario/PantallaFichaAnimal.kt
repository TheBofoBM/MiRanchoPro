package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.*

private data class EntradaHistoria(val id: String,val fecha: Long,val titulo: String,val detalle: String)

@Composable
fun PantallaFichaAnimal(id: String,nav: NavController,vm: PanelRanchoViewModel) {
    val animales by vm.animales.collectAsState();val pesos by vm.pesajes.collectAsState()
    val vacunas by vm.vacunas.collectAsState();val enfermedades by vm.enfermedades.collectAsState()
    val eventos by vm.eventos.collectAsState();val gestaciones by vm.gestaciones.collectAsState();val partos by vm.partos.collectAsState()
    val historialGestaciones by vm.historialGestaciones.collectAsState()
    val a=animales.find { it.idArete==id }
    var dialogo by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(vm.versionGuardado) { if(vm.versionGuardado>0) dialogo="" }
    LaunchedEffect(dialogo) { vm.limpiarError() }
    val gestacion=gestaciones.find { it.vacaId==id }
    val propios=pesos.filter { it.animalId==id }
    val historia=(pesos.filter { it.animalId==id }.map { EntradaHistoria("p-${it.id}",it.fecha,"Pesaje · ${it.peso} kg",listOf(it.notas,it.responsable).filter(String::isNotBlank).joinToString(" · ")) } +
        vacunas.filter { it.idAnimal==id }.map { EntradaHistoria("v-${it.id}",Validaciones.fecha(it.fechaAplicacion) ?: 0,"Vacunación · ${it.nombreVacuna}","Dosis: ${it.dosis}. ${it.notas}") } +
        enfermedades.filter { it.idAnimal==id }.map { EntradaHistoria("e-${it.id}",Validaciones.fecha(it.fechaDeteccion) ?: 0,"Salud · ${it.nombre}","${it.estado} · ${it.sintomas}\nTratamiento registrado: ${it.tratamiento}") } +
        eventos.filter { it.animalId==id }.map { EntradaHistoria(it.id,it.fecha,it.tipo,it.detalle+if(it.responsable.isBlank()) "" else " · ${it.responsable}") } +
        historialGestaciones.filter { it.vacaId==id }.map { EntradaHistoria("g-${it.id}",it.fechaInseminacion,"Gestación · ${it.estado}","${it.tipo}\nEstimación: ${Validaciones.fechaTexto(it.fechaProbableParto)}. ${it.notas}") }
    ).sortedByDescending { it.fecha }
    if(dialogo=="peso") DialogoPesaje(id,vm) { dialogo="" }
    if(dialogo=="gestacion") DialogoGestacion(id,vm) { dialogo="" }
    if(dialogo=="parto" && a!=null) DialogoParto(a,gestacion,vm) { dialogo="" }
    if(dialogo=="interrumpir") AlertDialog(onDismissRequest={dialogo=""},title={Text("Interrumpir gestación")},text={Text("Se conservará el registro en el historial.")},confirmButton={TextButton(onClick={vm.cerrarGestacion(id)},enabled=!vm.guardando){Text("Confirmar")}},dismissButton={TextButton(onClick={dialogo=""}){Text("Volver")}})
    MarcoRancho("Ficha del animal",{nav.popBackStack()}) {
        if(a==null) item { Text("Cargando animal…") }
        else {
            item { TituloSeccion(a.nombre.ifBlank { "Arete ${a.idArete}" });Text("#${a.idArete} · ${a.tipo} · ${a.estado}",color=MaterialTheme.colorScheme.onSurfaceVariant) }
            a.fotoPath?.let { foto -> item { AsyncImage(model=foto,contentDescription="Fotografía de ${a.nombre.ifBlank { a.idArete }}",modifier=Modifier.fillMaxWidth().height(190.dp),contentScale=ContentScale.Crop) } }
            item { TarjetaDato("${a.peso} kg · ${a.ubicacion}","Nacimiento: ${Validaciones.fechaTexto(a.fechaNacimiento).ifBlank { "Desconocido" }}\nEdad: ${Validaciones.edad(a.fechaNacimiento)} · Raza: ${a.raza}") }
            a.madreId?.let { madre -> item { OutlinedButton(onClick={nav.navigate(Pantalla.FichaAnimal.crearRuta(madre))},modifier=Modifier.fillMaxWidth()){Text("Ver madre · $madre")} } }
            val crias=animales.filter { it.madreId==id }
            if(crias.isNotEmpty()) item { TituloSeccion("Crías registradas");crias.forEach { cria -> TextButton(onClick={nav.navigate(Pantalla.FichaAnimal.crearRuta(cria.idArete))}){Text("${cria.idArete} · ${cria.tipo}")} } }
            item {
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Button(onClick={dialogo="peso"},enabled=a.estado !in listOf("Baja","Pendiente"),modifier=Modifier.fillMaxWidth()){Text("Registrar pesaje")}
                    OutlinedButton(onClick={nav.navigate(Pantalla.EditarAnimal.crearRuta(id))},modifier=Modifier.fillMaxWidth()){Text("Editar datos del animal")}
                }
            }
            if(a.tipo in listOf("Vaca","Vaquilla","Becerra") && a.estado !in listOf("Baja","Pendiente")) item {
                TituloSeccion("Reproducción")
                if(gestacion!=null) {
                    TarjetaDato("Gestación activa","${gestacion.tipo} · ${Validaciones.fechaTexto(gestacion.fechaInseminacion)}\nFecha central estimada: ${Validaciones.fechaTexto(gestacion.fechaProbableParto)}\nVentana orientativa: ${AgendaRancho.intervaloParto(gestacion.fechaProbableParto)}")
                    Text("Estimación orientativa de 283 días con margen de 7 días. No determina el día exacto del parto.",style=MaterialTheme.typography.bodySmall)
                    TextButton(onClick={dialogo="interrumpir"}){Text("Marcar gestación interrumpida")}
                } else OutlinedButton(onClick={dialogo="gestacion"},modifier=Modifier.fillMaxWidth()){Text("Registrar monta o inseminación")}
                Button(onClick={dialogo="parto"},modifier=Modifier.fillMaxWidth()){Text("Registrar parto y crías")}
            }
            item { EstadoOperacion(vm) }
            if(propios.isNotEmpty()) item { TituloSeccion("Evolución del peso");GraficaPesos(propios) }
            item { TituloSeccion("Historial") }
            if(historia.isEmpty()) item { Text("Los próximos pesajes y eventos aparecerán aquí.") }
            items(historia,key={it.id}) { e -> TarjetaDato(e.titulo,"${if(e.fecha>0) Validaciones.fechaTexto(e.fecha) else "Fecha sin completar"}\n${e.detalle}") }
            val cierres=partos.filter { it.madreId==id }
            if(cierres.isNotEmpty()) item { TarjetaDato("Partos registrados",cierres.joinToString("\n") { "${Validaciones.fechaTexto(it.fecha)} · ${it.resultado}" }) }
        }
    }
}

@Composable fun GraficaPesos(pesos: List<Pesaje>) {
    val puntos=pesos.sortedWith(compareBy<Pesaje> { it.fecha }.thenBy { it.registradoEn });val primero=puntos.first();val ultimo=puntos.last()
    if(puntos.size<2) { Text("Último pesaje: ${ultimo.peso} kg · ${Validaciones.fechaTexto(ultimo.fecha)}");Text("Agrega otro pesaje para ver la evolución.",style=MaterialTheme.typography.bodySmall);return }
    Text("${Validaciones.fechaTexto(primero.fecha)}: ${primero.peso} kg → ${Validaciones.fechaTexto(ultimo.fecha)}: ${ultimo.peso} kg",style=MaterialTheme.typography.bodyMedium)
    val color=MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(120.dp).semantics { contentDescription="Gráfica de ${puntos.size} pesajes. Peso inicial ${primero.peso} kilogramos, último ${ultimo.peso} kilogramos. Los valores individuales están en el historial." }) {
        val min=puntos.minOf { it.peso };val rango=(puntos.maxOf { it.peso }-min).coerceAtLeast(1.0)
        val tiempo=(ultimo.fecha-primero.fecha).coerceAtLeast(1)
        val offsets=puntos.map { Offset(8f+(it.fecha-primero.fecha).toFloat()/tiempo*(size.width-16),size.height-8-((it.peso-min)/rango*(size.height-16)).toFloat()) }
        offsets.zipWithNext().forEach { (a,b) -> drawLine(color,a,b,4f) };offsets.forEach { drawCircle(color,6f,it) }
    }
}

@Composable private fun DialogoPesaje(id: String,vm: PanelRanchoViewModel,cerrar: () -> Unit) {
    var peso by rememberSaveable { mutableStateOf("") };var fecha by rememberSaveable { mutableStateOf(Validaciones.fechaTexto(System.currentTimeMillis())) };var notas by rememberSaveable { mutableStateOf("") };var error by remember { mutableStateOf<String?>(null) }
    FormularioDialogo("Pesaje de $id",cerrar,vm.guardando,error ?: vm.errorOperacion,operacion=vm) {
        CampoFormulario("Peso (kg)",peso,{peso=it},teclado=KeyboardType.Decimal)
        CampoFormulario("Fecha del pesaje",fecha,{fecha=it},ejemplo="dd/mm/aaaa")
        CampoFormulario("Notas",notas,{notas=it})
        Button(onClick={val p=Validaciones.decimal(peso);val f=Validaciones.fecha(fecha);if(p==null || p<=0 || f==null) error="Revisa peso y fecha" else {error=null;vm.pesar(id,f,p,notas)}},enabled=!vm.guardando,modifier=Modifier.fillMaxWidth()){Text("Guardar pesaje")}
    }
}
@Composable private fun DialogoGestacion(id: String,vm: PanelRanchoViewModel,cerrar: () -> Unit) {
    var fecha by rememberSaveable { mutableStateOf("") };var tipo by rememberSaveable { mutableStateOf("Monta Natural") };var error by remember { mutableStateOf<String?>(null) }
    FormularioDialogo("Gestación de $id",cerrar,vm.guardando,error ?: vm.errorOperacion,operacion=vm) {
        CampoFormulario("Fecha de monta o inseminación",fecha,{fecha=it},ejemplo="dd/mm/aaaa")
        SelectorFormulario("Método",tipo,listOf("Monta Natural","Inseminación"),{tipo=it})
        Text("Se mostrará una fecha estimada y una ventana orientativa.")
        Button(onClick={val f=Validaciones.fecha(fecha);if(f==null) error="Ingresa una fecha válida" else {error=null;vm.gestacion(id,f,tipo)}},enabled=!vm.guardando,modifier=Modifier.fillMaxWidth()){Text("Guardar gestación")}
    }
}
@Composable private fun DialogoParto(madre: Animal,g: Embarazo?,vm: PanelRanchoViewModel,cerrar: () -> Unit) {
    var fecha by rememberSaveable { mutableStateOf(Validaciones.fechaTexto(System.currentTimeMillis())) };var resultado by rememberSaveable { mutableStateOf("Crías vivas") }
    var cantidad by rememberSaveable { mutableStateOf("1") };var notas by rememberSaveable { mutableStateOf("") }
    val valores=remember { mutableStateMapOf<String,String>() };var error by remember { mutableStateOf<String?>(null) }
    FormularioDialogo("Parto de ${madre.idArete}",cerrar,vm.guardando,error ?: vm.errorOperacion,operacion=vm) {
        CampoFormulario("Fecha del parto",fecha,{fecha=it},ejemplo="dd/mm/aaaa")
        Text(if(g==null) "Se registrará sin una gestación previa." else "Al guardar se finalizará la gestación activa.")
        SelectorFormulario("Resultado",resultado,listOf("Crías vivas","Sin crías vivas"),{resultado=it})
        if(resultado=="Crías vivas") {
            SelectorFormulario("Número de crías",cantidad,listOf("1","2","3","4"),{cantidad=it})
            repeat(cantidad.toInt()) { i ->
                TituloSeccion("Cría ${i+1}")
                CampoFormulario("Arete de cría ${i+1}",valores["id$i"] ?: "",{valores["id$i"]=it})
                SelectorFormulario("Sexo de cría ${i+1}",valores["tipo$i"] ?: "Becerra",listOf("Becerra","Becerro"),{valores["tipo$i"]=it})
                CampoFormulario("Peso de cría ${i+1} (kg)",valores["peso$i"] ?: "",{valores["peso$i"]=it},teclado=KeyboardType.Decimal)
            }
        }
        CampoFormulario("Notas del parto",notas,{notas=it})
        Button(onClick={
            val f=Validaciones.fecha(fecha);val n=if(resultado=="Crías vivas") cantidad.toInt() else 0
            if(f==null || (0 until n).any { valores["id$it"].isNullOrBlank() || Validaciones.decimal(valores["peso$it"] ?: "")?.let { p -> p>0 }!=true }) error="Revisa fecha, aretes y pesos de las crías"
            else {error=null;vm.parto(madre.idArete,g?.id,f,resultado,notas,(0 until n).map { i -> Animal(idArete=valores["id$i"]!!.trim(),tipo=valores["tipo$i"] ?: "Becerra",peso=Validaciones.decimal(valores["peso$i"]!!)!!,fechaNacimiento=f,ubicacion="Sin Lote") })}
        },enabled=!vm.guardando,modifier=Modifier.fillMaxWidth()){Text("Guardar parto y crías")}
    }
}
