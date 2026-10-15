package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Vacunacion
import kotlinx.coroutines.flow.Flow

@Dao
interface VacunacionDao {
    @Query("UPDATE vacunaciones SET recordatorioCompletado = 1 WHERE id = :id") suspend fun atenderRecordatorio(id: String)
    @Query("SELECT * FROM animales WHERE idArete = :id") suspend fun animal(id: String): com.equipo.miranchopro.data.model.Animal?
    @Transaction suspend fun registrarValidado(v: Vacunacion) {
        val a=animal(v.idAnimal) ?: throw IllegalArgumentException("Selecciona un animal existente")
        val reglas=com.equipo.miranchopro.utils.Validaciones
        require(a.estado !in listOf("Baja","Pendiente") && v.nombreVacuna.isNotBlank() && v.dosis.isNotBlank()) { "Selecciona un animal activo e indica vacuna y dosis" }
        val fecha=reglas.fecha(v.fechaAplicacion) ?: throw IllegalArgumentException("Ingresa una fecha válida")
        require(reglas.errorFecha(v.fechaAplicacion,"La fecha")==null && (a.fechaNacimiento==null || fecha>=a.fechaNacimiento)) { "La aplicación no puede ser futura ni anterior al nacimiento" }
        if(!v.proximaDosis.isNullOrBlank()) require(reglas.fecha(v.proximaDosis)?.let { it>=fecha }==true) { "La próxima dosis debe ser posterior a la aplicación" }
        insertar(v)
    }
    @Query("SELECT * FROM vacunaciones")
    fun obtenerTodas(): Flow<List<Vacunacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vacunacion: Vacunacion)

    @Query("SELECT * FROM vacunaciones WHERE idAnimal = :idAnimal")
    fun obtenerPorAnimal(idAnimal: String): Flow<List<Vacunacion>>
}
