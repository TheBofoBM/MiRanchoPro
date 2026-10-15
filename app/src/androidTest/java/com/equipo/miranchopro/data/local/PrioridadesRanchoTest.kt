package com.equipo.miranchopro.data.local

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.utils.Validaciones
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PrioridadesRanchoTest {
    private lateinit var db: RanchoDatabase
    private val context=ApplicationProvider.getApplicationContext<Context>()
    private val fecha=Validaciones.fecha("01/06/2025")!!
    private val madre=Animal(idArete="MADRE",tipo="Vaca",peso=450.0,fechaNacimiento=Validaciones.fecha("01/01/2020"))
    @Before fun preparar()=runBlocking {
        db=Room.inMemoryDatabaseBuilder(context,RanchoDatabase::class.java).build()
        db.animalDao().insertar(madre)
        db.usuarioDao().registrarUsuario(Usuario("respaldo@example.test","solo-fixture","Administrador"))
    }
    @After fun cerrar() { db.close() }
    private suspend fun gestacion(): Embarazo {
        db.embarazoDao().registrarValidado(Embarazo(vacaId=madre.idArete,fechaInseminacion=Validaciones.fecha("01/09/2024")!!,tipo="Monta Natural",fechaProbableParto=fecha))
        return db.embarazoDao().getEmbarazoActivoPorVaca(madre.idArete)!!
    }
    private suspend fun debeFallar(bloque: suspend () -> Unit) { try { bloque();fail("La operación debía rechazarse") } catch(_: IllegalArgumentException) { } }

    @Test fun partoGemelarEnlazaCriasYCierraGestacion()=runBlocking {
        val g=gestacion();val p=Parto(madreId=madre.idArete,gestacionId=g.id,fecha=fecha,resultado="Crías vivas")
        db.historialDao().registrarParto(p,listOf(Animal(idArete="CRIA-1",tipo="Becerra",peso=32.0),Animal(idArete="CRIA-2",tipo="Becerro",peso=35.0)))
        assertNull(db.embarazoDao().getEmbarazoActivoPorVaca(madre.idArete))
        assertEquals(p.id,db.animalDao().obtenerPorId("CRIA-1")?.partoId)
        assertEquals(madre.idArete,db.animalDao().obtenerPorId("CRIA-2")?.madreId)
        assertEquals(2,db.historialDao().pesajes().first().size)
    }
    @Test fun areteDuplicadoReviertePartoYCriasYCierre()=runBlocking {
        val g=gestacion()
        debeFallar { db.historialDao().registrarParto(Parto(madreId=madre.idArete,gestacionId=g.id,fecha=fecha,resultado="Crías vivas"),listOf(Animal(idArete="NUEVA",tipo="Becerra",peso=30.0),Animal(idArete=madre.idArete,tipo="Becerro",peso=30.0))) }
        assertNull(db.animalDao().obtenerPorId("NUEVA"));assertNotNull(db.embarazoDao().getEmbarazoActivoPorVaca(madre.idArete));assertTrue(db.historialDao().partos().first().isEmpty())
    }
    @Test fun partoAnteriorALaMontaSeRechaza()=runBlocking {
        val g=gestacion()
        debeFallar { db.historialDao().registrarParto(Parto(madreId=madre.idArete,gestacionId=g.id,fecha=Validaciones.fecha("01/01/2024")!!,resultado="Sin crías vivas"),emptyList()) }
        assertNotNull(db.embarazoDao().getEmbarazoActivoPorVaca(madre.idArete))
    }
    @Test fun pesajeHistoricoNoSobrescribeElMasReciente()=runBlocking {
        db.historialDao().registrarPesaje(Pesaje(animalId=madre.idArete,fecha=fecha,peso=470.0))
        db.historialDao().registrarPesaje(Pesaje(animalId=madre.idArete,fecha=fecha-86400000,peso=460.0))
        assertEquals(470.0,db.animalDao().obtenerPorId(madre.idArete)!!.peso,0.0)
        debeFallar { db.historialDao().registrarPesaje(Pesaje(animalId=madre.idArete,fecha=Validaciones.fecha("01/01/2019")!!,peso=10.0)) }
    }
    @Test fun segundoPesajeDelDiaActualizaElPesoAunqueSeCaptureSoloLaFecha()=runBlocking {
        db.animalDao().insertarValidado(madre.copy(idArete="HOY"))
        db.historialDao().registrarPesaje(Pesaje(animalId="HOY",fecha=Validaciones.fecha(Validaciones.fechaTexto(System.currentTimeMillis()))!!,peso=490.0,registradoEn=System.currentTimeMillis()+1))
        assertEquals(490.0,db.animalDao().obtenerPorId("HOY")!!.peso,0.0)
    }
    @Test fun saludAntesDelNacimientoYCambioDeNacimientoIncoherenteSeRechazan()=runBlocking {
        debeFallar { db.vacunacionDao().registrarValidado(Vacunacion(nombreVacuna="Vacuna",dosis="Según registro",fechaAplicacion="01/01/2019",idAnimal=madre.idArete)) }
        db.vacunacionDao().registrarValidado(Vacunacion(nombreVacuna="Vacuna",dosis="Según registro",fechaAplicacion="01/01/2024",idAnimal=madre.idArete))
        debeFallar { db.animalDao().actualizarValidado(madre.copy(fechaNacimiento=fecha)) }
    }
    @Test fun bajaConGestacionActivaSeRechaza()=runBlocking {
        gestacion()
        debeFallar { db.animalDao().actualizarValidado(madre.copy(estado="Baja",fechaBaja=System.currentTimeMillis(),motivoBaja="Venta")) }
        assertEquals("Sano",db.animalDao().obtenerPorId(madre.idArete)?.estado)
    }
    @Test fun consumoConcurrenteConservaSaldoYAuditoria()=runBlocking {
        val i=Insumo(nombre="Alimento",tipo="Alimento",cantidad=10.0,unidadMedida="kg")
        db.insumoDao().insertarInsumo(i,"dueño")
        val resultados=(1..2).map { async(Dispatchers.IO) { db.insumoDao().descontarStock(i.id,7.0,"empleado","Alimentación") } }.awaitAll()
        assertEquals(1,resultados.sum());assertEquals(3.0,db.insumoDao().porId(i.id)!!.cantidad,0.0)
        val consumo=db.historialDao().movimientos().first().single { it.tipo=="Consumo" }
        assertEquals("empleado",consumo.responsable);assertEquals(-7.0,consumo.cantidad,0.0)
    }
    @Test fun medicamentoNoPermiteSaldoNegativoYRegistraResponsable()=runBlocking {
        val m=Medicamento(nombre="Producto",dosis="",stock=4,unidadMedida="frascos")
        db.medicamentoDao().guardarValidado(m,true,"dueño")
        debeFallar { db.medicamentoDao().moverStock(m.id,5,false,"Aplicación","empleado") }
        db.medicamentoDao().moverStock(m.id,2,false,"Aplicación","empleado")
        assertEquals(2,db.medicamentoDao().porId(m.id)?.stock)
        assertEquals("empleado",db.historialDao().movimientos().first().first { it.tipo=="Consumo" }.responsable)
    }
    @Test fun respaldoCifradoRecuperaDatosYFotografiasEnOtraBase()=runBlocking {
        val foto=File(context.filesDir,"foto-prueba-${UUID.randomUUID()}.jpg").apply { writeBytes(byteArrayOf(1,2,3,4)) }
        val archivo=File(context.cacheDir,"prueba-${UUID.randomUUID()}.mrp")
        val otra=Room.inMemoryDatabaseBuilder(context,RanchoDatabase::class.java).build()
        var revision: RespaldoRancho.Revision?=null
        try {
            db.animalDao().actualizar(madre.copy(fotoPath=foto.path));db.historialDao().registrarPesaje(Pesaje(animalId=madre.idArete,fecha=fecha,peso=480.0))
            RespaldoRancho(context,db).exportar(Uri.fromFile(archivo),"contraseña-segura-123")
            assertFalse(archivo.readText(Charsets.ISO_8859_1).contains("respaldo@example.test"))
            val servicio=RespaldoRancho(context,otra)
            debeFallar { servicio.revisar(Uri.fromFile(archivo),"contraseña-incorrecta") }
            revision=servicio.revisar(Uri.fromFile(archivo),"contraseña-segura-123")
            assertEquals(1,revision!!.animales);assertEquals(1,revision!!.fotos)
            servicio.restaurar(revision!!)
            val recuperada=otra.animalDao().obtenerPorId(madre.idArete)!!
            assertEquals(480.0,recuperada.peso,0.0);assertNotEquals(foto.path,recuperada.fotoPath)
            assertArrayEquals(foto.readBytes(),File(recuperada.fotoPath!!).readBytes())
            assertEquals(1,otra.historialDao().pesajes().first().size);assertNotNull(otra.usuarioDao().buscarPorCorreo("respaldo@example.test"))
        } finally { otra.close();revision?.let { RespaldoRancho(context,db).descartar(it) };foto.delete();archivo.delete() }
    }
    @Test fun restauracionFallidaConservaDatosPrevios()=runBlocking {
        val archivo=File(context.cacheDir,"prueba-${UUID.randomUUID()}.mrp");val servicio=RespaldoRancho(context,db)
        var revision: RespaldoRancho.Revision?=null
        try {
            servicio.exportar(Uri.fromFile(archivo),"contraseña-segura-123");revision=servicio.revisar(Uri.fromFile(archivo),"contraseña-segura-123")
            revision!!.datos.getJSONObject("tablas").getJSONArray("animales").getJSONObject(0).put("madreId","NO-EXISTE")
            debeFallar { servicio.restaurar(revision!!) }
            assertNull(db.animalDao().obtenerPorId(madre.idArete)?.madreId);assertEquals(1,db.animalDao().obtenerTodos().first().size)
        } finally { revision?.let { servicio.descartar(it) };archivo.delete() }
    }
}
