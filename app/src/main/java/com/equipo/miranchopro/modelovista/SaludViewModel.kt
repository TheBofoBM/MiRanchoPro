package com.equipo.miranchopro.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.data.repository.SaludRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SaludViewModel(
    private val repository: SaludRepository
) : OperacionViewModel() {

    val listaAnimales = repository.obtenerAnimales().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // MEDICAMENTOS
    val listaMedicamentos: StateFlow<List<Medicamento>> = repository.obtenerMedicamentos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarMedicamento(medicamento: Medicamento) {
        operar {
            repository.insertarMedicamento(medicamento)
        }
    }

    fun editarMedicamento(medicamento: Medicamento) {
        operar {
            repository.actualizarMedicamento(medicamento)
        }
    }

    fun eliminarMedicamento(medicamento: Medicamento) {
        operar {
            repository.eliminarMedicamento(medicamento)
        }
    }

    fun existeMedicamento(nombre: String, idAExcluir: String? = null): Boolean {
        return listaMedicamentos.value.any {
            it.nombre.trim().equals(nombre.trim(), ignoreCase = true) && it.id != idAExcluir
        }
    }

    // VACUNACIÓN
    val listaVacunaciones: StateFlow<List<Vacunacion>> = repository.obtenerVacunaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registrarVacunacion(vacunacion: Vacunacion) {
        operar {
            repository.registrarVacunacion(vacunacion)
        }
    }

    // ENFERMEDADES
    val listaEnfermedades: StateFlow<List<Enfermedad>> = repository.obtenerEnfermedades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registrarEnfermedad(enfermedad: Enfermedad) {
        operar {
            repository.registrarEnfermedad(enfermedad)
        }
    }

    fun actualizarEnfermedad(enfermedad: Enfermedad) {
        operar {
            repository.actualizarEnfermedad(enfermedad)
        }
    }

    fun actualizarEstadoEnfermedad(enfermedad: Enfermedad, nuevoEstado: String) {
        operar {
            repository.actualizarEnfermedad(enfermedad.copy(estado = nuevoEstado))
        }
    }
}
