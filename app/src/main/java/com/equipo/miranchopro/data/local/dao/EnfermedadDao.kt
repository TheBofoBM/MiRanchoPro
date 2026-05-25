package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Enfermedad
import kotlinx.coroutines.flow.Flow

@Dao
interface EnfermedadDao {
    @Query("SELECT * FROM enfermedades")
    fun obtenerTodas(): Flow<List<Enfermedad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(enfermedad: Enfermedad)

    @Update
    suspend fun actualizar(enfermedad: Enfermedad)

    @Query("SELECT * FROM enfermedades WHERE idAnimal = :idAnimal")
    fun obtenerPorAnimal(idAnimal: String): Flow<List<Enfermedad>>
}
