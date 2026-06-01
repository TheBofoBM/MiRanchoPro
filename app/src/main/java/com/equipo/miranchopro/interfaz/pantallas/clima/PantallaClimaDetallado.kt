package com.equipo.miranchopro.interfaz.pantallas.clima

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.equipo.miranchopro.data.api.ForecastDay
import com.equipo.miranchopro.data.api.Hour
import com.equipo.miranchopro.modelovista.ClimaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaClimaDetallado(
    navController: NavController,
    climaViewModel: ClimaViewModel
) {
    val climaResponse by climaViewModel.climaState.collectAsState()
    val cargandoClima by climaViewModel.cargando.collectAsState()
    val errorClima by climaViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        if (climaResponse == null) {
            climaViewModel.obtenerClima()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pronóstico Detallado", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(padding)
        ) {
            if (cargandoClima) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF008577)
                )
            } else if (errorClima != null) {
                Text(
                    text = errorClima!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    climaResponse?.forecast?.forecastday?.let { days ->
                        items(days) { day ->
                            PronosticoDiaCard(day)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PronosticoDiaCard(day: ForecastDay) {
    val diaNombre = remember(day.date) {
        try {
            val formatInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = formatInput.parse(day.date)
            val today = Calendar.getInstance()
            val forecastCal = Calendar.getInstance().apply { 
                if (date != null) time = date 
            }
            
            if (date != null && 
                today.get(Calendar.DAY_OF_YEAR) == forecastCal.get(Calendar.DAY_OF_YEAR) &&
                today.get(Calendar.YEAR) == forecastCal.get(Calendar.YEAR)) {
                "Hoy"
            } else if (date != null) {
                val formatOutput = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
                formatOutput.format(date).replaceFirstChar { char -> char.uppercase() }
            } else {
                day.date
            }
        } catch (e: Exception) {
            day.date
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = diaNombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = day.day.condition.text, color = Color.Gray, fontSize = 14.sp)
                }
                AsyncImage(
                    model = "https:${day.day.condition.icon}",
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Pronóstico por hora",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(day.hour) { hour ->
                    ItemHoraClima(hour)
                }
            }
        }
    }
}

@Composable
fun ItemHoraClima(hour: Hour) {
    val horaTexto = remember(hour.time) {
        try {
            val formatInput = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = formatInput.parse(hour.time)
            val formatOutput = SimpleDateFormat("HH:mm", Locale.getDefault())
            if (date != null) formatOutput.format(date) else hour.time.split(" ").lastOrNull() ?: ""
        } catch (e: Exception) {
            hour.time.split(" ").lastOrNull() ?: ""
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFFF1F3F4), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .width(55.dp)
    ) {
        Text(text = horaTexto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            model = "https:${hour.condition.icon}",
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${hour.tempC.toInt()}°", fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}
