package com.equipo.miranchopro.interfaz

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.navigation.compose.rememberNavController
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.interfaz.componentes.*
import com.equipo.miranchopro.interfaz.pantallas.inventario.*
import com.equipo.miranchopro.modelovista.PanelRanchoViewModel
import com.equipo.miranchopro.utils.Validaciones
import com.equipo.miranchopro.interfaz.tema.TemaMiRanchoPro
import com.equipo.miranchopro.interfaz.pantallas.inicio.PrioridadesInicio
import com.equipo.miranchopro.interfaz.pantallas.tareas.PantallaAgenda
import com.equipo.miranchopro.interfaz.pantallas.reportes.PantallaEstadisticas
import java.io.File
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.cancel
import androidx.lifecycle.viewModelScope
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujosPrioridadesTest {
    @get:Rule val compose=createComposeRule()
    private val contexto=ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: RanchoDatabase
    private var panel: PanelRanchoViewModel?=null
    @Before fun abrir()=runBlocking {
        db=Room.inMemoryDatabaseBuilder(contexto,RanchoDatabase::class.java).build()
        db.animalDao().insertar(Animal(idArete="UI-001",nombre="Luna",tipo="Vaca",peso=400.0,fechaNacimiento=Validaciones.fecha("01/01/2020")))
    }
    @After fun cerrar() { panel?.viewModelScope?.cancel();db.close();Borradores.borrar(contexto,"prueba-ui") }
    @Test fun fichaPermiteRegistrarPesajeYLoMuestraEnHistorial() {
        val vm=PanelRanchoViewModel(db) { "dueño-prueba" }
        panel=vm
        compose.setContent { TemaMiRanchoPro(oscuro=true) { CompositionLocalProvider(LocalRutaBorrador provides "prueba-ui-ficha") { PantallaFichaAnimal("UI-001",rememberNavController(),vm) } } }
        compose.waitUntil(10000) { vm.animales.value.isNotEmpty() }
        compose.onNodeWithText("Registrar pesaje").performScrollTo().performClick()
        compose.onNodeWithText("Peso (kg)").performTextInput("450")
        compose.onNodeWithText("Guardar pesaje").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.pesajes.value.any { it.peso==450.0 } }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Guardar pesaje").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("contenido-rancho").performScrollToNode(hasText("Evolución del peso"))
        compose.onNodeWithText("Evolución del peso").assertIsDisplayed()
        capturar("ficha-historial-oscuro.png")
        assertEquals("dueño-prueba",vm.pesajes.value.single().responsable)
        compose.onNodeWithTag("contenido-rancho").performScrollToNode(hasText("Registrar pesaje"))
        compose.onNodeWithText("Registrar pesaje").performClick()
        val campo=compose.onNode(hasSetTextAction() and hasText("Peso (kg)")).fetchSemanticsNode()
        assertEquals("",campo.config[androidx.compose.ui.semantics.SemanticsProperties.EditableText].text)
    }
    @Test fun busquedaDeGanadoEncuentraPorNombre() {
        val vm=PanelRanchoViewModel(db) { "dueño-prueba" }
        panel=vm
        compose.setContent { TemaMiRanchoPro(oscuro=true) { PantallaGanado(rememberNavController(),vm) } }
        compose.waitUntil(10000) { vm.animales.value.isNotEmpty() }
        compose.onNodeWithText("Buscar arete, nombre o raza").performTextInput("Luna")
        compose.onNodeWithText("#UI-001").assertIsDisplayed()
        capturar("ganado-busqueda-oscuro.png")
        compose.onNodeWithText("Buscar arete, nombre o raza").performTextReplacement("inexistente")
        compose.onNodeWithText("Sin coincidencias").assertIsDisplayed()
    }
    @Test fun nacimientosRapidosSePuedenEncontrarYCancelar() {
        runBlocking { db.animalDao().insertar(Animal(idArete="TEMP-ui",tipo="Becerro",peso=0.0,estado="Pendiente")) }
        val vm=PanelRanchoViewModel(db) { "dueño-prueba" };panel=vm
        compose.setContent { TemaMiRanchoPro(oscuro=true) { PantallaGanado(rememberNavController(),vm) } }
        compose.waitUntil(10000) { vm.animales.value.size==2 }
        compose.onNodeWithText("Nacimientos por completar: 1").performClick()
        compose.onNodeWithText("Cancelar registro temporal").performScrollTo().performClick()
        compose.onNodeWithText("Eliminar").performClick()
        compose.waitUntil(10000) { vm.animales.value.none { it.estado=="Pendiente" } }
        assertTrue(vm.animales.value.any { it.idArete=="UI-001" })
    }
    @Test fun borradorVuelveAlReabrirFormularioSinGuardarContrasena() {
        var visible by mutableStateOf(true)
        compose.setContent { MaterialTheme { CompositionLocalProvider(LocalRutaBorrador provides "prueba-ui-borrador") {
            if(visible) {
                var nombre by remember { mutableStateOf("") };var clave by remember { mutableStateOf("") }
                FormularioDialogo("Prueba",{visible=false}) {
                    CampoFormulario("Nombre",nombre,{nombre=it})
                    CampoFormulario("Contraseña",clave,{clave=it},teclado=androidx.compose.ui.text.input.KeyboardType.Password,transformacion=androidx.compose.ui.text.input.PasswordVisualTransformation())
                }
            }
        } } }
        compose.onNodeWithText("Nombre").performTextInput("Borrador conservado")
        compose.onNodeWithText("Contraseña").performTextInput("no-persistir")
        compose.runOnIdle { visible=false }
        compose.runOnIdle { visible=true }
        compose.onNodeWithText("Borrador conservado").assertExists()
        assertFalse(contexto.getSharedPreferences("borradores",Context.MODE_PRIVATE).all.values.any { it.toString().contains("no-persistir") })
    }
    @Test fun primerosPasosCalendarioYEstadisticasUsanLosRegistros() {
        runBlocking { db.animalDao().eliminar(db.animalDao().obtenerPorId("UI-001")!!) }
        val vm=PanelRanchoViewModel(db) { "dueño-prueba" };panel=vm
        val dueno=Usuario("dueño-prueba","","Administrador")
        var pagina by mutableIntStateOf(0)
        var oscuro by mutableStateOf(true)
        compose.setContent { TemaMiRanchoPro(oscuro=oscuro) {
            val nav=rememberNavController()
            when(pagina) {
                0 -> MarcoRancho("Inicio",{}) { item { PrioridadesInicio(vm,dueno,nav) } }
                1 -> PantallaAgenda(nav,vm,dueno)
                else -> PantallaEstadisticas(nav,vm)
            }
        } }
        compose.onNodeWithText("Primeros pasos en tu rancho").assertExists()
        capturar("primeros-pasos-oscuro.png")
        runBlocking {
            db.animalDao().insertar(Animal(idArete="UI-001",nombre="Luna",tipo="Vaca",peso=400.0,fechaNacimiento=Validaciones.fecha("01/01/2020")))
            db.animalDao().insertar(Animal(idArete="UI-002",nombre="Lucero",tipo="Toro",peso=600.0,fechaNacimiento=Validaciones.fecha("01/01/2020")))
            db.tareaDao().insertarTarea(Tarea(titulo="Revisar cerco",descripcion="Revisión de prueba",responsable=dueno.correo,prioridad=Prioridad.MEDIA,fecha=Validaciones.fechaTexto(System.currentTimeMillis())))
        }
        compose.waitUntil(10000) { vm.animales.value.size==2 && vm.tareas.value.isNotEmpty() }
        compose.onNodeWithText("Primeros pasos en tu rancho").assertDoesNotExist()
        compose.runOnIdle { pagina=1 }
        compose.onNodeWithTag("contenido-rancho").performScrollToNode(hasText("Revisar cerco"))
        compose.onNodeWithText("Revisar cerco").assertIsDisplayed();capturar("calendario-oscuro.png")
        compose.runOnIdle { pagina=2 }
        compose.onNodeWithTag("contenido-rancho").performScrollToNode(hasText("Ganado activo · 2"))
        compose.onNodeWithText("Peso actual promedio:",substring=true).assertTextContains("500",substring=true)
        capturar("estadisticas-oscuro.png")
        compose.runOnIdle { oscuro=false }
        capturar("estadisticas-claro.png")
    }
    private fun capturar(nombre: String) {
        compose.waitForIdle()
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        val carpeta=File(contexto.getExternalFilesDir(null),"capturas-avance03").apply { mkdirs() }
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot() ?: error("No se pudo capturar la pantalla")
        try { File(carpeta,nombre).outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) } } finally { bitmap.recycle() }
    }
}
