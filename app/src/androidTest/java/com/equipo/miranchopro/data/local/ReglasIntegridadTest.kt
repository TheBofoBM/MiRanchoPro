package com.equipo.miranchopro.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.utils.Validaciones
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReglasIntegridadTest {
    private lateinit var db: RanchoDatabase
    @Before fun abrir() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, RanchoDatabase::class.java).build()
    }
    @After fun cerrar() { db.close() }

    @Test fun consumoNegativoNoAumentaStock() = runBlocking {
        val insumo = Insumo(nombre = "Forraje", tipo = "Alimento", cantidad = 10.0, unidadMedida = "kg")
        db.insumoDao().insertarInsumo(insumo)
        assertEquals(0, db.insumoDao().descontarStock(insumo.id, -5.0))
        assertEquals(0, db.insumoDao().sumarStock(insumo.id, -5.0))
        assertEquals(10.0, db.insumoDao().obtenerTodos().first().single().cantidad, 0.0)
    }
    @Test fun consumosConcurrentesNoProducenStockNegativo() = runBlocking {
        val insumo = Insumo(nombre = "Forraje", tipo = "Alimento", cantidad = 10.0, unidadMedida = "kg")
        db.insumoDao().insertarInsumo(insumo)
        val resultados = coroutineScope { List(2) { async(Dispatchers.IO) { db.insumoDao().descontarStock(insumo.id, 7.0) } }.awaitAll() }
        assertEquals(1, resultados.sum())
        assertEquals(3.0, db.insumoDao().obtenerTodos().first().single().cantidad, 0.0)
    }
    @Test fun capacidadSeValidaEnElGuardado() = runBlocking {
        db.loteDao().guardarValidado(Lote(nombre = "Norte", capacidadMaxima = 1, ocupacionActual = 0))
        val a = Animal(idArete = "A", peso = 50.0, ubicacion = "Norte")
        db.animalDao().insertarValidado(a)
        assertTrue(runCatching { db.animalDao().insertarValidado(a.copy(idArete = "B")) }.isFailure)
        assertEquals(1, db.loteDao().obtenerTodosLosLotes().first().single().ocupacionActual)
    }
    @Test fun movimientoActualizaAnimalesYConteos() = runBlocking {
        db.loteDao().guardarValidado(Lote(nombre = "Norte", capacidadMaxima = 5, ocupacionActual = 0))
        db.loteDao().guardarValidado(Lote(nombre = "Sur", capacidadMaxima = 5, ocupacionActual = 0))
        db.animalDao().insertarValidado(Animal(idArete = "A", peso = 50.0, ubicacion = "Norte"))
        val lotes = db.loteDao().obtenerTodosLosLotes().first()
        db.loteDao().moverValidado(lotes[0].id, lotes[1].id)
        assertEquals("Sur", db.animalDao().obtenerPorId("A")!!.ubicacion)
        assertEquals(listOf(0, 1), db.loteDao().obtenerTodosLosLotes().first().map { it.ocupacionActual })
    }
    @Test fun movimientoSinCapacidadSeRevierte() = runBlocking {
        db.loteDao().guardarValidado(Lote(nombre = "Norte", capacidadMaxima = 5, ocupacionActual = 0))
        db.loteDao().guardarValidado(Lote(nombre = "Sur", capacidadMaxima = 1, ocupacionActual = 0))
        db.animalDao().insertarValidado(Animal(idArete = "A", peso = 50.0, ubicacion = "Norte"))
        db.animalDao().insertarValidado(Animal(idArete = "B", peso = 50.0, ubicacion = "Sur"))
        val lotes = db.loteDao().obtenerTodosLosLotes().first()
        assertTrue(runCatching { db.loteDao().moverValidado(lotes[0].id, lotes[1].id) }.isFailure)
        assertEquals("Norte", db.animalDao().obtenerPorId("A")!!.ubicacion)
    }
    @Test fun renombrarConservaAsignacionYNoPermiteDuplicados() = runBlocking {
        db.loteDao().guardarValidado(Lote(nombre = "Norte", capacidadMaxima = 5, ocupacionActual = 0))
        db.animalDao().insertarValidado(Animal(idArete = "A", peso = 50.0, ubicacion = "Norte"))
        val lote = db.loteDao().obtenerTodosLosLotes().first().single()
        db.loteDao().guardarValidado(lote.copy(nombre = "Centro"))
        assertEquals("Centro", db.animalDao().obtenerPorId("A")!!.ubicacion)
        assertTrue(runCatching { db.loteDao().guardarValidado(Lote(nombre = " centro ", capacidadMaxima = 5, ocupacionActual = 0)) }.isFailure)
        assertTrue(runCatching { db.loteDao().eliminarValidado(lote.copy(nombre = "Centro")) }.isFailure)
    }
    @Test fun gestacionesConcurrentesSoloCreanUnaActiva() = runBlocking {
        db.animalDao().insertar(Animal(idArete = "V1", peso = 400.0, tipo = "Vaca"))
        val repo = AnimalRepository(db.animalDao(), db.embarazoDao())
        val fecha = Validaciones.fecha("01/10/2026")!!
        val resultados = coroutineScope { List(2) { async(Dispatchers.IO) { repo.registrarEmbarazo("V1", fecha, "Monta Natural", "") } }.awaitAll() }
        assertEquals(1, resultados.count { it.isSuccess })
        assertEquals(1, repo.getHistorialEmbarazos("V1").first().size)
    }
    @Test fun correoDuplicadoEsInsensibleAMayusculas() = runBlocking {
        val dao = db.usuarioDao()
        dao.registrarSeguro(Usuario("empleado@example.test", "clave", "TRABAJADOR"))
        assertTrue(runCatching { dao.registrarSeguro(Usuario("EMPLEADO@example.test", "otra", "TRABAJADOR")) }.isFailure)
        assertEquals(1, dao.obtenerTrabajadores().first().size)
    }
}
