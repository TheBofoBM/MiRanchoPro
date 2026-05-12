package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                is EditarAnimalViewModel.EventoUI.BajaExitosa -> {
                    snackbarHostState.showSnackbar("Animal dado de baja")
                    alVolver()
                }
                is EditarAnimalViewModel.EventoUI.Error -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Animal") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.mostrarDialogoBaja = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Dar de baja", tint = Color.Red)
                    }
                }
            )
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ID Arete: ${viewModel.idArete}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = viewModel.peso,
                    onValueChange = { viewModel.peso = it },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.color,
                    onValueChange = { viewModel.color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.marcas,
                    onValueChange = { viewModel.marcas = it },
                    label = { Text("Marcas") },
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
                    onClick = { viewModel.actualizarAnimal() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.estaCargando,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.estaCargando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Actualizar Datos")
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.mostrarDialogoBaja = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dar de Baja")
                }
            }
        }
    }

    if (viewModel.mostrarDialogoBaja) {
        AlertDialog(
            onDismissRequest = { viewModel.mostrarDialogoBaja = false },
            title = { Text("Dar de baja animal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Seleccione el motivo de la baja:")

                    val motivos = listOf("Vendido", "Muerto", "Agregado accidentalmente")
                    motivos.forEach { motivo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.motivoBaja = motivo }
                        ) {
                            RadioButton(
                                selected = (viewModel.motivoBaja == motivo),
                                onClick = { viewModel.motivoBaja = motivo }
                            )
                            Text(motivo)
                        }
                    }

                    if (viewModel.motivoBaja == "Muerto") {
                        Text("Situación de la muerte:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        val situaciones = listOf("Medica", "Accidente", "Otro")
                        situaciones.forEach { situacion ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.situacionMuerte = situacion }
                            ) {
                                RadioButton(
                                    selected = (viewModel.situacionMuerte == situacion),
                                    onClick = { viewModel.situacionMuerte = situacion }
                                )
                                Text(situacion)
                            }
                        }

                        if (viewModel.situacionMuerte == "Otro") {
                            OutlinedTextField(
                                value = viewModel.otroMotivoMuerte,
                                onValueChange = { viewModel.otroMotivoMuerte = it },
                                label = { Text("Especifique") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmarBaja() },
                    enabled = viewModel.motivoBaja.isNotBlank() &&
                            (viewModel.motivoBaja != "Muerto" || viewModel.situacionMuerte.isNotBlank()),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Confirmar Baja")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.mostrarDialogoBaja = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
