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
    alAgregarAnimal: (String?) -> Unit = {}
) {
    BackHandler(enabled = viewModel.vistaActual != VistaInventario.CATEGORIAS) {
        viewModel.volverACategorias()
    }

    Scaffold(
        floatingActionButton = {
            // Solo mostramos el FAB si no estamos en la vista de bajas
            if (viewModel.vistaActual != VistaInventario.DADOS_DE_BAJA) {
                FloatingActionButton(
                    onClick = { alAgregarAnimal(viewModel.categoriaSeleccionada) },
                    containerColor = Color(0xFF008577),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp).padding(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(36.dp))
                }
            }
        }
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
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 40.dp)
                        .fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (viewModel.vistaActual != VistaInventario.CATEGORIAS) {
                            IconButton(
                                onClick = { viewModel.volverACategorias() },
                                modifier = Modifier.padding(end = 8.dp).size(48.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }
                        Text(
                            text = when (viewModel.vistaActual) {
                                VistaInventario.CATEGORIAS -> "Inventario"
                                VistaInventario.DADOS_DE_BAJA -> "Bajas"
                                VistaInventario.DETALLE_CATEGORIA -> viewModel.categoriaSeleccionada ?: "Lista"
                            },
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1.5).sp,
                            lineHeight = 52.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (viewModel.vistaActual == VistaInventario.CATEGORIAS) 
                            "${viewModel.listaAnimales.filter { it.estado != "Baja" }.size} animales registrados"
                        else 
                            "Explorando catálogo ganadero",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00BFA5)
                    )
                }
            }

            // CONTENIDO
            Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                when (viewModel.vistaActual) {
                    VistaInventario.CATEGORIAS -> SeccionCategorias(viewModel)
                    else -> SeccionListaAnimales(viewModel, alSeleccionarAnimal)
                }
            }
        }
    }
}

@Composable
fun SeccionCategorias(viewModel: InventarioViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SECCIONES DEL RANCHO",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 2.sp
            )
        }
        
        if (viewModel.estaCargando) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF008577))
                }
            }
        } else {
            items(viewModel.categorias) { (nombre, cantidad) ->
                CardCategoriaLujo(
                    titulo = nombre, 
                    subtitulo = "$cantidad animales", 
                    onClick = { viewModel.seleccionarCategoria(nombre) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                CardCategoriaLujo(
                    titulo = "Dados de Baja",
                    subtitulo = "Histórico",
                    colorFondo = Color(0xFFFFEBEE),
                    icono = Icons.Default.History,
                    colorIcono = Color.Red,
                    onClick = { viewModel.verBajas() }
                )
            }
        }
    }
}

@Composable
fun CardCategoriaLujo(
    titulo: String, 
    subtitulo: String, 
    colorFondo: Color = Color.White,
    icono: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Folder,
    colorIcono: Color = Color(0xFF008577),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colorIcono.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Text(text = subtitulo, color = Color.Gray, fontSize = 14.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionListaAnimales(
    viewModel: InventarioViewModel,
    alSeleccionarAnimal: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = viewModel.busqueda,
            onValueChange = { viewModel.busqueda = it },
            placeholder = { Text("Buscar por tag, nombre o raza...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFF008577),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val animales = viewModel.animalesFiltrados
            if (animales.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron animales", color = Color.Gray)
                    }
                }
            } else {
                items(animales) { animal ->
                    TarjetaAnimalLujo(animal, onClick = { alSeleccionarAnimal(animal.idArete) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaAnimalLujo(animal: Animal, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "#${animal.idArete}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(12.dp))
                EstadoBadgeLujo(animal.estado)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
            val subtitulo = if (animal.nombre.isNotBlank()) "${animal.nombre} • ${animal.tipo}" else animal.tipo
            Text(text = "$subtitulo • ${animal.raza}", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F3F4))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItemLujo(label = "PESO", value = "${animal.peso} kg")
                InfoItemLujo(label = "ORIGEN", value = animal.origen)
                InfoItemLujo(label = "LOTE", value = animal.ubicacion)
            }
        }
    }
}

@Composable
fun EstadoBadgeLujo(estado: String) {
    val colorBase = when(estado) {
        "Sano" -> Color(0xFFE8F5E9)
        "Baja" -> Color(0xFFFFEBEE)
        else -> Color(0xFFFFF3E0)
    }
    val colorTexto = when(estado) {
        "Sano" -> Color(0xFF2E7D32)
        "Baja" -> Color(0xFFC62828)
        else -> Color(0xFFEF6C00)
    }
    Surface(color = colorBase, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = estado.uppercase(), 
            color = colorTexto, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 11.sp, 
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoItemLujo(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF9AA0A6), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202124))
    }
}
