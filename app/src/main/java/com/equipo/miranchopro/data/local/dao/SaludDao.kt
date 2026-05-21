package com.equipo.miranchopro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.equipo.miranchopro.data.model.RegistroSalud
import kotlinx.coroutines.flow.Flow

@Dao
interface SaludDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(registro: RegistroSalud)

    @Query("SELECT * FROM registros_salud ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<RegistroSalud>>

    @Query("SELECT * FROM registros_salud WHERE idArete = :arete ORDER BY fecha DESC")
    fun obtenerPorAnimal(arete: String): Flow<List<RegistroSalud>>
}