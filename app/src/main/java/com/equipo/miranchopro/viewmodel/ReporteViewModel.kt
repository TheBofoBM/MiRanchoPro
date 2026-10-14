package com.equipo.miranchopro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.domain.usecase.GenerarReporteUseCase
import com.equipo.miranchopro.domain.usecase.ReporteInventario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReporteViewModel(private val generarReporteUseCase: GenerarReporteUseCase) : ViewModel() {

    private val _reporte = MutableStateFlow<ReporteInventario?>(null)
    val reporte: StateFlow<ReporteInventario?> = _reporte

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    val error = MutableStateFlow<String?>(null)

    fun cargarReporte() {
        viewModelScope.launch {
            _estaCargando.value = true
            error.value = null
            try {
                _reporte.value = generarReporteUseCase.ejecutar()
            } catch (e: Exception) {
                error.value = "No se pudo cargar el reporte. Inténtalo de nuevo."
            } finally {
                _estaCargando.value = false
            }
        }
    }
}
