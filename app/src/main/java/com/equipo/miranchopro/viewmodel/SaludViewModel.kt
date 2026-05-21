package com.equipo.miranchopro.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.RegistroSalud
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.SaludRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SaludViewModel(
    private val saludRepo: SaludRepository,
    private val animalRepo: AnimalRepository
) : ViewModel() {

    private val _registros = MutableStateFlow<List<RegistroSalud>>(emptyList())
    val registros: StateFlow<List<RegistroSalud>> = _registros

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            saludRepo.obtenerTodos().collect { lista ->
                _registros.value = lista
            }
        }
    }
}