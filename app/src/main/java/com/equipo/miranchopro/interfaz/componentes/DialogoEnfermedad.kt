package com.equipo.miranchopro.interfaz.componentes
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.equipo.miranchopro.utils.Validaciones
import com.equipo.miranchopro.data.model.*

@Composable
fun DialogoEnfermedad(animales: List<Animal> = emptyList(), guardando: Boolean = false, errorOperacion: String? = null,
    onDismiss: () -> Unit, onConfirm: (Enfermedad) -> Unit) {
    var idAnimal by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var detalle by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(Validaciones.fechaTexto(System.currentTimeMillis())) }
    var notas by rememberSaveable { mutableStateOf("") }
    var tratamiento by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }
    val ids=animales.filter{it.estado !in listOf("Baja","Pendiente")}.map{it.idArete}
    FormularioDialogo("Reportar enfermedad",onDismiss,guardando,errorOperacion) {
        if(ids.isEmpty()) Text("Registra un animal activo antes de continuar.")
        SelectorFormulario("Animal / arete *",idAnimal,ids,{idAnimal=it},if(intento && idAnimal !in ids) "Selecciona un animal activo" else null)
        CampoFormulario("Enfermedad *",nombre,{nombre=it},if(intento && nombre.isBlank()) "Ingresa el nombre" else null)
        CampoFormulario("Síntomas observados *",detalle,{detalle=it},if(intento && detalle.isBlank()) "Completa este campo" else null)
        CampoFormulario("Fecha *",fecha,{fecha=it},if(intento) Validaciones.errorFecha(fecha,"La fecha") else null,"dd/mm/aaaa")
        CampoFormulario("Tratamiento indicado (opcional)",tratamiento,{tratamiento=it})
        CampoFormulario("Notas (opcional)",notas,{notas=it})
        Button(onClick={intento=true
            val errorProxima: String? = null; val orden=true
            if(idAnimal in ids && nombre.isNotBlank() && detalle.isNotBlank() && Validaciones.errorFecha(fecha,"La fecha")==null && errorProxima==null && orden) onConfirm(Enfermedad(idAnimal=idAnimal,nombre=nombre.trim(),sintomas=detalle.trim(),fechaDeteccion=fecha.trim(),tratamiento=tratamiento.trim(),notas=notas.trim(),estado="Activo"))
        },enabled=!guardando && ids.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Guardar")}
    }
}
