package com.equipo.miranchopro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.equipo.miranchopro.data.model.Animal
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    // CU-03: Alta de animal (si el ID de arete ya existe, lanzará un error que atraparemos)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarAnimal(animal: Animal)

    // CU-05: Actualizar información
    @Update
    suspend fun actualizarAnimal(animal: Animal)

    // CU-04: Baja física (si quieres eliminar el registro completamente)
    @Delete
    suspend fun eliminarAnimalFisico(animal: Animal)

    // CU-04: Baja Lógica recomendada (cambia estado a Inactivo y guarda el motivo)
    @Query("UPDATE animales SET estado = 'Baja', ubicacion = :motivoBaja WHERE idArete = :idArete")
    suspend fun darDeBajaLogica(idArete: String, motivoBaja: String)

    // CU-06: Ver inventario (Flow permite que la lista en Compose se actualice sola si hay cambios)
    @Query("SELECT * FROM animales ORDER BY idArete ASC")
    fun obtenerInventario(): Flow<List<Animal>>

    // Útil para cargar un animal en la pantalla de EditarAnimal
    @Query("SELECT * FROM animales WHERE idArete = :idArete LIMIT 1")
    suspend fun obtenerAnimalPorId(idArete: String): Animal?
}