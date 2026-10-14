package com.equipo.miranchopro.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Cada guardado crea un archivo nuevo; un arete duplicado nunca sobrescribe otra foto. */
object AlmacenFotosAnimales {
    suspend fun guardar(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val directory = directorio(context)
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("No se pudo crear el directorio de fotografías")
        }
        val destination = File.createTempFile("animal_", ".jpg", directory)
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("No se pudo abrir la fotografía seleccionada")
            input.use { source ->
                destination.outputStream().use { output -> source.copyTo(output) }
            }
            if (destination.length() == 0L) throw IOException("La fotografía está vacía")
            destination.absolutePath
        } catch (e: Exception) {
            destination.delete()
            throw e
        }
    }

    fun eliminarArchivoNuevo(context: Context, path: String): Boolean = runCatching {
        val file = File(path).canonicalFile
        file.parentFile == directorio(context).canonicalFile &&
            file.name.startsWith("animal_") && file.delete()
    }.getOrDefault(false)

    private fun directorio(context: Context): File {
        return File(context.getExternalFilesDir(null) ?: context.filesDir, "FotoAnimales")
    }
}
