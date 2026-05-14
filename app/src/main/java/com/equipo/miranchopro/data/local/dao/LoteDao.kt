package com.equipo.miranchopro.data.local.dao

import androidx.room.Dao
<<<<<<< HEAD
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
=======
import androidx.room.Insert
>>>>>>> origin/feature/adolfo-inventario-configuracion
import androidx.room.Query
import androidx.room.Update
import com.equipo.miranchopro.data.model.Lote
import kotlinx.coroutines.flow.Flow

@Dao
interface LoteDao {
<<<<<<< HEAD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lote: Lote)

    @Update
    suspend fun actualizar(lote: Lote)

    @Delete
    suspend fun eliminar(lote: Lote)
=======
    @Insert
    suspend fun crearLote(lote: Lote)

    @Update
    suspend fun actualizarLote(lote: Lote)
>>>>>>> origin/feature/adolfo-inventario-configuracion

    @Query("SELECT * FROM lotes ORDER BY id ASC")
    fun obtenerTodosLosLotes(): Flow<List<Lote>>

    @Query("SELECT * FROM lotes WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarLotePorNombre(nombre: String): Lote?
}