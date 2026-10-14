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
fun DialogoMedicamento(medicamentoExistente: Medicamento? = null, validarNombre: (String,String?) -> Boolean = {_,_->false},
    guardando: Boolean = false, errorOperacion: String? = null, onDismiss: () -> Unit, onConfirm: (Medicamento) -> Unit) {
    var nombre by rememberSaveable { mutableStateOf(medicamentoExistente?.nombre ?: "") }
    var dosis by rememberSaveable { mutableStateOf(medicamentoExistente?.dosis ?: "") }
    var stock by rememberSaveable { mutableStateOf(medicamentoExistente?.stock?.toString() ?: "") }
    var unidad by rememberSaveable { mutableStateOf(medicamentoExistente?.unidadMedida ?: "") }
    var notas by rememberSaveable { mutableStateOf(medicamentoExistente?.descripcion ?: "") }
    var intento by rememberSaveable { mutableStateOf(false) }
    val errorNombre = if (!intento) null else if (nombre.isBlank()) "Ingresa el nombre" else if (validarNombre(nombre, medicamentoExistente?.id)) "Este medicamento ya existe" else null
    val errorStock = if (intento && stock.toIntOrNull()?.let { it >= 0 } != true) "Ingresa un entero de 0 a 2147483647" else null
    val errorUnidad = if (intento && unidad.isBlank()) "Indica la unidad de existencias" else null
    FormularioDialogo(if(medicamentoExistente==null) "Nuevo medicamento" else "Editar medicamento", onDismiss, guardando, errorOperacion) {
        CampoFormulario("Nombre *",nombre,{nombre=it},errorNombre)
        CampoFormulario("Dosis indicada (opcional)",dosis,{dosis=it})
        CampoFormulario("Existencias *",stock,{stock=it},errorStock,teclado=KeyboardType.Number)
        CampoFormulario("Unidad *",unidad,{unidad=it},errorUnidad,"Ej. frascos o ml")
        CampoFormulario("Notas (opcional)",notas,{notas=it})
        Button(onClick={ intento=true
            if(nombre.isNotBlank() && !validarNombre(nombre,medicamentoExistente?.id) && stock.toIntOrNull()?.let{it>=0}==true && unidad.isNotBlank())
                onConfirm(Medicamento(id=medicamentoExistente?.id ?: java.util.UUID.randomUUID().toString(),nombre=nombre.trim(),dosis=dosis.trim(),stock=stock.toInt(),unidadMedida=unidad.trim(),descripcion=notas.trim()))
        },enabled=!guardando,modifier=Modifier.fillMaxWidth()){Text("Guardar")}
    }
}
