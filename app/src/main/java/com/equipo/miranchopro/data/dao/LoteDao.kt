<<<<<<< HEAD
// This file is a duplicate and should be deleted. 
// The actual LoteDao is located in com.equipo.miranchopro.data.local.dao package 
// at the path: app/src/main/java/com/equipo/miranchopro/data/local/dao/LoteDao.kt
=======
package com.equipo.miranchopro.data.dao

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

    @Query("SELECT * FROM lotes ORDER BY id ASC")
    fun obtenerTodosLosLotes(): Flow<List<Lote>>

    @Query("SELECT * FROM lotes WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarLotePorNombre(nombre: String): Lote?
}
>>>>>>> origin/feature/adolfo-inventario-configuracion
