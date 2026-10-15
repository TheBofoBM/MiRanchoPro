package com.equipo.miranchopro.interfaz.pantallas.insumos

import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.utils.Validaciones
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.equipo.miranchopro.data.model.Insumo
import com.equipo.miranchopro.interfaz.componentes.DialogoInsumo
import com.equipo.miranchopro.interfaz.navegacion.Pantalla
import com.equipo.miranchopro.viewmodel.InsumoViewModel

@Composable
fun PantallaInsumos(
    navController: NavController,
    viewModel: InsumoViewModel
) {
    val insumos by viewModel.listaInsumos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var insumoAEditar by remember { mutableStateOf<Insumo?>(null) }
    var insumoAAbastecer by remember { mutableStateOf<Insumo?>(null) }
    var mostrarDialogoConsumo by remember { mutableStateOf<Insumo?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }

    LaunchedEffect(mostrarDialogo, mostrarDialogoConsumo?.id, insumoAAbastecer?.id) { viewModel.limpiarError() }
    LaunchedEffect(viewModel.versionGuardado) { if(viewModel.versionGuardado>0) {mostrarDialogo=false;insumoAEditar=null;mostrarDialogoConsumo=null;insumoAAbastecer=null} }
    var insumoAEliminar by remember { mutableStateOf<Insumo?>(null) }
    insumoAEliminar?.let { insumo -> ConfirmarEliminacion(insumo.nombre,{insumoAEliminar=null},{viewModel.eliminarInsumo(insumo);insumoAEliminar=null}) }
    if (mostrarDialogo) {
        DialogoInsumo(
            guardando=viewModel.guardando,errorOperacion=viewModel.errorOperacion,
            insumoExistente = insumoAEditar,
            onDismiss = {
                mostrarDialogo = false
                insumoAEditar = null
            },
            onConfirm = { nombre, tipo, cantidad, unidad ->
                val anterior = insumoAEditar
                if(anterior==null) viewModel.agregarInsumo(nombre,tipo,cantidad,unidad)
                else viewModel.guardar(anterior.copy(nombre=nombre,tipo=tipo,cantidad=cantidad,unidadMedida=unidad))
            }
        )
    }

    if (mostrarDialogoConsumo != null) {
        DialogoRegistrarConsumo(
            insumo = mostrarDialogoConsumo!!,
            guardando=viewModel.guardando,errorOperacion=viewModel.errorOperacion,
            onDismiss = { mostrarDialogoConsumo = null },
            onConfirm = { cantidad ->
                viewModel.registrarConsumo(mostrarDialogoConsumo!!, cantidad)
            }
        )
    }

    if (insumoAAbastecer != null) {
        DialogoAbastecerInsumo(
            insumo = insumoAAbastecer!!,
            guardando=viewModel.guardando,errorOperacion=viewModel.errorOperacion,
            onDismiss = { insumoAAbastecer = null },
            onConfirm = { cantidad ->
                viewModel.abastecerInsumo(insumoAAbastecer!!, cantidad)
            }
        )
    }

    Scaffold(
        snackbarHost={ MensajeFormulario(viewModel.errorOperacion) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    insumoAEditar = null
                    mostrarDialogo = true
                },
                containerColor = Color(0xFF008577),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp).padding(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(36.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Surface(
                color = Color(0xFF004D40),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Insumos",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Alimentación y forrajes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00BFA5)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(Icons.Default.AccountCircle, "Menú", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                onClick = {
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Perfil.ruta)
                                },
                                leadingIcon = { Icon(Icons.Default.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = {
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Configuracion.ruta)
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    menuExpandido = false
                                    navController.navigate(Pantalla.Login.ruta) {
                                        popUpTo(0)
                                    }
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }
                            )
                        }
                    }
                }
            }

            if (insumos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay insumos registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(insumos, key = { it.id }) { insumo ->
                        TarjetaInsumo(
                            insumo = insumo,
                            onRegistrarConsumo = { mostrarDialogoConsumo = insumo },
                            onAbastecer = { insumoAAbastecer = insumo },
                            onDelete = { insumoAEliminar = insumo },
                            onEdit = {insumoAEditar=insumo;mostrarDialogo=true}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaInsumo(
    insumo: Insumo,
    onRegistrarConsumo: () -> Unit,
    onAbastecer: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(insumo.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(insumo.tipo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${insumo.cantidad} ${insumo.unidadMedida}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (insumo.cantidad <= 10) Color.Red else Color.Black
                    )
                    if (insumo.cantidad <= 10) {
                        Text("¡Stock Bajo!", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit,"Editar insumo") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    TextButton(onClick = onRegistrarConsumo) {
                        Text("Gastar", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAbastecer,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008577)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Abastecer")
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoRegistrarConsumo(insumo: Insumo, guardando: Boolean = false, errorOperacion: String? = null, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    DialogoCantidadInsumo(insumo,false,guardando,errorOperacion,onDismiss,onConfirm)
}
@Composable
fun DialogoAbastecerInsumo(insumo: Insumo, guardando: Boolean = false, errorOperacion: String? = null, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    DialogoCantidadInsumo(insumo,true,guardando,errorOperacion,onDismiss,onConfirm)
}
@Composable
private fun DialogoCantidadInsumo(insumo: Insumo, sumar: Boolean, guardando: Boolean, errorOperacion: String?, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var cantidad by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var intento by remember { mutableStateOf(false) }
    val numero=Validaciones.decimal(cantidad)
    val error=if(intento && (numero==null || numero<=0)) "Ingresa una cantidad mayor que cero" else if(intento && !sumar && numero!=null && numero>insumo.cantidad) "No hay existencias suficientes" else null
    FormularioDialogo(if(sumar) "Abastecer insumo" else "Registrar consumo",onDismiss,guardando,errorOperacion,borradorId=insumo.id) {
        Text("${insumo.nombre}: ${insumo.cantidad} ${insumo.unidadMedida} disponibles")
        CampoFormulario("Cantidad en ${insumo.unidadMedida} *",cantidad,{cantidad=it},error,teclado=androidx.compose.ui.text.input.KeyboardType.Decimal)
        Button(onClick={intento=true; if(numero!=null && numero>0 && (sumar || numero<=insumo.cantidad)) onConfirm(numero)},enabled=!guardando,modifier=Modifier.fillMaxWidth()){Text(if(sumar) "Abastecer" else "Descontar")}
    }
}
