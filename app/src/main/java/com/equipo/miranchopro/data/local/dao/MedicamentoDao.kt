package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos")
    fun obtenerTodos(): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Medicamento?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(medicamento: Medicamento)

    @Update
    suspend fun actualizar(medicamento: Medicamento)

    @Delete
    suspend fun eliminar(medicamento: Medicamento)
}
