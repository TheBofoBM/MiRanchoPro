package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    var mostrarMenuTipo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventoUI.collectLatest { evento ->
            when (evento) {
                is RegistrarAnimalViewModel.EventoUI.Exito -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                    alFinalizar()
                }
                is RegistrarAnimalViewModel.EventoUI.Error -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Nuevo Animal") },
                navigationIcon = {
                    IconButton(onClick = alFinalizar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Información del Animal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = viewModel.idArete,
                onValueChange = { viewModel.idArete = it },
                label = { Text("ID Arete (Tag)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de Tipo
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Animal") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarMenuTipo = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar tipo")
                        }
                    }
                )
                DropdownMenu(
                    expanded = mostrarMenuTipo,
                    onDismissRequest = { mostrarMenuTipo = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    viewModel.tiposDisponibles.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                viewModel.tipo = tipo
                                mostrarMenuTipo = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.peso,
                onValueChange = { viewModel.peso = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.edad,
                onValueChange = { viewModel.edad = it },
                label = { Text("Edad (ej. 2 años, 6 meses)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.color,
                onValueChange = { viewModel.color = it },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.ubicacion,
                onValueChange = { viewModel.ubicacion = it },
                label = { Text("Asignar a Lote (Opcional)") },
                placeholder = { Text("Ej. Lote A") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.marcas,
                onValueChange = { viewModel.marcas = it },
                label = { Text("Marcas Particulares") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !viewModel.estaCargando,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.estaCargando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar Animal")
                }
            }
        }
    }
}