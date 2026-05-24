package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equipo.miranchopro.data.model.Usuario

@Composable
fun DialogoTrabajador(
    trabajadorExistente: Usuario? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var correo by remember { mutableStateOf(trabajadorExistente?.correo ?: "") }
    var contrasena by remember { mutableStateOf(trabajadorExistente?.contrasena ?: "") }
    var verContrasena by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (trabajadorExistente == null) "Alta de Trabajador" else "Modificar Trabajador",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = correo,
                    onValueChange = { 
                        correo = it
                        errorCorreo = null 
                    },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = trabajadorExistente == null, // El correo es PK, no se edita
                    isError = errorCorreo != null,
                    supportingText = {
                        if (errorCorreo != null) {
                            Text(text = errorCorreo!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verContrasena = !verContrasena }) {
                            Icon(if (verContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    }
                )

                Button(
                    onClick = {
                        if (correo.isNotBlank() && contrasena.isNotBlank()) {
                            onConfirm(correo, contrasena)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(8.dp)
                ) { 
                    Text(if (trabajadorExistente == null) "Registrar" else "Guardar Cambios") 
                }
            }
        }
    }
}
