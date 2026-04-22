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
import com.equipo.miranchopro.modelovista.RegistrarAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarAnimal(
    viewModel: RegistrarAnimalViewModel = viewModel(),
    alFinalizar: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is RegistrarAnimalViewModel.EventoUI.Exito -> {
                    snackbarHostState.showSnackbar("Animal registrado con éxito")
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
