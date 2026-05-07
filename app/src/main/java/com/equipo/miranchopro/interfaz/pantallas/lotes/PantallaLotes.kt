package com.equipo.miranchopro.interfaz.pantallas.lotes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.viewmodel.LotesViewModel

// ─── Colores consistentes con LoginScreen ────────────────────────────────────

private val ColorBackground  = Color(0xFFFFFFFF)
private val ColorText        = Color(0xFF2C3E50)
private val ColorPrimary     = Color(0xFF0E8A5A)
private val ColorSubtext     = Color(0xFF95A5A6)
private val ColorLabel       = Color(0xFF7F8C8D)
private val ColorBadgeBg     = Color(0xFFE8F5E9)
private val ColorCardBg      = Color(0xFFF8F9FA)
private val ColorFieldBorder = Color(0xFFE0E0E0)
private val ColorDanger      = Color(0xFFD32F2F)
private val ColorDangerBg    = Color(0xFFFDECEC)
private val ColorWarning     = Color(0xFFF57C00)
private val ColorWarningBg   = Color(0xFFFFF3E0)

// ─── Pantalla principal ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLotes(
    viewModel: LotesViewModel,
    onAgregarLote: () -> Unit,
    onVerDetalle: (Int) -> Unit
) {
    val listaLotes by viewModel.lotes.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "fade_in"
    )

    Scaffold(
        containerColor = ColorBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gestión de Lotes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarLote,
                containerColor = ColorPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Nuevo Lote")
            }
        }
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .alpha(alpha)
        ) {
            // Resumen rápido en la parte superior
            if (listaLotes.isNotEmpty()) {
                ResumenLotes(listaLotes)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaLotes) { lote ->
                    TarjetaLote(
                        lote = lote,
                        onClick = { onVerDetalle(lote.id) } // Esto activará la navegación hacia tu nuevo diseño
                    )
                }
            }
        }
    }
}

// ─── Resumen superior ────────────────────────────────────────────────────────

@Composable
private fun ResumenLotes(lotes: List<Lote>) {
    val totalCabezas = lotes.sumOf { it.ocupacionActual }
    val totalCapacidad = lotes.sumOf { it.capacidadMaxima }
    val lotesLlenos = lotes.count { it.capacidadMaxima > 0 && (it.ocupacionActual.toFloat() / it.capacidadMaxima) >= 0.9f }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChipResumen(
            label = "Total cabezas",
            valor = "$totalCabezas / $totalCapacidad",
            bgColor = ColorBadgeBg,
            textColor = ColorPrimary,
            modifier = Modifier.weight(1f)
        )
        if (lotesLlenos > 0) {
            ChipResumen(
                label = "Lotes al límite",
                valor = "$lotesLlenos",
                bgColor = ColorDangerBg,
                textColor = ColorDanger,
                modifier = Modifier.weight(1f)
            )
        } else {
            ChipResumen(
                label = "Lotes activos",
                valor = "${lotes.size}",
                bgColor = ColorBadgeBg,
                textColor = ColorPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ChipResumen(
    label: String,
    valor: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = ColorLabel,
                letterSpacing = 0.5.sp
            )
            Text(
                text = valor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// ─── Tarjeta de lote ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaLote(lote: Lote, onClick: () -> Unit) {
    val progreso = if (lote.capacidadMaxima > 0) {
        lote.ocupacionActual.toFloat() / lote.capacidadMaxima
    } else {
        0f
    }

    val esCritico   = progreso >= 0.9f
    val esAdvertencia = progreso >= 0.7f && !esCritico

    val colorIndicador = when {
        esCritico     -> ColorDanger
        esAdvertencia -> ColorWarning
        else          -> ColorPrimary
    }

    val colorEtiquetaBg = when {
        esCritico     -> ColorDangerBg
        esAdvertencia -> ColorWarningBg
        else          -> ColorBadgeBg
    }

    val textoEstado = when {
        esCritico     -> "Lleno"
        esAdvertencia -> "Casi lleno"
        else          -> "Disponible"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorFieldBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Encabezado: ícono + nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorEtiquetaBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐄", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lote.nombre,
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ColorText,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Etiqueta de estado
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorEtiquetaBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(colorIndicador, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = textoEstado,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorIndicador
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conteo de cabezas
            Text(
                text = "${lote.ocupacionActual}/${lote.capacidadMaxima}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorText
            )
            Text(
                text = "cabezas",
                fontSize = 11.sp,
                color = ColorSubtext,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colorIndicador,
                trackColor = ColorFieldBorder
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(progreso * 100).toInt()}% ocupado",
                fontSize = 10.sp,
                color = ColorLabel
            )
        }
    }
}