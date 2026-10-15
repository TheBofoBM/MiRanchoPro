package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equipo.miranchopro.data.model.Prioridad
import com.equipo.miranchopro.data.model.Tarea
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAsignarTarea(tareaExistente: Tarea? = null, listaTrabajadores: List<String> = emptyList(),
    guardando: Boolean = false, errorOperacion: String? = null, onDismiss: () -> Unit, onConfirm: (Tarea) -> Unit) {
    var titulo by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(tareaExistente?.titulo ?: "") }
    var descripcion by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(tareaExistente?.descripcion ?: "") }
    var responsable by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(tareaExistente?.responsable ?: "") }
    var prioridad by remember { mutableStateOf(tareaExistente?.prioridad ?: Prioridad.MEDIA) }
    var fecha by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(tareaExistente?.fecha ?: "") }
    var intento by remember { mutableStateOf(false) }
    var calendario by remember { mutableStateOf(false) }
    val datePickerState=rememberDatePickerState()
    if(calendario) DatePickerDialog(onDismissRequest={calendario=false},confirmButton={TextButton(onClick={
        datePickerState.selectedDateMillis?.let { fecha=SimpleDateFormat("dd/MM/yyyy",Locale.ROOT).apply{timeZone=TimeZone.getTimeZone("UTC")}.format(Date(it)) };calendario=false
    }){Text("Seleccionar")}},dismissButton={TextButton(onClick={calendario=false}){Text("Cancelar")}}){ DatePicker(state=datePickerState) }
    FormularioDialogo(if(tareaExistente==null) "Nueva tarea" else "Editar tarea",onDismiss,guardando,errorOperacion,borradorId=tareaExistente?.id?.toString() ?: "nueva") {
        CampoFormulario("Título *",titulo,{titulo=it},if(intento && titulo.isBlank()) "Ingresa el título" else null)
        CampoFormulario("Descripción *",descripcion,{descripcion=it},if(intento && descripcion.isBlank()) "Describe la tarea" else null)
        SelectorFormulario("Responsable *",responsable,listaTrabajadores.distinct(),{responsable=it},if(intento && responsable !in listaTrabajadores) "Selecciona un responsable activo" else null)
        SelectorFormulario("Prioridad",prioridad.etiqueta,Prioridad.entries.map{it.etiqueta},{valor->prioridad=Prioridad.entries.first{it.etiqueta==valor}})
        CampoFormulario("Fecha de tarea (opcional)",fecha,{fecha=it},ejemplo="dd/mm/aaaa")
        OutlinedButton(onClick={calendario=true},modifier=Modifier.fillMaxWidth()){Text(if(fecha.isBlank()) "Elegir fecha (opcional)" else "Fecha: $fecha")}
        if(fecha.isNotBlank()) TextButton(onClick={fecha=""}){Text("Quitar fecha")}
        Button(onClick={intento=true
            if(titulo.isNotBlank() && descripcion.isNotBlank() && responsable in listaTrabajadores)
                onConfirm(Tarea(id=tareaExistente?.id ?: 0,titulo=titulo.trim(),descripcion=descripcion.trim(),responsable=responsable,prioridad=prioridad,estaHecha=tareaExistente?.estaHecha ?: false,fecha=fecha.ifBlank{null}))
        },enabled=!guardando,modifier=Modifier.fillMaxWidth()){Text("Guardar tarea")}
    }
}

@Composable
fun BuscadorTrabajadoresDialog(
    trabajadores: List<String>,
    onDismiss: () -> Unit,
    onSeleccionar: (String) -> Unit
) {
    var filtro by remember { mutableStateOf("") }
    val listaFiltrada = trabajadores.filter { it.contains(filtro, ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Seleccionar Responsable", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = filtro,
                    onValueChange = { filtro = it },
                    label = { Text("Buscar empleado...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listaFiltrada) { trabajador ->
                        ListItem(
                            headlineContent = { Text(trabajador) },
                            modifier = Modifier.clickable { onSeleccionar(trabajador) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                    if (listaFiltrada.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron trabajadores",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Cerrar") }
            }
        }
    }
}
