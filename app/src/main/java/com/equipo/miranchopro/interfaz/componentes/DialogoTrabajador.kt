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
fun DialogoTrabajador(trabajadorExistente: Usuario? = null, guardando: Boolean = false, errorOperacion: String? = null,
    onDismiss: () -> Unit, onConfirm: (String,String) -> Unit) {
    var correo by rememberSaveable { mutableStateOf(trabajadorExistente?.correo ?: "") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }
    val errorCorreo = if(intento && !Validaciones.correoValido(correo)) "Ingresa un correo válido" else null
    val errorClave = if(intento && (trabajadorExistente==null || contrasena.isNotEmpty()) && contrasena.length<6) "Usa al menos 6 caracteres" else null
    FormularioDialogo(if(trabajadorExistente==null) "Alta de empleado" else "Editar empleado",onDismiss,guardando,errorOperacion) {
        CampoFormulario("Correo *",correo,{correo=it},errorCorreo,teclado=KeyboardType.Email,enabled=trabajadorExistente==null)
        CampoFormulario(if(trabajadorExistente==null) "Contraseña *" else "Nueva contraseña (opcional)",contrasena,{contrasena=it},errorClave,teclado=KeyboardType.Password,transformacion=PasswordVisualTransformation())
        if(trabajadorExistente!=null) Text("Deja la contraseña vacía para conservar la actual.")
        Button(onClick={intento=true
            if(Validaciones.correoValido(correo) && ((trabajadorExistente!=null && contrasena.isEmpty()) || contrasena.length>=6)) onConfirm(Validaciones.correo(correo),contrasena)
        },enabled=!guardando,modifier=Modifier.fillMaxWidth()){Text("Guardar")}
    }
}
