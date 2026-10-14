package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equipo.miranchopro.modelovista.OperacionViewModel

@Composable
fun MensajeFormulario(mensaje: String?) {
    if (!mensaje.isNullOrBlank()) Text(mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun EstadoOperacion(viewModel: OperacionViewModel) {
    if (viewModel.guardando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    MensajeFormulario(viewModel.errorOperacion)
}

@Composable
fun FormularioDialogo(titulo: String, onDismiss: () -> Unit, guardando: Boolean = false, error: String? = null, contenido: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = { if (!guardando) onDismiss() }) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().heightIn(max = 640.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, enabled = !guardando) { Icon(Icons.Default.Close, "Cerrar formulario") }
                }
                if (guardando) LinearProgressIndicator(Modifier.fillMaxWidth())
                MensajeFormulario(error)
                contenido()
            }
        }
    }
}

@Composable
fun CampoFormulario(etiqueta: String, valor: String, cambiar: (String) -> Unit, error: String? = null, ejemplo: String = "", teclado: KeyboardType = KeyboardType.Text, enabled: Boolean = true, transformacion: VisualTransformation = VisualTransformation.None, enfocar: Boolean = false) {
    val foco = remember { FocusRequester() }
    LaunchedEffect(enfocar, error) { if (enfocar && error != null) foco.requestFocus() }
    OutlinedTextField(value = valor, onValueChange = cambiar, label = { Text(etiqueta) }, placeholder = { Text(ejemplo) },
        isError = error != null, supportingText = if (error != null) {{ Text(error) }} else null,
        modifier = Modifier.fillMaxWidth().focusRequester(foco), singleLine = true, enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = teclado), visualTransformation = transformacion)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFormulario(etiqueta: String, valor: String, opciones: List<String>, cambiar: (String) -> Unit, error: String? = null) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
        OutlinedTextField(value = valor, onValueChange = {}, readOnly = true, label = { Text(etiqueta) }, isError = error != null,
            supportingText = if (error != null) {{ Text(error) }} else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion -> DropdownMenuItem(text = { Text(opcion) }, onClick = { cambiar(opcion); expandido = false }) }
        }
    }
}

@Composable
fun ConfirmarEliminacion(nombre: String, cancelar: () -> Unit, confirmar: () -> Unit) {
    AlertDialog(onDismissRequest = cancelar, title = { Text("Confirmar eliminación") },
        text = { Text("Se eliminará $nombre. Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = confirmar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = cancelar) { Text("Cancelar") } })
}
