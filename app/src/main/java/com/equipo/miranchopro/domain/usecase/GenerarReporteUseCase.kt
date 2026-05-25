package com.equipo.miranchopro.domain.usecase

import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

data class ReporteInventario(
    val fechaGeneracion: String,
    val totalAnimales: Int,
    val pesoPromedio: Double,
    val totalPeso: Double,
    val conteoPorTipo: Map<String, Int>,
    val conteoPorRaza: Map<String, Int>,
    val conteoPorEstado: Map<String, Int>,
    val conteoPorUbicacion: Map<String, Int>,
    val conteoPorOrigen: Map<String, Int>,
    val animalesRecientes: List<Animal>
)

class GenerarReporteUseCase(private val repository: AnimalRepository) {

    suspend fun ejecutar(): ReporteInventario {
        // Obtenemos la lista actual de animales (usando first() para obtener el valor actual del Flow)
        val listaAnimales = repository.obtenerTodos().first()
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaHoy = sdf.format(Date())

        val pesoTotal = listaAnimales.sumOf { it.peso }
        val promedio = if (listaAnimales.isNotEmpty()) pesoTotal / listaAnimales.size else 0.0

        return ReporteInventario(
            fechaGeneracion = fechaHoy,
            totalAnimales = listaAnimales.size,
            pesoPromedio = promedio,
            totalPeso = pesoTotal,
            conteoPorTipo = listaAnimales.groupBy { it.tipo }.mapValues { it.value.size },
            conteoPorRaza = listaAnimales.groupBy { it.raza }.mapValues { it.value.size },
            conteoPorEstado = listaAnimales.groupBy { it.estado }.mapValues { it.value.size },
            conteoPorUbicacion = listaAnimales.groupBy { it.ubicacion }.mapValues { it.value.size },
            conteoPorOrigen = listaAnimales.groupBy { it.origen }.mapValues { it.value.size },
            // Ordenamos por fecha de registro (más recientes primero)
            animalesRecientes = listaAnimales.sortedByDescending { it.fechaRegistro }
        )
    }
}
