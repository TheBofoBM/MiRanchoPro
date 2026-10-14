package com.equipo.miranchopro.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RanchoMigrationsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val name = "migration-verification-${UUID.randomUUID()}"
    private var database: RanchoDatabase? = null

    @After
    fun closeDatabase() {
        database?.close()
        context.deleteDatabase(name)
    }

    @Test
    fun version9PreservesRecordsFromEveryExistingTable() = runBlocking {
        createLegacyDatabase(version = 9, hasBirthDate = false, hasPregnancies = false)

        val migrated = openCurrentDatabase()
        val animal = requireNotNull(migrated.animalDao().obtenerPorId("VACA-001"))
        assertEquals("Luna", animal.nombre)
        assertEquals(450.5, animal.peso, 0.0)
        assertEquals(1_700_000_000_000L, animal.fechaRegistro)
        assertEquals("/fotos/animal-original.jpg", animal.fotoPath)
        assertEquals("10:30:00", animal.horaNacimiento)
        assertNull(animal.fechaNacimiento)
        assertNotNull(migrated.usuarioDao().buscarPorCorreo("prueba@example.test"))
        assertEquals(1, migrated.loteDao().obtenerTodosLosLotes().first().size)
        assertEquals(1, migrated.insumoDao().obtenerTodos().first().size)
        assertEquals(1, migrated.medicamentoDao().obtenerTodos().first().size)
        assertEquals(1, migrated.vacunacionDao().obtenerTodas().first().size)
        assertEquals(1, migrated.enfermedadDao().obtenerTodas().first().size)
        assertEquals(1, migrated.tareaDao().obtenerTodas().first().size)
        assertTrue(migrated.embarazoDao().getHistorialEmbarazos("VACA-001").first().isEmpty())
        assertEquals(12, migrated.openHelper.writableDatabase.version)
    }

    @Test
    fun version10PreservesBirthDate() = runBlocking {
        createLegacyDatabase(version = 10, hasBirthDate = true, hasPregnancies = false)

        val animal = requireNotNull(openCurrentDatabase().animalDao().obtenerPorId("VACA-001"))
        assertEquals(1_600_000_000_000L, animal.fechaNacimiento)
    }

    @Test
    fun developmentVersion10WithoutBirthDatePreservesAnimal() = runBlocking {
        createLegacyDatabase(version = 10, hasBirthDate = false, hasPregnancies = false)

        val animal = requireNotNull(openCurrentDatabase().animalDao().obtenerPorId("VACA-001"))
        assertEquals("Luna", animal.nombre)
        assertNull(animal.fechaNacimiento)
    }

    @Test
    fun developmentVersion10PreservesAnExistingPregnancyTable() = runBlocking {
        createLegacyDatabase(version = 10, hasBirthDate = true, hasPregnancies = true)

        assertExistingPregnancy(openCurrentDatabase())
    }

    @Test
    fun version11PreservesPregnancyAndAddsItsForeignKeyIndex() = runBlocking {
        createLegacyDatabase(version = 11, hasBirthDate = true, hasPregnancies = true)

        val migrated = openCurrentDatabase()
        assertExistingPregnancy(migrated)
        val hasIndex = migrated.openHelper.writableDatabase.query("PRAGMA index_list(`embarazos`)").use {
            val indexName = it.getColumnIndexOrThrow("name")
            var found = false
            while (it.moveToNext()) {
                if (it.getString(indexName) == "index_embarazos_vacaId") found = true
            }
            found
        }
        assertTrue(hasIndex)
    }

    @Test
    fun unsupportedVersionFailsWithoutErasingItsRecords() {
        createLegacyDatabase(version = 8, hasBirthDate = false, hasPregnancies = false)
        val current = openCurrentDatabase()
        try {
            current.openHelper.writableDatabase
            fail("Una versión sin migración no debe recrearse vacía")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message.orEmpty().contains("migration", ignoreCase = true))
        } finally {
            current.close()
            database = null
        }

        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READONLY).use {
            assertEquals(8, it.version)
            it.rawQuery("SELECT nombre, fotoPath FROM animales WHERE idArete = 'VACA-001'", null).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Luna", cursor.getString(0))
                assertEquals("/fotos/animal-original.jpg", cursor.getString(1))
            }
        }
    }

    private fun openCurrentDatabase(): RanchoDatabase {
        return RanchoDatabase.builder(context, name).build().also { database = it }
    }

    private suspend fun assertExistingPregnancy(migrated: RanchoDatabase) {
        val pregnancy = requireNotNull(migrated.embarazoDao().getEmbarazoActivoPorVaca("VACA-001"))
        assertEquals(1, pregnancy.id)
        assertEquals("Historial que debe conservarse", pregnancy.notas)
        assertEquals(1_710_000_000_000L, pregnancy.fechaInseminacion)
        assertEquals(1_734_451_200_000L, pregnancy.fechaProbableParto)
        assertEquals(1_600_000_000_000L, migrated.animalDao().obtenerPorId("VACA-001")?.fechaNacimiento)
    }

    private fun createLegacyDatabase(version: Int, hasBirthDate: Boolean, hasPregnancies: Boolean) {
        // El esquema 11 se exportó antes de cambiar el modelo. La versión 9 del
        // historial Git tiene las mismas tablas salvo fechaNacimiento y embarazos.
        val schema = instrumentation.context.assets
            .open("com.equipo.miranchopro.data.local.RanchoDatabase/11.json")
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        val entities = schema.getJSONArray("entities")
        val path = context.getDatabasePath(name)
        path.parentFile?.mkdirs()

        SQLiteDatabase.openOrCreateDatabase(path, null).use { legacy ->
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                val table = entity.getString("tableName")
                if (table == "embarazos" && !hasPregnancies) continue
                var sql = entity.getString("createSql").replace("\${TABLE_NAME}", table)
                if (table == "animales" && !hasBirthDate) {
                    sql = sql.replace(", `fechaNacimiento` INTEGER", "")
                }
                legacy.execSQL(sql)
            }
            legacy.execSQL("INSERT INTO usuarios VALUES ('prueba@example.test', 'solo-fixture', 'ADMIN')")
            legacy.execSQL("INSERT INTO lotes VALUES (1, 'Lote Norte', 20, 1)")
            val animal = ContentValues().apply {
                put("idArete", "VACA-001")
                put("nombre", "Luna")
                put("tipo", "Vaca")
                put("raza", "Serrana")
                put("edad", "3 años")
                put("peso", 450.5)
                put("caracteristica", "Registro histórico")
                put("origen", "Comprada")
                put("color", "Café")
                put("marcas", "Mancha blanca")
                put("ubicacion", "Lote Norte")
                put("estado", "Sano")
                put("fechaRegistro", 1_700_000_000_000L)
                put("horaNacimiento", "10:30:00")
                put("fotoPath", "/fotos/animal-original.jpg")
                if (hasBirthDate) put("fechaNacimiento", 1_600_000_000_000L)
            }
            legacy.insertOrThrow("animales", null, animal)
            legacy.execSQL("INSERT INTO insumos VALUES ('insumo-1', 'Forraje', 'Alimento', 100, 'kg', 10)")
            legacy.execSQL("INSERT INTO medicamentos VALUES ('medicamento-1', 'Registro de prueba', 'Registrada', 2, 'Frascos', 'Inventario')")
            legacy.execSQL("INSERT INTO vacunaciones VALUES ('vacuna-1', 'Registro de prueba', 'Registrada', '01/10/2026', 'VACA-001', NULL, 'Conservar')")
            legacy.execSQL("INSERT INTO enfermedades VALUES ('salud-1', 'Registro de prueba', 'Registrados', 'Registrado', 'VACA-001', '01/10/2026', 'Recuperado', 'Conservar')")
            legacy.execSQL("INSERT INTO tareas VALUES (1, 'Revisar inventario', 'Registro histórico', 'prueba@example.test', 'ALTA', 0, '14/10/2026')")
            if (hasPregnancies) {
                legacy.execSQL("INSERT INTO embarazos VALUES (1, 'VACA-001', 1710000000000, 'Inseminación', 1734451200000, 'Activo', 'Historial que debe conservarse')")
            }
            legacy.version = version
        }
    }
}
