package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.equipo.miranchopro.data.model.Medicamento

class MedicamentoViewModel : ViewModel() {
    private val _listaMedicamentos = mutableStateListOf<Medicamento>(
        Medicamento(nombre = "Ivermectina", dosis = "1ml / 50kg", stock = 10, unidadMedida = "Frascos"),
        Medicamento(nombre = "Vitamina ADE", dosis = "5ml", stock = 5, unidadMedida = "Frascos"),
        Medicamento(nombre = "Oxitetraciclina", dosis = "1ml / 10kg", stock = 8, unidadMedida = "Frascos")
    )
    val listaMedicamentos: List<Medicamento> get() = _listaMedicamentos

    fun agregarMedicamento(medicamento: Medicamento) {
        _listaMedicamentos.add(medicamento)
    }

    fun editarMedicamento(medicamento: Medicamento) {
        val index = _listaMedicamentos.indexOfFirst { it.id == medicamento.id }
        if (index != -1) {
            _listaMedicamentos[index] = medicamento
        }
    }

    fun eliminarMedicamento(medicamento: Medicamento) {
        _listaMedicamentos.remove(medicamento)
    }
}
