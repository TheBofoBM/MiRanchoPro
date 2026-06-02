package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equipo.miranchopro.data.model.Medicamento
import java.util.UUID

@Composable
fun DialogoMedicamento(
    medicamentoExistente: Medicamento? = null,
    validarNombre: (String, String?) -> Boolean = { _, _ -> false },
    onDismiss: () -> Unit,
    onConfirm: (Medicamento) -> Unit
) {
    var nombre by remember { mutableStateOf(medicamentoExistente?.nombre ?: "") }
    var dosis by remember { mutableStateOf(medicamentoExistente?.dosis ?: "") }
    var stock by remember { mutableStateOf(medicamentoExistente?.stock?.toString() ?: "") }
    var unidadMedida by remember { mutableStateOf(medicamentoExistente?.unidadMedida ?: "") }
    var descripcion by remember { mutableStateOf(medicamentoExistente?.descripcion ?: "") }
    
    var errorNombre by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (medicamentoExistente == null) "Nuevo Medicamento" else "Editar Medicamento",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { 
                        nombre = it
                        errorNombre = null 
                    },
                    label = { Text("Nombre del medicamento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre != null,
                    supportingText = {
                        if (errorNombre != null) {
                            Text(text = errorNombre!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = dosis,
                    onValueChange = { dosis = it },
                    label = { Text("Dosis sugerida") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { if (it.all { char -> char.isDigit() }) stock = it },
                        label = { Text("Stock") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unidadMedida,
                        onValueChange = { unidadMedida = it },
                        label = { Text("Unidad") },
                        placeholder = { Text("ml, mg...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Notas") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )

                Button(
                    onClick = {
                        if (nombre.isNotBlank() && stock.isNotBlank()) {
                            if (validarNombre(nombre, medicamentoExistente?.id)) {
                                errorNombre = "Este medicamento ya existe"
                            } else {
                                onConfirm(
                                    Medicamento(
                                        id = medicamentoExistente?.id ?: UUID.randomUUID().toString(),
                                        nombre = nombre,
                                        dosis = dosis,
                                        stock = stock.toIntOrNull() ?: 0,
                                        unidadMedida = unidadMedida,
                                        descripcion = descripcion
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Guardar") }
            }
        }
    }
}
