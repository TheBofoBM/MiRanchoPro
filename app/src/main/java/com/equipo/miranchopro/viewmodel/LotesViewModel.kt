package com.equipo.miranchopro.viewmodel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.modelovista.OperacionViewModel
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.data.repository.LoteRepository
import kotlinx.coroutines.flow.*

class LotesViewModel(private val repository: LoteRepository, private val puedeAdministrar: () -> Boolean = {false}) : OperacionViewModel() {
    private fun exigirPermiso() { require(puedeAdministrar()) { "Solo el dueño puede administrar lotes" } }
    val lotes = repository.obtenerTodosLosLotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun guardarNuevoLote(lote: Lote, alGuardar: () -> Unit = {}) = operar(alGuardar) { exigirPermiso(); repository.insertarLote(lote) }
    fun actualizarLote(lote: Lote, alGuardar: () -> Unit = {}) = operar(alGuardar) { exigirPermiso(); repository.actualizarLote(lote) }
    fun moverGanado(origen: Lote, destino: Lote, alGuardar: () -> Unit = {}) = operar(alGuardar) { exigirPermiso(); repository.mover(origen,destino) }
    fun validarTraslado(destino: Lote, cantidad: Int) = cantidad > 0 && destino.ocupacionActual.toLong() + cantidad <= destino.capacidadMaxima
    fun eliminarLote(lote: Lote, onResultado: (Boolean, String) -> Unit) = operar({ onResultado(true,"Lote eliminado") }) { exigirPermiso(); repository.eliminarLote(lote) }
}
