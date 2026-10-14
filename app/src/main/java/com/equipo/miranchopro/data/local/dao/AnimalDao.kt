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

    @Query("DELETE FROM animales WHERE idArete = :idArete AND estado = 'Pendiente'")
    suspend fun eliminarPendientePorId(idArete: String): Int

    @Transaction
    suspend fun completarNacimientoPendiente(idTemporal: String, animal: Animal) {
        require(idTemporal != animal.idArete && animal.estado != "Pendiente") {
            "El nacimiento debe tener un arete definitivo y un estado completo"
        }
        check(eliminarPendientePorId(idTemporal) == 1) {
            "El nacimiento pendiente ya no está disponible"
        }
        // Si el arete está duplicado o falla la inserción, se revierte también el borrado.
        insertar(animal)
    }
}
