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
fun DialogoAsignarTarea(
    tareaExistente: Tarea? = null,
    listaTrabajadores: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Tarea) -> Unit
) {
    var titulo by remember { mutableStateOf(tareaExistente?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tareaExistente?.descripcion ?: "") }
    var responsable by remember { mutableStateOf(tareaExistente?.responsable ?: "") }
    var prioridad by remember { mutableStateOf(tareaExistente?.prioridad ?: Prioridad.MEDIA) }
    var fecha by remember { mutableStateOf(tareaExistente?.fecha ?: "") }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarBuscadorTrabajadores by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Validación de campos obligatorios
    val tituloError = titulo.isBlank()
    val descripcionError = descripcion.isBlank()
    val responsableError = responsable.isBlank()
    val esFormularioValido = !tituloError && !descripcionError && !responsableError

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        fecha = formatter.format(Date(it))
                    }
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarBuscadorTrabajadores) {
        BuscadorTrabajadoresDialog(
            trabajadores = listaTrabajadores,
            onDismiss = { mostrarBuscadorTrabajadores = false },
            onSeleccionar = {
                responsable = it
                mostrarBuscadorTrabajadores = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (tareaExistente == null) "Asignar Tarea" else "Editar Tarea",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la tarea") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = tituloError,
                    supportingText = { if (tituloError) Text("El título es obligatorio", color = MaterialTheme.colorScheme.error) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    isError = descripcionError,
                    supportingText = { if (descripcionError) Text("La descripción es obligatoria", color = MaterialTheme.colorScheme.error) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth().clickable { mostrarBuscadorTrabajadores = true }) {
                    OutlinedTextField(
                        value = responsable,
                        onValueChange = {},
                        label = { Text("Responsable") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        isError = responsableError,
                        trailingIcon = { Icon(Icons.Default.Search, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = if (responsableError) MaterialTheme.colorScheme.error else Color.LightGray,
                            disabledLabelColor = if (responsableError) MaterialTheme.colorScheme.error else Color.Gray,
                            disabledTrailingIconColor = Color.Gray
                        ),
                        supportingText = { if (responsableError) Text("El responsable es obligatorio", color = MaterialTheme.colorScheme.error) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f).clickable { 
                        prioridad = when(prioridad) {
                            Prioridad.BAJA -> Prioridad.MEDIA
                            Prioridad.MEDIA -> Prioridad.ALTA
                            Prioridad.ALTA -> Prioridad.BAJA
                        }
                    }) {
                        OutlinedTextField(
                            value = prioridad.etiqueta,
                            onValueChange = {},
                            label = { Text("Prioridad") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray
                            )
                        )
                    }
                    Box(modifier = Modifier.weight(1f).clickable { mostrarDatePicker = true }) {
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            label = { Text("Fecha (opcional)") },
                            placeholder = { Text("dd/mm/aaaa") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray,
                                disabledTrailingIconColor = Color.Gray
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (esFormularioValido) {
                            onConfirm(
                                Tarea(
                                    id = tareaExistente?.id ?: 0,
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    responsable = responsable,
                                    prioridad = prioridad,
                                    estaHecha = tareaExistente?.estaHecha ?: false,
                                    fecha = fecha.ifBlank { null }
                                )
                            )
                        }
                    },
                    enabled = esFormularioValido,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { 
                    Text("Guardar Tarea") 
                }
            }
        }
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
            color = Color.White
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
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                    if (listaFiltrada.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron trabajadores",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
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
