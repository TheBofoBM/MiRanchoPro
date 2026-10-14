package com.equipo.miranchopro.interfaz.pantallas.perfil
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.utils.*
import kotlinx.coroutines.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(navController: NavController, usuario: Usuario?) {
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    var cambiarClave by remember { mutableStateOf(false) };var mensaje by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) };var error by remember { mutableStateOf<String?>(null) }
    if(cambiarClave) {
        var actual by rememberSaveable { mutableStateOf("") };var nueva by rememberSaveable { mutableStateOf("") };var confirmar by rememberSaveable { mutableStateOf("") }
        FormularioDialogo("Cambiar contraseña",{cambiarClave=false},guardando,error) {
            CampoFormulario("Contraseña actual",actual,{actual=it},teclado=KeyboardType.Password,transformacion=PasswordVisualTransformation())
            CampoFormulario("Nueva contraseña",nueva,{nueva=it},teclado=KeyboardType.Password,transformacion=PasswordVisualTransformation())
            CampoFormulario("Confirmar contraseña",confirmar,{confirmar=it},teclado=KeyboardType.Password,transformacion=PasswordVisualTransformation())
            Button(onClick={
                error=when {nueva.length<6->"Usa al menos 6 caracteres";nueva!=confirmar->"Las contraseñas no coinciden";actual.isBlank()->"Ingresa la contraseña actual";else->null}
                if(error==null && usuario!=null && !guardando) {
                    guardando=true
                    scope.launch {
                        try {
                            val dao=RanchoDatabase.getDatabase(context).usuarioDao()
                            val cuenta=dao.buscarPorCorreo(usuario.correo) ?: throw IllegalArgumentException("La cuenta ya no existe")
                            require(withContext(Dispatchers.Default){Claves.verificar(actual,cuenta.contrasena)}){"La contraseña actual no es correcta"}
                            val hash=withContext(Dispatchers.Default){Claves.proteger(nueva)}
                            require(dao.cambiarClave(cuenta.correo,cuenta.contrasena,hash)==1){"La contraseña cambió; vuelve a intentarlo"}
                            cambiarClave=false;mensaje="Contraseña actualizada"
                        } catch(e: CancellationException){throw e} catch(e: Exception){error=Validaciones.mensaje(e)} finally{guardando=false}
                    }
                }
            },enabled=!guardando){Text("Guardar contraseña")}
        }
    }
    Scaffold(topBar={TopAppBar(title={Text("Mi perfil")},navigationIcon={IconButton(onClick={navController.popBackStack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Volver")}})}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            Text(usuario?.correo ?: "Sin sesión",style=MaterialTheme.typography.headlineSmall)
            Text(if(usuario?.rol?.equals("Administrador",true)==true) "Dueño / administrador" else "Empleado",style=MaterialTheme.typography.titleMedium)
            Text("Esta cuenta permite acceder al rancho guardado en este dispositivo.")
            mensaje?.let{Text(it)};Button(onClick={error=null;cambiarClave=true},enabled=usuario!=null){Text("Cambiar contraseña")}
        }
    }
}
