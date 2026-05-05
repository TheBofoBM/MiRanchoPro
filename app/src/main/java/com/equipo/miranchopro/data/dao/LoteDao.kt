package com.equipo.miranchopro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.equipo.miranchopro.data.model.Lote
import kotlinx.coroutines.flow.Flow

@Dao
interface LoteDao {
    @Insert
    suspend fun crearLote(lote: Lote)

    @Update
    suspend fun actualizarLote(lote: Lote)

    // Usamos Flow para que la UI se actualice en tiempo real si hay cambios en la BD
    @Query("SELECT * FROM lotes ORDER BY id ASC")
    fun obtenerTodosLosLotes(): Flow<List<Lote>>

    @Query("SELECT * FROM lotes WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarLotePorNombre(nombre: String): Lote?
}