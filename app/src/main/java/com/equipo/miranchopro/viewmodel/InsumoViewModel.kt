package com.equipo.miranchopro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.InsumoDao
import com.equipo.miranchopro.data.model.Insumo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InsumoViewModel(private val insumoDao: InsumoDao) : ViewModel() {

    val listaInsumos: StateFlow<List<Insumo>> = insumoDao.obtenerTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarInsumo(nombre: String, tipo: String, cantidadInicial: Double, unidad: String) {
        viewModelScope.launch {
            val nuevo = Insumo(
                nombre = nombre,
                tipo = tipo,
                cantidad = cantidadInicial,
                unidadMedida = unidad
            )
            insumoDao.insertarInsumo(nuevo)
        }
    }

    fun registrarConsumo(insumo: Insumo, cantidadAGastar: Double) {
        viewModelScope.launch {
            if (insumo.cantidad >= cantidadAGastar) {
                insumoDao.descontarStock(insumo.id, cantidadAGastar)
            }
        }
    }

    fun abastecerInsumo(insumo: Insumo, cantidadAñadida: Double) {
        viewModelScope.launch {
            val actualizado = insumo.copy(cantidad = insumo.cantidad + cantidadAñadida)
            insumoDao.actualizarInsumo(actualizado)
        }
    }

    fun eliminarInsumo(insumo: Insumo) {
        viewModelScope.launch {
            insumoDao.eliminarInsumo(insumo)
        }
    }
}
