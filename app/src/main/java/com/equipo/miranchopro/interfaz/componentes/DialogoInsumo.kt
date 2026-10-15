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
fun DialogoInsumo(insumoExistente: Insumo? = null, guardando: Boolean = false, errorOperacion: String? = null,
    onDismiss: () -> Unit, onConfirm: (String,String,Double,String) -> Unit) {
    var nombre by rememberSaveable { mutableStateOf(insumoExistente?.nombre ?: "") }
    var tipo by rememberSaveable { mutableStateOf(insumoExistente?.tipo ?: "Alimento") }
    var cantidad by rememberSaveable { mutableStateOf(insumoExistente?.cantidad?.toString() ?: "") }
    var unidad by rememberSaveable { mutableStateOf(insumoExistente?.unidadMedida ?: "kg") }
    var intento by rememberSaveable { mutableStateOf(false) }
    FormularioDialogo(if(insumoExistente==null) "Nuevo insumo" else "Editar insumo",onDismiss,guardando,errorOperacion,borradorId=insumoExistente?.id ?: "nuevo") {
        CampoFormulario("Nombre *",nombre,{nombre=it},if(intento && nombre.isBlank()) "Ingresa el nombre" else null)
        SelectorFormulario("Tipo",tipo,listOf("Alimento","Forraje","Suplemento","Otro"),{tipo=it})
        CampoFormulario("Cantidad *",cantidad,{cantidad=it},if(intento && Validaciones.decimal(cantidad)?.let{it>=0}!=true) "Ingresa una cantidad mayor o igual que cero" else null,teclado=KeyboardType.Decimal)
        CampoFormulario("Unidad *",unidad,{unidad=it},if(intento && unidad.isBlank()) "Indica la unidad" else null,"kg, bultos…")
        Button(onClick={intento=true
            val numero=Validaciones.decimal(cantidad)
            if(nombre.isNotBlank() && unidad.isNotBlank() && numero!=null && numero>=0) onConfirm(nombre.trim(),tipo,numero,unidad.trim())
        },enabled=!guardando,modifier=Modifier.fillMaxWidth()){Text("Guardar insumo")}
    }
}
