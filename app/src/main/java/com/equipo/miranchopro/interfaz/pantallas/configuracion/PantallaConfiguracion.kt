package com.equipo.miranchopro.interfaz.pantallas.configuracion
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.equipo.miranchopro.interfaz.tema.PreferenciasApp
import com.equipo.miranchopro.data.local.RecordatoriosRancho
import com.equipo.miranchopro.interfaz.componentes.SelectorFormulario
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(navController: NavController,dueno: Boolean = false) {
    val context=LocalContext.current;val tema by PreferenciasApp.tema.collectAsState()
    var activo by remember { mutableStateOf(RecordatoriosRancho.activo(context)) }
    var hora by remember { mutableIntStateOf(RecordatoriosRancho.hora(context)) }
    var dias by remember { mutableIntStateOf(RecordatoriosRancho.dias(context)) }
    var permitido by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val ciclo=LocalLifecycleOwner.current
    DisposableEffect(ciclo) { val observer=LifecycleEventObserver { _,event -> if(event==Lifecycle.Event.ON_RESUME) permitido=NotificationManagerCompat.from(context).areNotificationsEnabled() };ciclo.lifecycle.addObserver(observer);onDispose { ciclo.lifecycle.removeObserver(observer) } }
    val pedir=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido -> permitido=concedido;activo=concedido;RecordatoriosRancho.configurar(context,concedido,hora,dias) }
    Scaffold(topBar={TopAppBar(title={Text("Configuración")},navigationIcon={IconButton(onClick={navController.popBackStack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Volver")}})}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text("Apariencia",style=MaterialTheme.typography.titleLarge)
            listOf("sistema" to "Usar tema del teléfono","claro" to "Tema claro","oscuro" to "Tema oscuro").forEach { (valor,nombre) ->
                Row { RadioButton(selected=tema==valor,onClick={PreferenciasApp.elegir(context,valor)});TextButton(onClick={PreferenciasApp.elegir(context,valor)}){Text(nombre)} }
            }
            HorizontalDivider();Text("Recordatorios",style=MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                Text("Resumen diario de pendientes",modifier=Modifier.weight(1f))
                Switch(checked=activo && permitido,onCheckedChange={nuevo ->
                    if(nuevo && android.os.Build.VERSION.SDK_INT>=33 && !permitido) pedir.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    else {activo=nuevo;RecordatoriosRancho.configurar(context,nuevo,hora,dias)}
                })
            }
            SelectorFormulario("Hora aproximada",String.format(java.util.Locale.ROOT,"%02d:00",hora),(0..23).map { String.format(java.util.Locale.ROOT,"%02d:00",it) },{hora=it.substringBefore(":").toInt();RecordatoriosRancho.configurar(context,activo,hora,dias)})
            SelectorFormulario("Anticipación", if(dias==1) "1 día" else "$dias días",listOf("0 días","1 día","3 días","7 días","14 días","30 días"),{dias=it.substringBefore(" ").toInt();RecordatoriosRancho.configurar(context,activo,hora,dias)})
            Text("El resumen reúne tareas, próximas aplicaciones y partos. Android puede retrasarlo por ahorro de batería. Los pendientes también se consultan en el calendario.")
            if(!permitido) TextButton(onClick={context.startActivity(if(android.os.Build.VERSION.SDK_INT>=26) android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,context.packageName) else android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:${context.packageName}")))}){Text("Permitir notificaciones en Android")}
            if(dueno) { HorizontalDivider();Text("Datos del rancho",style=MaterialTheme.typography.titleLarge);OutlinedButton(onClick={navController.navigate(Pantalla.Respaldo.ruta)},modifier=Modifier.fillMaxWidth()){Text("Respaldo y restauración")} }
            OutlinedButton(onClick={navController.navigate(Pantalla.Ayuda.ruta)},modifier=Modifier.fillMaxWidth()){Text("Consultar guía de uso")}
            HorizontalDivider();Text("Información",style=MaterialTheme.typography.titleLarge)
            Text("Mi Rancho PRO · Versión 1.0");Text("Idioma: Español")
            Text("Consulta tus pendientes en Tareas y el seguimiento del ganado en Salud.")
        }
    }
}
