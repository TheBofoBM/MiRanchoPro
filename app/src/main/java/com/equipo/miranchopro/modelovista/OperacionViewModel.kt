package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.utils.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

open class OperacionViewModel : ViewModel() {
    var guardando by mutableStateOf(false)
        private set
    var errorOperacion by mutableStateOf<String?>(null)
        private set
    var versionGuardado by mutableIntStateOf(0)
        private set
    fun limpiarError() { errorOperacion = null }
    protected fun operar(alTerminar: (() -> Unit)? = null, bloque: suspend () -> Unit) {
        if (guardando) return
        guardando = true
        errorOperacion = null
        viewModelScope.launch {
            try { bloque(); versionGuardado++; alTerminar?.invoke() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { errorOperacion = Validaciones.mensaje(e) }
            finally { guardando = false }
        }
    }
}
