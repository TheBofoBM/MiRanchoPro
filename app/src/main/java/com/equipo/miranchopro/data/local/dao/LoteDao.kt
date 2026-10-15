package com.equipo.miranchopro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.equipo.miranchopro.data.model.Lote
import kotlinx.coroutines.flow.Flow

@Dao
interface LoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lote: Lote)

    @Update
    suspend fun actualizar(lote: Lote)

    @Delete
    suspend fun eliminar(lote: Lote)

    @Query("SELECT id, nombre, capacidadMaxima, (SELECT COUNT(*) FROM animales WHERE ubicacion = lotes.nombre AND estado NOT IN ('Baja','Pendiente')) AS ocupacionActual FROM lotes ORDER BY id ASC")
    fun obtenerTodosLosLotes(): Flow<List<Lote>>

    @Query("SELECT * FROM lotes WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarLotePorNombre(nombre: String): Lote?

    @Query("SELECT * FROM lotes WHERE id = :id")
    suspend fun porId(id: Int): Lote?
    @Query("SELECT COUNT(*) FROM lotes WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) AND id != :excluir")
    suspend fun nombresIguales(nombre: String, excluir: Int): Int
    @Query("SELECT COUNT(*) FROM animales WHERE ubicacion = :nombre AND estado NOT IN ('Baja','Pendiente')")
    suspend fun ocupacionReal(nombre: String): Int
    @Query("UPDATE animales SET ubicacion = :destino WHERE ubicacion = :origen")
    suspend fun renombrarUbicacion(origen: String, destino: String)
    @Query("UPDATE animales SET ubicacion = :destino WHERE ubicacion = :origen AND estado NOT IN ('Baja','Pendiente')")
    suspend fun trasladarAnimales(origen: String, destino: String)
    @Query("INSERT INTO eventos_animales (id, animalId, fecha, tipo, detalle, responsable) SELECT lower(hex(randomblob(16))), idArete, :fecha, 'Traslado', :detalle, :responsable FROM animales WHERE ubicacion = :origen AND estado NOT IN ('Baja','Pendiente')")
    suspend fun registrarTraslado(origen: String, detalle: String, fecha: Long, responsable: String)

    @Transaction
    suspend fun guardarValidado(lote: Lote) {
        val nombre = lote.nombre.trim()
        require(nombre.isNotBlank() && nombre.length <= 80 && !nombre.equals("Sin Lote", true)) { "Ingresa un nombre de lote de 1 a 80 caracteres distinto de Sin Lote" }
        require(nombresIguales(nombre, lote.id) == 0) { "Ya existe un lote con ese nombre" }
        require(lote.capacidadMaxima > 0) { "La capacidad debe ser mayor que cero" }
        val anterior = if (lote.id == 0) null else porId(lote.id) ?: throw IllegalArgumentException("El lote ya no existe")
        require(anterior == null || lote.capacidadMaxima >= ocupacionReal(anterior.nombre)) { "La capacidad no puede ser menor que los animales asignados" }
        if (anterior == null) insertar(lote.copy(nombre = nombre, ocupacionActual = 0))
        else { actualizar(lote.copy(nombre = nombre)); renombrarUbicacion(anterior.nombre, nombre) }
    }

    @Transaction
    suspend fun moverValidado(origenId: Int, destinoId: Int, responsable: String = "") {
        require(origenId != destinoId) { "Selecciona un lote diferente" }
        val origen = porId(origenId) ?: throw IllegalArgumentException("El lote de origen ya no existe")
        val destino = porId(destinoId) ?: throw IllegalArgumentException("El lote de destino ya no existe")
        val cantidad = ocupacionReal(origen.nombre)
        require(cantidad > 0) { "El lote de origen está vacío" }
        require(cantidad.toLong() + ocupacionReal(destino.nombre) <= destino.capacidadMaxima.toLong()) { "El destino no tiene capacidad suficiente" }
        registrarTraslado(origen.nombre,"${origen.nombre} → ${destino.nombre}",System.currentTimeMillis(),responsable)
        trasladarAnimales(origen.nombre, destino.nombre)
    }

    @Transaction
    suspend fun eliminarValidado(lote: Lote) {
        val actual = porId(lote.id) ?: throw IllegalArgumentException("El lote ya no existe")
        require(ocupacionReal(actual.nombre) == 0) { "El lote todavía tiene animales asignados" }
        // Los registros históricos permanecen, sin apuntar a un lote eliminado.
        renombrarUbicacion(actual.nombre, "Sin Lote")
        eliminar(actual)
    }
}
