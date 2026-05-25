package com.equipo.miranchopro.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.data.repository.SaludRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SaludViewModel(
    private val repository: SaludRepository
) : ViewModel() {

    // MEDICAMENTOS
    val listaMedicamentos: StateFlow<List<Medicamento>> = repository.obtenerMedicamentos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarMedicamento(medicamento: Medicamento) {
        viewModelScope.launch {
            repository.insertarMedicamento(medicamento)
        }
    }

    fun editarMedicamento(medicamento: Medicamento) {
        viewModelScope.launch {
            repository.actualizarMedicamento(medicamento)
        }
    }

    fun eliminarMedicamento(medicamento: Medicamento) {
        viewModelScope.launch {
            repository.eliminarMedicamento(medicamento)
        }
    }

    suspend fun existeMedicamento(nombre: String, idAExcluir: String? = null): Boolean {
        return listaMedicamentos.value.any { 
            it.nombre.equals(nombre, ignoreCase = true) && it.id != idAExcluir 
        }
    }

    // VACUNACIÓN
    val listaVacunaciones: StateFlow<List<Vacunacion>> = repository.obtenerVacunaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registrarVacunacion(vacunacion: Vacunacion) {
        viewModelScope.launch {
            repository.registrarVacunacion(vacunacion)
        }
    }

    // ENFERMEDADES
    val listaEnfermedades: StateFlow<List<Enfermedad>> = repository.obtenerEnfermedades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registrarEnfermedad(enfermedad: Enfermedad) {
        viewModelScope.launch {
            repository.registrarEnfermedad(enfermedad)
        }
    }

    fun actualizarEnfermedad(enfermedad: Enfermedad) {
        viewModelScope.launch {
            repository.actualizarEnfermedad(enfermedad)
        }
    }

    fun actualizarEstadoEnfermedad(enfermedad: Enfermedad, nuevoEstado: String) {
        viewModelScope.launch {
            repository.actualizarEnfermedad(enfermedad.copy(estado = nuevoEstado))
        }
    }
}
