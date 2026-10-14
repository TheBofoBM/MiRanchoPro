package com.equipo.miranchopro.modelovista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.api.WeatherApiService
import com.equipo.miranchopro.data.api.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClimaViewModel(private val context: android.content.Context? = null) : ViewModel() {
    private val apiService = WeatherApiService.create()
    private val _climaState = MutableStateFlow<WeatherResponse?>(null)
    val climaState: StateFlow<WeatherResponse?> = _climaState

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Reemplazar con una API Key real de https://www.weatherapi.com/
    private val API_KEY = "6b0a375c2b3f4ca394f143012262505"

    private val preferencias = context?.applicationContext?.getSharedPreferences("clima",android.content.Context.MODE_PRIVATE)
    val ciudadActual = MutableStateFlow(preferencias?.getString("ciudad","Xalapa") ?: "Xalapa")
    val actualizacion = MutableStateFlow<Long?>(null)
    fun obtenerClima(ciudad: String = ciudadActual.value) {
        if(_cargando.value) return
        val seleccion=ciudad.trim()
        if(seleccion.isBlank() || seleccion.length>100) { _error.value="Ingresa una ciudad de 1 a 100 caracteres";return }

        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                val response = apiService.getForecast(API_KEY, seleccion)
                _climaState.value = response
                ciudadActual.value=seleccion
                actualizacion.value=System.currentTimeMillis()
                preferencias?.edit()?.putString("ciudad",seleccion)?.apply()
            } catch (e: Exception) {
                if(e is kotlinx.coroutines.CancellationException) throw e
                _error.value = "No se pudo obtener el clima. Revisa la conexión y el nombre de la ciudad."
            } finally {
                _cargando.value = false
            }
        }
    }
}
