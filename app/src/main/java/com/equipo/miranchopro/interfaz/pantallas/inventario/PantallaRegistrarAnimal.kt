package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarAnimal(
    viewModel: RegistrarAnimalViewModel = viewModel(),
    alFinalizar: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is RegistrarAnimalViewModel.EventoUI.Exito -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                    // Opcional: Podrías navegar atrás después de un tiempo
                }
                is RegistrarAnimalViewModel.EventoUI.Error -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Registrar Nuevo Animal") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de Categoría (Tipo)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría (Tipo)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.tiposDisponibles.forEach { seleccion ->
                        DropdownMenuItem(
                            text = { Text(seleccion) },
                            onClick = {
                                viewModel.tipo = seleccion
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.idArete,
                onValueChange = { viewModel.idArete = it },
                label = { Text("ID Arete (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.mensajeError?.contains("arete", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = viewModel.peso,
                onValueChange = { viewModel.peso = it },
                label = { Text("Peso (kg) (Obligatorio)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.mensajeError?.contains("peso", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = viewModel.color,
                onValueChange = { viewModel.color = it },
                label = { Text("Color (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.mensajeError?.contains("color", ignoreCase = true) == true
            )

            OutlinedTextField(
                value = viewModel.marcas,
                onValueChange = { viewModel.marcas = it },
                label = { Text("Marcas o Señas Particulares") },
                modifier = Modifier.fillMaxWidth()
            )

            if (viewModel.mensajeError != null) {
                Text(
                    text = viewModel.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.registrarAnimal() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.estaCargando
            ) {
                if (viewModel.estaCargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar Animal")
                }
            }
        }
    }
}
