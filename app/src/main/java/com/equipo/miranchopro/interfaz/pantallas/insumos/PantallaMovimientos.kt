package com.equipo.miranchopro.interfaz.pantallas.insumos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.Validaciones
import java.util.Locale

@Composable
fun PantallaMovimientos(nav: NavController,vm: PanelRanchoViewModel) {
    val movimientos by vm.movimientos.collectAsState();val insumos by vm.insumos.collectAsState();val medicamentos by vm.medicamentos.collectAsState()
    var clase by rememberSaveable { mutableStateOf("Todos") };var producto by rememberSaveable { mutableStateOf("Todos los productos") };var dialogo by rememberSaveable { mutableStateOf("") }
    val opciones=(insumos.map { "Insumo · ${it.nombre}" }+medicamentos.map { "Medicamento · ${it.nombre}" }).sorted()
    LaunchedEffect(vm.versionGuardado) { if(vm.versionGuardado>0) dialogo="" };LaunchedEffect(dialogo) { vm.limpiarError() }
    if(dialogo.isNotBlank()) {
        var seleccion by rememberSaveable { mutableStateOf(opciones.firstOrNull() ?: "") };var cantidad by rememberSaveable { mutableStateOf("") };var tipo by rememberSaveable { mutableStateOf("Entrada") }
        var motivo by rememberSaveable { mutableStateOf("") };var minimo by rememberSaveable { mutableStateOf("") };var caduca by rememberSaveable { mutableStateOf("") };var error by remember { mutableStateOf<String?>(null) }
        val insumo=insumos.find { "Insumo · ${it.nombre}"==seleccion };val medicamento=medicamentos.find { "Medicamento · ${it.nombre}"==seleccion }
        LaunchedEffect(seleccion) { minimo=(insumo?.stockMinimo ?: medicamento?.stockMinimo ?: 0).toString();caduca=Validaciones.fechaTexto(insumo?.fechaCaducidad ?: medicamento?.fechaCaducidad) }
        FormularioDialogo(if(dialogo=="movimiento") "Movimiento de existencias" else "Alertas del producto",{dialogo=""},vm.guardando,error ?: vm.errorOperacion,operacion=vm) {
            SelectorFormulario("Producto",seleccion,opciones,{seleccion=it})
            CompositionLocalProvider(LocalFormularioBorrador provides (LocalFormularioBorrador.current+seleccion+"|")) {
            if(dialogo=="movimiento") {
                SelectorFormulario("Movimiento",tipo,listOf("Entrada","Consumo"),{tipo=it})
                CampoFormulario("Cantidad",cantidad,{cantidad=it},ejemplo=insumo?.unidadMedida ?: medicamento?.unidadMedida ?: "",teclado=KeyboardType.Decimal)
                CampoFormulario("Motivo",motivo,{motivo=it})
            } else {
                CampoFormulario("Existencias mínimas",minimo,{minimo=it},teclado=KeyboardType.Decimal)
                CampoFormulario("Caducidad (opcional)",caduca,{caduca=it},ejemplo="dd/mm/aaaa")
            }
            }
            Button(onClick={
                if(dialogo=="movimiento") {
                    val numero=Validaciones.decimal(cantidad)
                    if(numero==null || numero<=0 || motivo.isBlank() || (medicamento!=null && cantidad.toIntOrNull()==null)) error="Revisa cantidad, producto y motivo; medicamentos requiere un entero"
                    else { error=null;if(medicamento!=null) vm.moverMedicamento(medicamento.id,cantidad.toInt(),tipo=="Entrada",motivo) else if(insumo!=null) vm.moverInsumo(insumo.id,numero,tipo=="Entrada",motivo) }
                } else {
                    val numero=Validaciones.decimal(minimo)
                    if(numero==null || numero<0 || Validaciones.errorFecha(caduca,"La caducidad",opcional=true,permitirFutura=true)!=null || (medicamento!=null && minimo.toIntOrNull()==null)) error="Revisa mínimo y caducidad"
                    else {error=null;if(insumo!=null) vm.configurarInsumo(insumo.copy(stockMinimo=numero,fechaCaducidad=Validaciones.fecha(caduca))) else if(medicamento!=null) vm.configurarMedicamento(medicamento.copy(stockMinimo=minimo.toInt(),fechaCaducidad=Validaciones.fecha(caduca)))}
                }
            },enabled=!vm.guardando && opciones.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Guardar")}
        }
    }
    MarcoRancho("Movimientos de inventario",{nav.popBackStack()}) {
        val hoy=com.equipo.miranchopro.utils.AgendaRancho.inicioDia(System.currentTimeMillis())
        val alertasInsumos=insumos.filter { it.cantidad<=it.stockMinimo || it.fechaCaducidad?.let { f -> f<=hoy }==true }
        val alertasMedicamentos=medicamentos.filter { it.stock<=it.stockMinimo || it.fechaCaducidad?.let { f -> f<=hoy }==true }
        if(alertasInsumos.isNotEmpty() || alertasMedicamentos.isNotEmpty()) item { TituloSeccion("Productos por revisar") }
        items(alertasInsumos,key={"alerta-i-${it.id}"}) { TarjetaDato(it.nombre,"Existencias: ${it.cantidad} ${it.unidadMedida} · Mínimo: ${it.stockMinimo}\nCaducidad: ${Validaciones.fechaTexto(it.fechaCaducidad).ifBlank { "Sin registrar" }}") }
        items(alertasMedicamentos,key={"alerta-m-${it.id}"}) { TarjetaDato(it.nombre,"Existencias: ${it.stock} ${it.unidadMedida} · Mínimo: ${it.stockMinimo}\nCaducidad: ${Validaciones.fechaTexto(it.fechaCaducidad).ifBlank { "Sin registrar" }}") }
        item { Button(onClick={dialogo="movimiento"},enabled=opciones.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Registrar entrada o consumo")};OutlinedButton(onClick={dialogo="alertas"},enabled=opciones.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Configurar mínimos y caducidad")} }
        item { SelectorFormulario("Clase de producto",clase,listOf("Todos","Insumo","Medicamento"),{clase=it});SelectorFormulario("Filtrar producto",producto,listOf("Todos los productos")+movimientos.map { "${it.clase} · ${it.nombre}" }.distinct().sorted(),{producto=it}) }
        item { EstadoOperacion(vm) }
        val filtrados=movimientos.filter { (clase=="Todos" || it.clase==clase) && (producto=="Todos los productos" || "${it.clase} · ${it.nombre}"==producto) }
        if(filtrados.isEmpty()) item { TarjetaDato("Sin movimientos","Registra productos en Insumos o Salud para comenzar.") }
        items(filtrados,key={it.id}) { m -> TarjetaDato("${m.tipo} · ${m.nombre}","${Validaciones.fechaTexto(m.fecha)} · ${String.format(Locale.getDefault(),"%+.2f",m.cantidad)} ${m.unidad}\nSaldo: ${m.saldo} ${m.unidad}\n${m.motivo}\nResponsable: ${m.responsable.ifBlank { "Registro anterior" }}") }
    }
}
