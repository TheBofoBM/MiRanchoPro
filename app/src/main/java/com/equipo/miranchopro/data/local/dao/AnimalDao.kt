package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Animal
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animales")
    fun obtenerTodos(): Flow<List<Animal>>

    @Query("SELECT * FROM animales WHERE idArete = :idArete")
    suspend fun obtenerPorId(idArete: String): Animal?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(animal: Animal)

    @Update
    suspend fun actualizar(animal: Animal)

    @Delete
    suspend fun eliminar(animal: Animal)

    @Query("SELECT * FROM animales WHERE ubicacion = :nombreLote AND estado != 'Baja'")
    fun obtenerAnimalesPorLote(nombreLote: String): Flow<List<Animal>>

    @Query("SELECT * FROM animales WHERE ubicacion != :nombreLote AND estado != 'Baja'")
    fun obtenerAnimalesFueraDeLote(nombreLote: String): Flow<List<Animal>>
}
