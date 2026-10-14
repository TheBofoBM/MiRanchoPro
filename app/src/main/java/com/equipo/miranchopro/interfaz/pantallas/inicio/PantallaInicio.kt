package com.equipo.miranchopro.interfaz.pantallas.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.equipo.miranchopro.data.api.ForecastDay
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.modelovista.InventarioViewModel
import com.equipo.miranchopro.modelovista.TareasViewModel
import com.equipo.miranchopro.modelovista.SaludViewModel
import com.equipo.miranchopro.modelovista.ClimaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaInicio(
    navController: NavController,
    inventarioViewModel: InventarioViewModel,
    saludViewModel: SaludViewModel,
    usuarioActual: Usuario? = null,
    tareasViewModel: TareasViewModel = viewModel(),
    climaViewModel: ClimaViewModel = viewModel()
) {
    val totalAnimales = inventarioViewModel.listaAnimales.count {it.estado !in listOf("Baja","Pendiente")}
    val todasLasTareas by tareasViewModel.listaTareas.collectAsState()

    // Filtrado de tareas según el rol para el resumen
    val tareasFiltradas = if (usuarioActual?.rol?.uppercase() == "TRABAJADOR") {
        todasLasTareas.filter { it.responsable == usuarioActual.correo }
    } else {
        todasLasTareas
    }
    val tareasPendientes = tareasFiltradas.count { !it.estaHecha }

    val medicamentos by saludViewModel.listaMedicamentos.collectAsState()
    val medicamentosBajoStock = medicamentos.count { it.stock < 5 }
    var menuExpandido by remember { mutableStateOf(false) }

    val climaResponse by climaViewModel.climaState.collectAsState()
    val cargandoClima by climaViewModel.cargando.collectAsState()
    val errorClima by climaViewModel.error.collectAsState()

    val ciudad by climaViewModel.ciudadActual.collectAsState()
    val actualizado by climaViewModel.actualizacion.collectAsState()
    var cambiarCiudad by remember { mutableStateOf(false) }
    if(cambiarCiudad) {
        var ciudadNueva by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(ciudad) }
        com.equipo.miranchopro.interfaz.componentes.FormularioDialogo("Ubicación del clima",{cambiarCiudad=false}) {
            com.equipo.miranchopro.interfaz.componentes.CampoFormulario("Ciudad",ciudadNueva,{ciudadNueva=it},ejemplo="Ej. Xalapa, México")
            Button(onClick={climaViewModel.obtenerClima(ciudadNueva);cambiarCiudad=false},enabled=ciudadNueva.isNotBlank() && ciudadNueva.trim().length<=100){Text("Consultar clima")}
        }
    }
    LaunchedEffect(Unit) {
        climaViewModel.obtenerClima()
    }

    val sugerenciaClima = remember(climaResponse) {
        val condicion = climaResponse?.forecast?.forecastday?.firstOrNull()?.day?.condition?.text?.lowercase() ?: ""
        when {
            condicion.contains("soleado") || condicion.contains("despejado") -> "El clima se presta para arrear becerros."
            condicion.contains("lluvia") || condicion.contains("llovizna") -> "Día lluvioso: Asegúrate de que el ganado joven esté bajo techo."
            condicion.contains("nublado") -> "Cielo nublado: Buen momento para revisar las cercas y el perímetro."
            condicion.contains("tormenta") -> "Alerta de tormenta: Revisa los niveles de agua y asegura los cobertizos."
            else -> "¡Buen día para trabajar en el rancho!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = Color(0xFF004D40),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mi Rancho",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRO",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00BFA5),
                            letterSpacing = (-1).sp
                        )
                    }
                    Text(
                        text = "Panel de Control",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { menuExpandido = true }) {
                        Icon(Icons.Default.AccountCircle, "Menú", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = { menuExpandido = false; navController.navigate(Pantalla.Perfil.ruta) },
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clima Detallado") },
                            onClick = { menuExpandido = false; navController.navigate(Pantalla.ClimaDetallado.ruta) },
                            leadingIcon = { Icon(Icons.Default.Cloud, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = { menuExpandido = false; navController.navigate(Pantalla.Configuracion.ruta) },
                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                menuExpandido = false
                                navController.navigate(Pantalla.Login.ruta) { popUpTo(0) }
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = "¡Hola, Bienvenido!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    if (!cargandoClima && climaResponse != null) {
                        Text(
                            text = sugerenciaClima,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item { Text("ESTADO ACTUAL", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardResumenInicio("Animales", totalAnimales.toString(), Icons.Default.Pets, Color(0xFF008577), Modifier.weight(1f)) { navController.navigate(Pantalla.Inventario.ruta) }
                    CardResumenInicio(
                        titulo = if (usuarioActual?.rol?.uppercase() == "TRABAJADOR") "Mis Pendientes" else "Tareas",
                        valor = tareasPendientes.toString(),
                        icono = Icons.Default.ListAlt,
                        colorIcono = Color(0xFFFFA000),
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate(Pantalla.Tareas.ruta) }
                }
            }

            if (medicamentosBajoStock > 0 && usuarioActual?.rol?.equals("Administrador", true) == true) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red); Spacer(modifier = Modifier.width(12.dp))
                            Text("$medicamentosBajoStock medicamentos con stock bajo", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("PRONÓSTICO DEL CLIMA (3 DÍAS)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                    Text(
                        "Ver más",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate(Pantalla.ClimaDetallado.ruta) }
                    )
                }
            }
            item {
                TextButton(onClick={cambiarCiudad=true}){Text("Ubicación del clima: $ciudad · Cambiar")}
                actualizado?.let { Text("Actualizado: "+SimpleDateFormat("dd/MM HH:mm",Locale.getDefault()).format(Date(it)),style=MaterialTheme.typography.bodySmall) }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Pantalla.ClimaDetallado.ruta) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    if (cargandoClima) {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (errorClima != null) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Column { Text(errorClima!!, color = MaterialTheme.colorScheme.error);TextButton(onClick={climaViewModel.obtenerClima()}){Text("Reintentar")} }
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val pronostico = climaResponse?.forecast?.forecastday ?: emptyList()
                            pronostico.forEachIndexed { index, dia ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ItemClimaApi(dia)
                                }
                                if (index < pronostico.size - 1) {
                                    VerticalDivider(
                                        modifier = Modifier.height(40.dp),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Text("ACCESO RÁPIDO", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemAccesoRapido("Inventario de Ganado", "Ver y registrar animales", Icons.Default.Agriculture, Color(0xFF4CAF50)) { navController.navigate(Pantalla.Inventario.ruta) }
                    if (usuarioActual?.rol?.equals("Administrador", true) == true) {
                        ItemAccesoRapido("Insumos y Alimento", "Control de stock y forraje", Icons.Default.Inventory2, Color(0xFF008577)) { navController.navigate(Pantalla.Insumos.ruta) }
                    }
                    ItemAccesoRapido("Salud Animal", "Control médico y stock", Icons.Default.MedicalServices, Color(0xFF2196F3)) { navController.navigate(Pantalla.Salud.ruta) }
                    ItemAccesoRapido("Gestión de Lotes", "Ubicación de animales", Icons.Default.GridView, Color(0xFF9C27B0)) { navController.navigate(Pantalla.Lotes.ruta) }
                    ItemAccesoRapido("Reportes", "Estadísticas del rancho", Icons.Default.Assessment, Color(0xFF607D8B)) { navController.navigate(Pantalla.Reportes.ruta) }
                }
            }
        }
    }
}

@Composable
fun ItemClimaApi(forecast: ForecastDay) {
    val diaNombre = remember(forecast.date) {
        try {
            val formatInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = formatInput.parse(forecast.date)
            val today = Calendar.getInstance()
            val forecastCal = Calendar.getInstance().apply { time = date!! }

            if (today.get(Calendar.DAY_OF_YEAR) == forecastCal.get(Calendar.DAY_OF_YEAR)) {
                "Hoy"
            } else {
                val formatOutput = SimpleDateFormat("EEE", Locale("es", "MX"))
                formatOutput.format(date).replaceFirstChar { it.uppercase() }
            }
        } catch (e: Exception) {
            forecast.date
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = diaNombre, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            model = "https:${forecast.day.condition.icon}",
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${forecast.day.avgTempC.toInt()}°C", fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(text = forecast.day.condition.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
fun CardResumenInicio(titulo: String, valor: String, icono: ImageVector, colorIcono: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(36.dp).background(colorIcono.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(titulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ItemAccesoRapido(titulo: String, subtitulo: String, icono: ImageVector, color: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(icono, null, tint = color, modifier = Modifier.size(22.dp)) }
            Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp) ; Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}
