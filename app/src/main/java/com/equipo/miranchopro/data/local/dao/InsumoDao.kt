package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Insumo
import kotlinx.coroutines.flow.Flow

@Dao
interface InsumoDao {
    @Query("SELECT * FROM insumos")
    fun obtenerTodos(): Flow<List<Insumo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInsumo(insumo: Insumo)

    @Update
    suspend fun actualizarInsumo(insumo: Insumo)

    @Delete
    suspend fun eliminarInsumo(insumo: Insumo)

    @Query("UPDATE insumos SET cantidad = cantidad - :gasto WHERE id = :idInsumo")
    suspend fun descontarStock(idInsumo: String, gasto: Double)
}
