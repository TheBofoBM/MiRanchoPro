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
import com.equipo.miranchopro.data.model.Vacunacion
import java.util.*

@Composable
fun DialogoVacunacion(
    onDismiss: () -> Unit,
    onConfirm: (Vacunacion) -> Unit
) {
    var nombreVacuna by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var idAnimal by remember { mutableStateOf("") }
    var fechaAplicacion by remember { mutableStateOf("") } // Idealmente un DatePicker
    var proximaDosis by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Registrar Vacunación",
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
                    value = nombreVacuna,
                    onValueChange = { nombreVacuna = it },
                    label = { Text("Nombre de la vacuna") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dosis,
                        onValueChange = { dosis = it },
                        label = { Text("Dosis") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fechaAplicacion,
                        onValueChange = { fechaAplicacion = it },
                        label = { Text("Fecha") },
                        placeholder = { Text("dd/mm/aaaa") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = proximaDosis,
                    onValueChange = { proximaDosis = it },
                    label = { Text("Próxima Dosis (Opcional)") },
                    placeholder = { Text("dd/mm/aaaa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )

                Button(
                    onClick = {
                        if (nombreVacuna.isNotBlank() && idAnimal.isNotBlank()) {
                            onConfirm(
                                Vacunacion(
                                    nombreVacuna = nombreVacuna,
                                    dosis = dosis,
                                    fechaAplicacion = fechaAplicacion,
                                    idAnimal = idAnimal,
                                    proximaDosis = proximaDosis.ifBlank { null },
                                    notas = notas
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Registrar") }
            }
        }
    }
}
