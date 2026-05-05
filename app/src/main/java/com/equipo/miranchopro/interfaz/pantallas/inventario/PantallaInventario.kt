package com.equipo.miranchopro.interfaz.pantallas.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.modelovista.InventarioViewModel
import com.equipo.miranchopro.modelovista.VistaInventario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInventario(
    viewModel: InventarioViewModel = viewModel(),
    alSeleccionarAnimal: (String) -> Unit = {},
    alAgregarAnimal: () -> Unit = {}
) {
    // Manejar el botón atrás físico para volver a categorías
    BackHandler(enabled = viewModel.vistaActual == VistaInventario.DETALLE_CATEGORIA) {
        viewModel.volverACategorias()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (viewModel.vistaActual == VistaInventario.CATEGORIAS) "Inventario Ganadero" 
                        else "Categoría: ${viewModel.categoriaSeleccionada}"
                    ) 
                },
                navigationIcon = {
                    if (viewModel.vistaActual == VistaInventario.DETALLE_CATEGORIA) {
                        IconButton(onClick = { viewModel.volverACategorias() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = alAgregarAnimal,
                containerColor = Color(0xFF008577),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Animal")
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            if (viewModel.vistaActual == VistaInventario.CATEGORIAS) {
                SeccionCategorias(viewModel)
            } else {
                SeccionDetalleCategoria(viewModel, alSeleccionarAnimal)
            }
        }
    }
}

@Composable
fun SeccionCategorias(viewModel: InventarioViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Seleccione una categoría",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (viewModel.estaCargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (viewModel.categorias.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay animales registrados aún", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.categorias) { (nombre, cantidad) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.seleccionarCategoria(nombre) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "$cantidad animales", color = Color.Gray, fontSize = 14.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionDetalleCategoria(
    viewModel: InventarioViewModel,
    alSeleccionarAnimal: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de búsqueda dentro de la categoría
        OutlinedTextField(
            value = viewModel.busqueda,
            onValueChange = { viewModel.busqueda = it },
            placeholder = { Text("Buscar por tag o raza...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        if (viewModel.animalesFiltrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron animales", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.animalesFiltrados) { animal ->
                    TarjetaAnimal(animal, onClick = { alSeleccionarAnimal(animal.idArete) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaAnimal(animal: Animal, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "#${animal.idArete}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                EstadoBadge(animal.estado)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
            }
            
            Text(
                text = "${animal.tipo} • ${animal.raza}",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "Edad", value = animal.edad)
                InfoItem(label = "Peso", value = "${animal.peso} kg")
                InfoItem(label = "Ubicación", value = animal.ubicacion)
            }
        }
    }
}

@Composable
fun EstadoBadge(estado: String) {
    val colorBase = if (estado == "Sano") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val colorTexto = if (estado == "Sano") Color(0xFF2E7D32) else Color(0xFFEF6C00)
    
    Surface(
        color = colorBase,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = estado,
            color = colorTexto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
