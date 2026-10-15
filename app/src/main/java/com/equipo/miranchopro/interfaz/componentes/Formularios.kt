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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import com.equipo.miranchopro.modelovista.OperacionViewModel

@Composable
fun MensajeFormulario(mensaje: String?) {
    if (!mensaje.isNullOrBlank()) Text(mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun EstadoOperacion(viewModel: OperacionViewModel) {
    if (viewModel.guardando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    MensajeFormulario(viewModel.errorOperacion)
}

@Composable
fun FormularioDialogo(titulo: String, onDismiss: () -> Unit, guardando: Boolean = false, error: String? = null, borradorId: String = "", operacion: OperacionViewModel? = LocalOperacionBorrador.current, contenido: @Composable ColumnScope.() -> Unit) {
    val context=LocalContext.current
    val clave="${LocalRutaBorrador.current}|$titulo|$borradorId|"
    var intento by remember { mutableStateOf(false) }
    val errorActual by rememberUpdatedState(error)
    val versionInicial=remember(clave,operacion) { operacion?.versionGuardado ?: 0 }
    LaunchedEffect(guardando) { if(guardando) intento=true }
    DisposableEffect(clave,operacion) { onDispose { if((operacion!=null && operacion.versionGuardado>versionInicial) || (operacion==null && intento && errorActual==null)) Borradores.borrar(context,clave) } }
    Dialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(Modifier.fillMaxWidth().heightIn(max = 640.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismiss, enabled = !guardando) { Icon(Icons.Default.Close, "Cerrar formulario") }
                    }
                    if (guardando) LinearProgressIndicator(Modifier.fillMaxWidth())
                    MensajeFormulario(error)
                    CompositionLocalProvider(LocalFormularioBorrador provides clave) { contenido() }
                    Text("El borrador se conserva al cerrar este formulario.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick={Borradores.borrar(context,clave);onDismiss()},enabled=!guardando) { Text("Descartar borrador") }
                }
            }
        }
    }
}

@Composable
fun CampoFormulario(etiqueta: String, valor: String, cambiar: (String) -> Unit, error: String? = null, ejemplo: String = "", teclado: KeyboardType = KeyboardType.Text, enabled: Boolean = true, transformacion: VisualTransformation = VisualTransformation.None, enfocar: Boolean = false) {
    val context=LocalContext.current
    val clave=LocalFormularioBorrador.current?.takeIf { transformacion==VisualTransformation.None && teclado!=KeyboardType.Password }?.plus(etiqueta)
    LaunchedEffect(clave) { clave?.let { Borradores.leer(context,it)?.let(cambiar) } }
    val foco = remember { FocusRequester() }
    LaunchedEffect(enfocar, error) { if (enfocar && error != null) foco.requestFocus() }
    OutlinedTextField(value = valor, onValueChange = { clave?.let { c -> Borradores.guardar(context,c,it) };cambiar(it) }, label = { Text(etiqueta) }, placeholder = { Text(ejemplo) },
        isError = error != null, supportingText = if (error != null) {{ Text(error) }} else null,
        modifier = Modifier.fillMaxWidth().focusRequester(foco), singleLine = true, enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = teclado), visualTransformation = transformacion)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFormulario(etiqueta: String, valor: String, opciones: List<String>, cambiar: (String) -> Unit, error: String? = null) {
    val context=LocalContext.current
    val clave=LocalFormularioBorrador.current?.plus(etiqueta)
    LaunchedEffect(clave,opciones) { clave?.let { Borradores.leer(context,it)?.takeIf { v -> v in opciones }?.let(cambiar) } }
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
        OutlinedTextField(value = valor, onValueChange = {}, readOnly = true, label = { Text(etiqueta) }, isError = error != null,
            supportingText = if (error != null) {{ Text(error) }} else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion -> DropdownMenuItem(text = { Text(opcion) }, onClick = { clave?.let { Borradores.guardar(context,it,opcion) };cambiar(opcion); expandido = false }) }
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
