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

    @Query("SELECT * FROM embarazos WHERE vacaId = :vacaId AND estado = 'Activo' ORDER BY fechaInseminacion DESC, id DESC LIMIT 1")
    suspend fun getEmbarazoActivoPorVaca(vacaId: String): Embarazo?

    @Query("SELECT * FROM embarazos WHERE vacaId = :vacaId ORDER BY fechaInseminacion DESC")
    fun getHistorialEmbarazos(vacaId: String): Flow<List<Embarazo>>

    @Query("SELECT * FROM animales WHERE idArete = :id")
    suspend fun animal(id: String): com.equipo.miranchopro.data.model.Animal?

    @Transaction
    suspend fun registrarValidado(embarazo: Embarazo) {
        val vaca = animal(embarazo.vacaId) ?: throw IllegalArgumentException("El animal no existe")
        require(vaca.tipo in listOf("Vaca", "Vaquilla", "Becerra") && vaca.estado !in listOf("Baja", "Pendiente")) { "Selecciona una hembra activa" }
        require(embarazo.fechaInseminacion <= com.equipo.miranchopro.utils.Validaciones.finDeHoy()) { "La fecha de monta no puede estar en el futuro" }
        require(vaca.fechaNacimiento == null || embarazo.fechaInseminacion >= vaca.fechaNacimiento) { "La monta no puede ser anterior al nacimiento" }
        require(embarazo.tipo in listOf("Monta Natural", "Inseminación")) { "Selecciona el tipo de cruce" }
        require(getEmbarazoActivoPorVaca(vaca.idArete) == null) { "Este animal ya tiene una gestación activa" }
        registrarEmbarazo(embarazo)
    }

    @Query("UPDATE embarazos SET estado = :estado WHERE vacaId = :id AND estado = 'Activo'")
    suspend fun cerrarActivos(id: String, estado: String): Int
}
