package com.example.miranchopro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miranchopro.viewmodel.EditAnimalViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAnimalScreen(
    idArete: String,
    viewModel: EditAnimalViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idArete) {
        viewModel.loadAnimal(idArete)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is EditAnimalViewModel.UiEvent.Success -> {
                    snackbarHostState.showSnackbar("Animal actualizado correctamente")
                }
                is EditAnimalViewModel.UiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editar Animal") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (viewModel.isLoading && viewModel.idArete.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
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
                    isError = viewModel.errorMessage != null && viewModel.peso.isBlank()
                )

                OutlinedTextField(
                    value = viewModel.color,
                    onValueChange = { viewModel.color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.errorMessage != null && viewModel.color.isBlank()
                )

                OutlinedTextField(
                    value = viewModel.marcas,
                    onValueChange = { viewModel.marcas = it },
                    label = { Text("Marcas") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (viewModel.errorMessage != null) {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.updateAnimal() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
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
