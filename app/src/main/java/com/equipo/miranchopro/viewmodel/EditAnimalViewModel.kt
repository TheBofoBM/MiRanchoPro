package com.equipo.miranchopro.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EditAnimalViewModel(
    private val repository: AnimalRepository = AnimalRepository()
) : ViewModel() {

    var idArete by mutableStateOf("")
        private set

    var peso by mutableStateOf("")
    var color by mutableStateOf("")
    var marcas by mutableStateOf("")

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        object Success : UiEvent()
        data class Error(val message: String) : UiEvent()
    }

    fun loadAnimal(id: String) {
        viewModelScope.launch {
            isLoading = true
            val animal = repository.getAnimalById(id)
            if (animal != null) {
                idArete = animal.idArete
                peso = animal.peso.toString()
                color = animal.color
                marcas = animal.marcas
            }
            isLoading = false
        }
    }

    fun updateAnimal() {
        if (peso.isBlank() || color.isBlank()) {
            errorMessage = "El peso y el color son obligatorios"
            return
        }

        val pesoDouble = peso.toDoubleOrNull()
        if (pesoDouble == null) {
            errorMessage = "El peso debe ser un número válido"
            return
        }

        errorMessage = null
        viewModelScope.launch {
            isLoading = true
            val updatedAnimal = Animal(
                idArete = idArete,
                peso = pesoDouble,
                color = color,
                marcas = marcas
            )
            val success = repository.updateAnimal(updatedAnimal)
            isLoading = false
            if (success) {
                _uiEvent.emit(UiEvent.Success)
            } else {
                _uiEvent.emit(UiEvent.Error("Error al actualizar el animal"))
            }
        }
    }

    fun onErrorMessageDismissed() {
        errorMessage = null
    }
}
