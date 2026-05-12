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

    fun cargarReporte() {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                _reporte.value = generarReporteUseCase.ejecutar()
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _estaCargando.value = false
            }
        }
    }
}
