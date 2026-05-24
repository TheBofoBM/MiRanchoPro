package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equipo.miranchopro.data.model.Enfermedad

@Composable
fun DialogoEnfermedad(
    onDismiss: () -> Unit,
    onConfirm: (Enfermedad) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var sintomas by remember { mutableStateOf("") }
    var tratamiento by remember { mutableStateOf("") }
    var idAnimal by remember { mutableStateOf("") }
    var fechaDeteccion by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Reportar Enfermedad",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = idAnimal,
                    onValueChange = { idAnimal = it },
                    label = { Text("ID Animal (Arete)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la enfermedad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sintomas,
                    onValueChange = { sintomas = it },
                    label = { Text("Síntomas observados") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tratamiento,
                    onValueChange = { tratamiento = it },
                    label = { Text("Tratamiento inicial") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fechaDeteccion,
                    onValueChange = { fechaDeteccion = it },
                    label = { Text("Fecha de detección") },
                    placeholder = { Text("dd/mm/aaaa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (nombre.isNotBlank() && idAnimal.isNotBlank()) {
                            onConfirm(
                                Enfermedad(
                                    nombre = nombre,
                                    sintomas = sintomas,
                                    tratamiento = tratamiento,
                                    idAnimal = idAnimal,
                                    fechaDeteccion = fechaDeteccion,
                                    estado = "Activo"
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Guardar Reporte") }
            }
        }
    }
}
