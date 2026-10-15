package com.equipo.miranchopro.interfaz.pantallas.configuracion

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.modelovista.RespaldoViewModel
import com.equipo.miranchopro.utils.Validaciones

@Composable
fun PantallaRespaldo(vm: RespaldoViewModel,dueno: Boolean,volver: () -> Unit,alRestaurar: () -> Unit) {
    var clave by remember { mutableStateOf("") };var repetir by remember { mutableStateOf("") };var actual by remember { mutableStateOf("") };var uri by remember { mutableStateOf<Uri?>(null) };var error by remember { mutableStateOf<String?>(null) }
    val crear=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { destino -> if(destino!=null) vm.exportar(destino,clave) }
    val abrir=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { origen -> uri=origen;vm.cancelar() }
    LaunchedEffect(vm.restaurado) { if(vm.restaurado) { clave="";actual="";alRestaurar() } }
    MarcoRancho("Respaldo y restauración",volver) {
        item { TarjetaDato("Conserva tus registros","El archivo incluye cuentas, ganado, salud, tareas, historiales y fotografías. Se cifra con la contraseña del respaldo. Si la pierdes, no podremos abrirlo. Límite actual: 64 MB por archivo.") }
        item { CampoFormulario("Contraseña del respaldo",clave,{clave=it},teclado=KeyboardType.Password,transformacion=PasswordVisualTransformation()) }
        if(dueno) {
            item { CampoFormulario("Repetir contraseña para crear respaldo",repetir,{repetir=it},teclado=KeyboardType.Password,transformacion=PasswordVisualTransformation()) }
            item { Button(onClick={if(clave.length<12 || clave!=repetir) error="Usa al menos 12 caracteres y repite la misma contraseña" else {error=null;crear.launch("MiRancho-${Validaciones.fechaTexto(System.currentTimeMillis()).replace('/','-')}.mrp")}},enabled=!vm.guardando,modifier=Modifier.fillMaxWidth()){Text("Crear y guardar respaldo")} }
        }
        item { HorizontalDivider();TituloSeccion("Recuperar un respaldo") }
        item { OutlinedButton(onClick={abrir.launch(arrayOf("*/*"))},enabled=!vm.guardando,modifier=Modifier.fillMaxWidth()){Text(if(uri==null) "Seleccionar archivo .mrp" else "Cambiar archivo seleccionado")} }
        if(uri!=null) item { Button(onClick={error=null;vm.revisar(uri!!,clave)},enabled=!vm.guardando && clave.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("Comprobar respaldo")} }
        vm.revision?.let { r ->
            item { TarjetaDato("Respaldo del ${Validaciones.fechaTexto(r.fecha)}","${r.animales} animales · ${r.fotos} fotografías\nAl restaurar se reemplazarán los registros de este teléfono. Después deberás entrar con una cuenta del respaldo.") }
            if(dueno) item { CampoFormulario("Tu contraseña actual para autorizar",actual,{actual=it},teclado=KeyboardType.Password,transformacion=PasswordVisualTransformation()) }
            item { Button(onClick={vm.restaurar(actual)},enabled=!vm.guardando && (!dueno || actual.isNotBlank()),modifier=Modifier.fillMaxWidth()){Text("Confirmar reemplazo y restaurar")};TextButton(onClick={vm.cancelar()},enabled=!vm.guardando){Text("Cancelar restauración")} }
        }
        item { EstadoOperacion(vm);MensajeFormulario(error);vm.mensaje?.let { Text(it) } }
    }
}
