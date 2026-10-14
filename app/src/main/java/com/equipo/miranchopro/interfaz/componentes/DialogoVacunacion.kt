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
fun DialogoVacunacion(animales: List<Animal> = emptyList(), guardando: Boolean = false, errorOperacion: String? = null,
    onDismiss: () -> Unit, onConfirm: (Vacunacion) -> Unit) {
    var idAnimal by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var detalle by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(Validaciones.fechaTexto(System.currentTimeMillis())) }
    var notas by rememberSaveable { mutableStateOf("") }
    var proxima by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }
    val ids=animales.filter{it.estado !in listOf("Baja","Pendiente")}.map{it.idArete}
    FormularioDialogo("Registrar vacunación",onDismiss,guardando,errorOperacion) {
        if(ids.isEmpty()) Text("Registra un animal activo antes de continuar.")
        SelectorFormulario("Animal / arete *",idAnimal,ids,{idAnimal=it},if(intento && idAnimal !in ids) "Selecciona un animal activo" else null)
        CampoFormulario("Vacuna *",nombre,{nombre=it},if(intento && nombre.isBlank()) "Ingresa el nombre" else null)
        CampoFormulario("Dosis y unidad *",detalle,{detalle=it},if(intento && detalle.isBlank()) "Completa este campo" else null)
        CampoFormulario("Fecha *",fecha,{fecha=it},if(intento) Validaciones.errorFecha(fecha,"La fecha") else null,"dd/mm/aaaa")
        CampoFormulario("Próxima aplicación (opcional)",proxima,{proxima=it},if(intento) Validaciones.errorFecha(proxima,"La próxima fecha",opcional=true,permitirFutura=true) ?: if(proxima.isNotBlank() && Validaciones.fecha(proxima)!=null && Validaciones.fecha(fecha)!=null && Validaciones.fecha(proxima)!!<Validaciones.fecha(fecha)!!) "Debe ser posterior o igual a la aplicación" else null else null,"dd/mm/aaaa")
        CampoFormulario("Notas (opcional)",notas,{notas=it})
        Button(onClick={intento=true
            val errorProxima = Validaciones.errorFecha(proxima,"La próxima fecha",opcional=true,permitirFutura=true)
            val orden = proxima.isBlank() || (Validaciones.fecha(proxima)!=null && Validaciones.fecha(fecha)!=null && Validaciones.fecha(proxima)!!>=Validaciones.fecha(fecha)!!)
            if(idAnimal in ids && nombre.isNotBlank() && detalle.isNotBlank() && Validaciones.errorFecha(fecha,"La fecha")==null && errorProxima==null && orden) onConfirm(Vacunacion(idAnimal=idAnimal,nombreVacuna=nombre.trim(),dosis=detalle.trim(),fechaAplicacion=fecha.trim(),proximaDosis=proxima.trim().ifBlank{null},notas=notas.trim()))
        },enabled=!guardando && ids.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Guardar")}
    }
}
