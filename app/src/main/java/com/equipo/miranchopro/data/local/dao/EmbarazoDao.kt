package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Embarazo
import kotlinx.coroutines.flow.Flow

@Dao
interface EmbarazoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarEmbarazo(embarazo: Embarazo)

    @Update
    suspend fun actualizarEmbarazo(embarazo: Embarazo)

    @Query("SELECT * FROM embarazos WHERE vacaId = :vacaId AND estado = 'Activo' LIMIT 1")
    suspend fun getEmbarazoActivoPorVaca(vacaId: String): Embarazo?

    @Query("SELECT * FROM embarazos WHERE vacaId = :vacaId ORDER BY fechaInseminacion DESC")
    fun getHistorialEmbarazos(vacaId: String): Flow<List<Embarazo>>
}
