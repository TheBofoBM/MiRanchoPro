package com.example.miranchopro.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equipo.miranchopro.data.model.Prioridad
import com.equipo.miranchopro.data.model.Tarea

@Composable
fun DialogoAsignarTarea(
    tareaExistente: Tarea? = null,
    onDismiss: () -> Unit,
    onConfirm: (Tarea) -> Unit
) {
    var titulo by remember { mutableStateOf(tareaExistente?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tareaExistente?.descripcion ?: "") }
    var responsable by remember { mutableStateOf(tareaExistente?.responsable ?: "Juan Pérez") }
    var prioridad by remember { mutableStateOf(tareaExistente?.prioridad ?: Prioridad.MEDIA) }
    var fecha by remember { mutableStateOf(tareaExistente?.fecha ?: "") }

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
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = responsable,
                    onValueChange = { responsable = it },
                    label = { Text("Responsable") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha (opcional)") },
                        placeholder = { Text("dd/mm/aaaa") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null) }
                    )
                }

                Button(
                    onClick = {
                        if (titulo.isNotBlank()) {
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
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Guardar Tarea") }
            }
        }
    }
}
