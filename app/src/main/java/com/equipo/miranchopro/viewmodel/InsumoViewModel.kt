package com.equipo.miranchopro.viewmodel
import com.equipo.miranchopro.modelovista.OperacionViewModel
import com.equipo.miranchopro.data.local.dao.InsumoDao
import com.equipo.miranchopro.data.model.Insumo
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class InsumoViewModel(private val insumoDao: InsumoDao) : OperacionViewModel() {
    val listaInsumos = insumoDao.obtenerTodos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun agregarInsumo(nombre: String, tipo: String, cantidadInicial: Double, unidad: String) = guardar(Insumo(nombre=nombre, tipo=tipo, cantidad=cantidadInicial, unidadMedida=unidad))
    fun guardar(insumo: Insumo) = operar {
        require(insumo.nombre.isNotBlank() && insumo.unidadMedida.isNotBlank()) { "Nombre y unidad son obligatorios" }
        require(insumo.cantidad.isFinite() && insumo.cantidad >= 0) { "La cantidad debe ser un número mayor o igual que cero" }
        insumoDao.insertarInsumo(insumo.copy(nombre=insumo.nombre.trim(), unidadMedida=insumo.unidadMedida.trim()))
    }
    fun registrarConsumo(insumo: Insumo, cantidadAGastar: Double) = operar {
        require(cantidadAGastar.isFinite() && cantidadAGastar > 0) { "La cantidad debe ser mayor que cero" }
        require(insumoDao.descontarStock(insumo.id, cantidadAGastar) == 1) { "No hay existencias suficientes o el insumo ya no existe" }
    }
    fun abastecerInsumo(insumo: Insumo, cantidadAñadida: Double) = operar {
        require(cantidadAñadida.isFinite() && cantidadAñadida > 0) { "La cantidad debe ser mayor que cero" }
        require(insumoDao.sumarStock(insumo.id, cantidadAñadida) == 1) { "No se pudo actualizar la cantidad; revisa el insumo y el valor" }
    }
    fun eliminarInsumo(insumo: Insumo) = operar { insumoDao.eliminarInsumo(insumo) }
}
