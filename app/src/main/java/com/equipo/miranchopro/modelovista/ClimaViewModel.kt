package com.equipo.miranchopro.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.api.WeatherApiService
import com.equipo.miranchopro.data.api.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClimaViewModel : ViewModel() {
    private val apiService = WeatherApiService.create()
    private val _climaState = MutableStateFlow<WeatherResponse?>(null)
    val climaState: StateFlow<WeatherResponse?> = _climaState

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Reemplazar con una API Key real de https://www.weatherapi.com/
    private val API_KEY = "6b0a375c2b3f4ca394f143012262505"

    fun obtenerClima(ciudad: String = "Xalapa") {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                val response = apiService.getForecast(API_KEY, ciudad)
                _climaState.value = response
            } catch (e: Exception) {
                _error.value = "Error al obtener el clima: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
