package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(bottom = relleno.calculateBottomPadding())
        ) {
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 12.dp, top = 4.dp, end = 24.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = alFinalizar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                    Column {
                        Text(
                            text = if (viewModel.horaNacimientoRegistrada != null) "Nacimiento" else "Nuevo Animal",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Formulario de registro",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00BFA5)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                if (viewModel.horaNacimientoRegistrada != null) {
                    Surface(
                        color = Color(0xFFE0F2F1),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = Color(0xFF008577))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Hora capturada", fontSize = 12.sp, color = Color(0xFF00695C))
                                Text(viewModel.horaNacimientoRegistrada!!, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                            }
                        }
                    }
                }

                Text("Tag / Identificador (Arete)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.idArete,
                    onValueChange = { viewModel.idArete = it },
                    placeholder = { Text("Ej: A-102", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Nombre (Opcional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.nombre,
                    onValueChange = { viewModel.nombre = it },
                    placeholder = { Text("Ej: Lucero", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Raza", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.raza,
                            onValueChange = { viewModel.raza = it },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Peso (kg)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.peso,
                            onValueChange = { viewModel.peso = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.registrarAnimal() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008577)),
                    enabled = !viewModel.estaCargando
                ) {
                    if (viewModel.estaCargando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Guardar Registro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
