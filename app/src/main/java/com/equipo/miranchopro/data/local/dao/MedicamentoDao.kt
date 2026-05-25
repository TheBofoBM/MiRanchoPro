package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos")
    fun obtenerTodos(): Flow<List<Medicamento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(medicamento: Medicamento)

    @Update
    suspend fun actualizar(medicamento: Medicamento)

    @Delete
    suspend fun eliminar(medicamento: Medicamento)
}
