package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.modelovista.EditarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarAnimal(
    idArete: String,
    viewModel: EditarAnimalViewModel = viewModel(),
    alVolver: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idArete) {
        viewModel.cargarAnimal(idArete)
    }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is EditarAnimalViewModel.EventoUI.Exito -> {
                    snackbarHostState.showSnackbar("Animal actualizado correctamente")
                }
                is EditarAnimalViewModel.EventoUI.Error -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editar Animal") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        if (viewModel.estaCargando && viewModel.idArete.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(relleno)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "ID Arete: ${viewModel.idArete}", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = viewModel.peso,
                    onValueChange = { viewModel.peso = it },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.mensajeError != null && viewModel.peso.isBlank()
                )

                OutlinedTextField(
                    value = viewModel.color,
                    onValueChange = { viewModel.color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.mensajeError != null && viewModel.color.isBlank()
                )

                OutlinedTextField(
                    value = viewModel.marcas,
                    onValueChange = { viewModel.marcas = it },
                    label = { Text("Marcas") },
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
                    onClick = { viewModel.actualizarAnimal() },
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
                        Text("Actualizar")
                    }
                }
            }
        }
    }
}
