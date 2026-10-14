package com.equipo.miranchopro.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.model.Animal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NacimientoPendienteTest {
    private lateinit var database: RanchoDatabase
    private lateinit var repository: AnimalRepository

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RanchoDatabase::class.java
        ).build()
        repository = AnimalRepository(database.animalDao(), database.embarazoDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun duplicateAreteKeepsPendingBirthAndExistingAnimalUnchanged() = runBlocking {
        val pending = Animal(idArete = "TEMP-original", peso = 0.0, estado = "Pendiente", horaNacimiento = "08:15:00")
        val existing = Animal(idArete = "ARETE-001", nombre = "Animal existente", peso = 350.0)
        database.animalDao().insertar(pending)
        database.animalDao().insertar(existing)

        val result = repository.completarNacimientoPendiente(pending.idArete, pending.copy(idArete = existing.idArete, peso = 35.0, estado = "Sano"))

        assertTrue(result.isFailure)
        assertEquals(pending, database.animalDao().obtenerPorId(pending.idArete))
        assertEquals(existing, database.animalDao().obtenerPorId(existing.idArete))
        assertEquals(2, database.animalDao().obtenerTodos().first().size)
    }

    @Test
    fun completingBirthReplacesOnlyItsPendingRecord() = runBlocking {
        val pending = Animal(idArete = "TEMP-original", peso = 0.0, estado = "Pendiente", fechaRegistro = 1234L, horaNacimiento = "08:15:00", fotoPath = "/foto-pendiente.jpg")
        val completed = pending.copy(idArete = "BECERRO-001", peso = 35.0, estado = "Sano")
        database.animalDao().insertar(pending)

        assertTrue(repository.completarNacimientoPendiente(pending.idArete, completed).isSuccess)
        assertNull(database.animalDao().obtenerPorId(pending.idArete))
        assertEquals(completed, database.animalDao().obtenerPorId(completed.idArete))
        assertEquals(1, database.animalDao().obtenerTodos().first().size)
    }

    @Test
    fun missingPendingBirthDoesNotCreateAnotherAnimal() = runBlocking {
        val completed = Animal(idArete = "BECERRO-001", peso = 35.0)

        assertTrue(repository.completarNacimientoPendiente("TEMP-missing", completed).isFailure)
        assertTrue(database.animalDao().obtenerTodos().first().isEmpty())
    }

    @Test
    fun completingAnOrdinaryAnimalIsRejectedWithoutChangingIt() = runBlocking {
        val existing = Animal(idArete = "VACA-001", nombre = "Conservar", peso = 350.0)
        database.animalDao().insertar(existing)

        assertTrue(repository.completarNacimientoPendiente(existing.idArete, existing.copy(idArete = "VACA-002")).isFailure)
        assertEquals(existing, database.animalDao().obtenerPorId(existing.idArete))
        assertNull(database.animalDao().obtenerPorId("VACA-002"))
    }

    @Test
    fun retryAfterSuccessfulCompletionDoesNotCreateAnotherBirth() = runBlocking {
        val pending = Animal(idArete = "TEMP-original", peso = 0.0, estado = "Pendiente")
        database.animalDao().insertar(pending)
        val completed = pending.copy(idArete = "BECERRO-001", peso = 35.0, estado = "Sano")

        assertTrue(repository.completarNacimientoPendiente(pending.idArete, completed).isSuccess)
        assertTrue(repository.completarNacimientoPendiente(pending.idArete, completed.copy(idArete = "BECERRO-002")).isFailure)
        assertEquals(listOf(completed), database.animalDao().obtenerTodos().first())
    }

    @Test
    fun consecutiveQuickBirthsGetDistinctIdentifiers() = runBlocking {
        val ids = (1..20).map { repository.registrarNacimientoRapido().getOrThrow() }

        assertEquals(20, ids.toSet().size)
        assertEquals(20, database.animalDao().obtenerTodos().first().size)
    }
}
