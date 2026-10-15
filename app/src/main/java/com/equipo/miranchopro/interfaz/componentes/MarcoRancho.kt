package com.equipo.miranchopro.interfaz.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarcoRancho(titulo: String, volver: () -> Unit, acciones: @Composable RowScope.() -> Unit = {}, contenido: LazyListScope.() -> Unit) {
    Scaffold(topBar={TopAppBar(title={Text(titulo,maxLines=2)},navigationIcon={IconButton(onClick=volver){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Volver")}},actions=acciones)}) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).imePadding().testTag("contenido-rancho"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=contenido)
    }
}
@Composable fun TituloSeccion(texto: String) { Text(texto,style=MaterialTheme.typography.titleLarge,modifier=Modifier.semantics { heading() }) }
@Composable fun TarjetaDato(titulo: String,detalle: String) {
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(titulo,style=MaterialTheme.typography.titleMedium);Text(detalle,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    } }
}
