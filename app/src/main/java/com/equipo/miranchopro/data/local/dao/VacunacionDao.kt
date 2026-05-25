package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Vacunacion
import kotlinx.coroutines.flow.Flow

@Dao
interface VacunacionDao {
    @Query("SELECT * FROM vacunaciones")
    fun obtenerTodas(): Flow<List<Vacunacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vacunacion: Vacunacion)

    @Query("SELECT * FROM vacunaciones WHERE idAnimal = :idAnimal")
    fun obtenerPorAnimal(idAnimal: String): Flow<List<Vacunacion>>
}
