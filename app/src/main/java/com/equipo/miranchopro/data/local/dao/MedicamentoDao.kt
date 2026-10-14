package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos")
    fun obtenerTodos(): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Medicamento?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(medicamento: Medicamento)

    @Update
    suspend fun actualizar(medicamento: Medicamento)

    @Delete
    suspend fun eliminar(medicamento: Medicamento)

    @Transaction
    suspend fun guardarValidado(medicamento: Medicamento, nuevo: Boolean) {
        require(medicamento.stock >= 0 && medicamento.nombre.isNotBlank() && medicamento.unidadMedida.isNotBlank()) { "Revisa nombre, unidad y existencias" }
        require(buscarPorNombre(medicamento.nombre)?.id.let { it == null || it == medicamento.id }) { "Este medicamento ya existe" }
        if (nuevo) insertar(medicamento) else actualizar(medicamento)
    }
}
