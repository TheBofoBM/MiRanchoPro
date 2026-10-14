package com.equipo.miranchopro.modelovista

import androidx.core.content.FileProvider
import androidx.room.Room
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.LoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RegistroAnimalFotoTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: RanchoDatabase
    private lateinit var repository: AnimalRepository
    private val testFiles = mutableListOf<File>()
    private val viewModels = ViewModelStore()

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(context, RanchoDatabase::class.java).build()
        repository = AnimalRepository(database.animalDao(), database.embarazoDao())
    }

    @After
    fun closeDatabase() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { viewModels.clear() }
        database.close()
        testFiles.forEach { it.delete() }
    }

    @Test
    fun duplicateAreteDoesNotOverwriteExistingPhotoOrDeletePendingBirth() = runBlocking {
        val originalPhoto = createPhoto("foto-original")
        val selectedPhoto = createPhoto("foto-nueva")
        val existing = Animal(idArete = "ARETE-001", peso = 350.0, fotoPath = originalPhoto.path)
        val pending = Animal(idArete = "TEMP-foto", peso = 0.0, estado = "Pendiente")
        database.animalDao().insertar(existing)
        database.animalDao().insertar(pending)
        val filesBefore = photoNames()
        val viewModel = createViewModel()
        withContext(Dispatchers.Main) { viewModel.cargarParaCompletar(pending.idArete) }
        withTimeout(5_000) {
            while (!withContext(Dispatchers.Main) { viewModel.esEdicionPendiente }) delay(10)
        }
        configure(viewModel, existing.idArete, selectedPhoto)

        assertTrue(save(viewModel) is RegistrarAnimalViewModel.EventoUI.Error)
        awaitFinished(viewModel)

        assertEquals("foto-original", originalPhoto.readText())
        assertEquals(existing, database.animalDao().obtenerPorId(existing.idArete))
        assertEquals(pending, database.animalDao().obtenerPorId(pending.idArete))
        assertEquals(filesBefore, photoNames())
    }

    @Test
    fun unreadablePhotoLeavesThePendingBirthAvailableForRetry() = runBlocking {
        val missingPhoto = createPhoto("foto-a-borrar")
        val missingUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", missingPhoto)
        missingPhoto.delete()
        val pending = Animal(idArete = "TEMP-foto", peso = 0.0, estado = "Pendiente")
        database.animalDao().insertar(pending)
        val viewModel = createViewModel()
        withContext(Dispatchers.Main) { viewModel.cargarParaCompletar(pending.idArete) }
        withTimeout(5_000) {
            while (!withContext(Dispatchers.Main) { viewModel.esEdicionPendiente }) delay(10)
        }
        withContext(Dispatchers.Main) {
            viewModel.idArete = "BECERRO-001"
            viewModel.peso = "35"
            viewModel.fechaNacimiento = "01/10/2026"
            viewModel.fotoUri = missingUri
        }
        val filesBefore = photoNames()

        assertTrue(save(viewModel) is RegistrarAnimalViewModel.EventoUI.Error)
        awaitFinished(viewModel)

        assertEquals(pending, database.animalDao().obtenerPorId(pending.idArete))
        assertNull(database.animalDao().obtenerPorId("BECERRO-001"))
        assertEquals(filesBefore, photoNames())
    }

    @Test
    fun successfulRegistrationKeepsItsOwnPhotoAndNoLongerDependsOnTheSource() = runBlocking {
        val source = createPhoto("contenido-de-la-foto")
        val viewModel = createViewModel()
        configure(viewModel, "BECERRO-001", source)

        assertTrue(save(viewModel) is RegistrarAnimalViewModel.EventoUI.Exito)
        awaitFinished(viewModel)
        val animal = requireNotNull(database.animalDao().obtenerPorId("BECERRO-001"))
        val savedPhoto = File(requireNotNull(animal.fotoPath)).also { testFiles.add(it) }
        assertNotEquals(source.path, savedPhoto.path)
        source.delete()
        assertEquals("contenido-de-la-foto", savedPhoto.readText())
    }

    private suspend fun createViewModel(): RegistrarAnimalViewModel = withContext(Dispatchers.Main) {
        RegistrarAnimalViewModel(repository, LoteRepository(database.loteDao())).also {
            viewModels.put("registro", it)
        }
    }

    private suspend fun configure(viewModel: RegistrarAnimalViewModel, arete: String, photo: File) {
        withContext(Dispatchers.Main) {
            viewModel.idArete = arete
            viewModel.peso = "35"
            viewModel.fechaNacimiento = "01/10/2026"
            viewModel.fotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photo)
        }
    }

    private suspend fun save(viewModel: RegistrarAnimalViewModel): RegistrarAnimalViewModel.EventoUI = coroutineScope {
        val event = async(Dispatchers.Main.immediate) { viewModel.eventoUI.first() }
        withContext(Dispatchers.Main) { viewModel.registrarAnimal(context) }
        withTimeout(5_000) { event.await() }
    }

    private suspend fun awaitFinished(viewModel: RegistrarAnimalViewModel) {
        withTimeout(5_000) {
            while (withContext(Dispatchers.Main) { viewModel.estaCargando }) delay(10)
        }
    }

    private fun createPhoto(content: String): File {
        val directory = File(context.getExternalFilesDir(null), "FotoAnimales").apply { mkdirs() }
        return File.createTempFile("verification_", ".jpg", directory).also {
            it.writeText(content)
            testFiles.add(it)
        }
    }

    private fun photoNames(): Set<String> {
        return File(context.getExternalFilesDir(null), "FotoAnimales").listFiles().orEmpty().map { it.name }.toSet()
    }
}
