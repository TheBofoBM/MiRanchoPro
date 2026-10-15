package com.equipo.miranchopro.data.local

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.security.SecureRandom
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Archivo portable cifrado. La importación valida primero y sustituye los registros en una transacción. */
class RespaldoRancho(private val context: Context, private val db: RanchoDatabase) {
    data class Revision(val archivo: File, val carpeta: File, val datos: JSONObject, val animales: Int, val fotos: Int, val fecha: Long)
    private val tablas = listOf("usuarios", "lotes", "animales", "insumos", "medicamentos", "vacunaciones", "enfermedades", "tareas", "embarazos", "pesajes", "eventos_animales", "partos", "movimientos_existencias")
    private val maxArchivo = 64L * 1024 * 1024
    private val maxExpandido = 128L * 1024 * 1024

    suspend fun exportar(uri: Uri, clave: String) = withContext(Dispatchers.IO) {
        require(clave.length >= 12) { "Usa una contraseña de respaldo de al menos 12 caracteres" }
        val temporal=File.createTempFile("respaldo-", ".zip", context.cacheDir)
        try {
            val raiz=JSONObject().put("formato",1).put("esquema",13).put("fecha",System.currentTimeMillis())
            ZipOutputStream(temporal.outputStream().buffered()).use { zip ->
                db.withTransaction {
                    val datos=JSONObject();val fotos=mutableMapOf<String,String>()
                    tablas.forEach { tabla ->
                        val filas=JSONArray()
                        db.openHelper.readableDatabase.query("SELECT * FROM `$tabla`").use { c ->
                            while(c.moveToNext()) {
                                val fila=JSONObject()
                                c.columnNames.forEachIndexed { i,nombre ->
                                    val valor: Any = when(c.getType(i)) {
                                        android.database.Cursor.FIELD_TYPE_NULL -> JSONObject.NULL
                                        android.database.Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                                        android.database.Cursor.FIELD_TYPE_FLOAT -> c.getDouble(i)
                                        else -> c.getString(i)
                                    }
                                    fila.put(nombre,valor)
                                }
                                if(tabla=="animales" && !fila.isNull("fotoPath")) {
                                    val ruta=fila.getString("fotoPath"); val foto=File(ruta)
                                    require(foto.isFile && esFotoPropia(foto)) { "No se pudo leer una fotografía. Corrígela antes de respaldar" }
                                    val entrada=fotos.getOrPut(ruta) { "fotos/${UUID.randomUUID()}.jpg" }
                                    fila.put("fotoPath",entrada)
                                }
                                filas.put(fila)
                            }
                        }
                        datos.put(tabla,filas)
                    }
                    raiz.put("tablas",datos)
                    zip.putNextEntry(ZipEntry("datos.json"));zip.write(raiz.toString().toByteArray(Charsets.UTF_8));zip.closeEntry()
                    fotos.forEach { (ruta,entrada) -> zip.putNextEntry(ZipEntry(entrada));File(ruta).inputStream().use { it.copyTo(zip) };zip.closeEntry() }
                }
            }
            require(temporal.length()<=maxArchivo) { "El respaldo supera el límite de 64 MB" }
            val salida=context.contentResolver.openOutputStream(uri,"wt") ?: error("No se pudo abrir el destino")
            salida.use { out -> cifrar(temporal,out,clave) }
        } finally { temporal.delete() }
    }

    suspend fun revisar(uri: Uri, clave: String): Revision = withContext(Dispatchers.IO) {
        val archivo=File.createTempFile("restaurar-", ".zip",context.cacheDir)
        val carpeta=File(context.filesDir,"restauraciones/${UUID.randomUUID()}").apply { mkdirs() }
        try {
            context.contentResolver.openInputStream(uri)?.use { entrada -> descifrar(entrada,archivo,clave) } ?: error("No se pudo abrir el respaldo")
            var total=0L;var datos: JSONObject?=null;var fotos=0;val nombres=mutableSetOf<String>()
            ZipInputStream(archivo.inputStream().buffered()).use { zip ->
                var entrada=zip.nextEntry
                while(entrada!=null) {
                    val nombre=entrada.name
                    require(nombres.add(nombre) && !entrada.isDirectory && (nombre=="datos.json" || Regex("fotos/[a-f0-9-]{36}\\.jpg").matches(nombre))) { "El respaldo contiene archivos no válidos" }
                    val destino=File(carpeta,nombre).canonicalFile
                    require(destino.path.startsWith(carpeta.canonicalPath+File.separator))
                    destino.parentFile!!.mkdirs()
                    destino.outputStream().use { out ->
                        val buffer=ByteArray(8192);var leidos=zip.read(buffer)
                        while(leidos!=-1) { total+=leidos;require(total<=maxExpandido && (nombre!="datos.json" || destino.length()+leidos<=32L*1024*1024)) { "El respaldo es demasiado grande" };out.write(buffer,0,leidos);leidos=zip.read(buffer) }
                    }
                    if(nombre=="datos.json") datos=JSONObject(destino.readText()) else fotos++
                    zip.closeEntry();entrada=zip.nextEntry
                }
            }
            val raiz=datos ?: error("Faltan los datos del respaldo")
            require(raiz.getInt("formato")==1 && raiz.getInt("esquema")==13) { "Este formato de respaldo no es compatible" }
            val tablasDatos=raiz.getJSONObject("tablas")
            require(tablasDatos.keys().asSequence().toSet()==tablas.toSet()) { "El respaldo está incompleto" }
            require(tablasDatos.getJSONArray("usuarios").length()>0) { "El respaldo no contiene una cuenta" }
            tablas.forEach { tabla ->
                val filas=tablasDatos.getJSONArray(tabla)
                require(filas.length()<=100000) { "Hay demasiados registros" }
                val columnas=columnas(tabla)
                repeat(filas.length()) { i -> require(filas.getJSONObject(i).keys().asSequence().toSet()==columnas.toSet()) { "La estructura del respaldo no coincide" } }
            }
            val animales=tablasDatos.getJSONArray("animales")
            repeat(animales.length()) { i -> val fila=animales.getJSONObject(i)
                if(!fila.isNull("fotoPath")) { val ruta=fila.getString("fotoPath");require(Regex("fotos/[a-f0-9-]{36}\\.jpg").matches(ruta) && File(carpeta,ruta).isFile) { "Falta una fotografía del respaldo" } }
            }
            Revision(archivo,carpeta,raiz,animales.length(),fotos,raiz.getLong("fecha"))
        } catch(e: Exception) { archivo.delete();carpeta.deleteRecursively();throw IllegalArgumentException("No se pudo leer el respaldo. Revisa el archivo y su contraseña",e) }
    }

    suspend fun restaurar(revision: Revision) = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                val sql=db.openHelper.writableDatabase
                tablas.asReversed().forEach { sql.execSQL("DELETE FROM `$it`") }
                val datos=revision.datos.getJSONObject("tablas")
                tablas.forEach { tabla ->
                    val columnas=columnas(tabla);val filas=datos.getJSONArray(tabla)
                    repeat(filas.length()) { i ->
                        val fila=filas.getJSONObject(i)
                        val valores=columnas.map { nombre ->
                            if(fila.isNull(nombre)) null else if(tabla=="animales" && nombre=="fotoPath") File(revision.carpeta,fila.getString(nombre)).absolutePath else fila.get(nombre)
                        }.toTypedArray()
                        sql.execSQL("INSERT INTO `$tabla` (${columnas.joinToString { "`$it`" }}) VALUES (${columnas.joinToString { "?" }})",valores)
                    }
                }
                sql.query("PRAGMA foreign_key_check").use { require(!it.moveToFirst()) { "Hay relaciones inválidas en el respaldo" } }
                sql.query("SELECT COUNT(*) FROM animales WHERE (madreId IS NOT NULL AND madreId NOT IN (SELECT idArete FROM animales)) OR (partoId IS NOT NULL AND partoId NOT IN (SELECT id FROM partos))").use { it.moveToFirst();require(it.getInt(0)==0) { "Hay animales con relaciones inválidas" } }
            }
            context.getSharedPreferences("sesion",Context.MODE_PRIVATE).edit().clear().commit()
            context.getSharedPreferences("borradores",Context.MODE_PRIVATE).edit().clear().commit()
            revision.archivo.delete()
        } catch(e: Exception) { descartar(revision);throw e }
    }

    fun descartar(revision: Revision) { revision.archivo.delete();revision.carpeta.deleteRecursively() }
    private fun columnas(tabla: String): List<String> = db.openHelper.readableDatabase.query("PRAGMA table_info(`$tabla`)").use { c -> buildList { while(c.moveToNext()) add(c.getString(c.getColumnIndexOrThrow("name"))) } }
    private fun esFotoPropia(f: File): Boolean = listOfNotNull(context.filesDir,context.getExternalFilesDir(null)).any { f.canonicalPath.startsWith(it.canonicalPath+File.separator) }
    private fun llave(clave: String,sal: ByteArray,algoritmo: String): SecretKeySpec {
        val spec=PBEKeySpec(clave.toCharArray(),sal,if(algoritmo.endsWith("SHA256")) 600000 else 1400000,256)
        return try { SecretKeySpec(SecretKeyFactory.getInstance(algoritmo).generateSecret(spec).encoded,"AES") } finally { spec.clearPassword() }
    }
    private fun cifrar(archivo: File,out: OutputStream,clave: String) {
        // Este KDF está disponible también en Android 24/25 para trasladar el archivo entre versiones.
        val algoritmo="PBKDF2WithHmacSHA1"
        val sal=ByteArray(16).also { SecureRandom().nextBytes(it) };val iv=ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cifrador=Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE,llave(clave,sal,algoritmo),GCMParameterSpec(128,iv)) }
        val data=DataOutputStream(out);data.writeUTF("MIRANCHO-1");data.writeUTF(algoritmo);data.write(sal);data.write(iv)
        archivo.inputStream().use { input -> val buffer=ByteArray(8192);var n=input.read(buffer);while(n!=-1) { cifrador.update(buffer,0,n)?.let { data.write(it) };n=input.read(buffer) } }
        data.write(cifrador.doFinal());data.flush()
    }
    private fun descifrar(input: InputStream,archivo: File,clave: String) {
        val data=DataInputStream(input);require(data.readUTF()=="MIRANCHO-1")
        val algoritmo=data.readUTF();require(algoritmo in listOf("PBKDF2WithHmacSHA256","PBKDF2WithHmacSHA1"))
        val sal=ByteArray(16);data.readFully(sal);val iv=ByteArray(12);data.readFully(iv)
        val cifrador=Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE,llave(clave,sal,algoritmo),GCMParameterSpec(128,iv)) }
        var total=0L
        archivo.outputStream().use { out -> val buffer=ByteArray(8192);var n=data.read(buffer);while(n!=-1) { total+=n;require(total<=maxArchivo+1024);cifrador.update(buffer,0,n)?.let { out.write(it) };n=data.read(buffer) };out.write(cifrador.doFinal()) }
    }
}
