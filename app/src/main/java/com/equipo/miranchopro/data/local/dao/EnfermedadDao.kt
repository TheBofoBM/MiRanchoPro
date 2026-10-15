package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Enfermedad
import kotlinx.coroutines.flow.Flow

@Dao
interface EnfermedadDao {
    @Query("SELECT * FROM animales WHERE idArete = :id") suspend fun animal(id: String): com.equipo.miranchopro.data.model.Animal?
    @Transaction suspend fun guardarValidado(e: Enfermedad,nueva: Boolean) {
        val a=animal(e.idAnimal) ?: throw IllegalArgumentException("Selecciona un animal existente")
        val reglas=com.equipo.miranchopro.utils.Validaciones
        val fecha=reglas.fecha(e.fechaDeteccion) ?: throw IllegalArgumentException("Ingresa una fecha válida")
        require(a.estado !in listOf("Baja","Pendiente") && e.nombre.isNotBlank() && e.sintomas.isNotBlank()) { "Revisa animal, enfermedad y síntomas" }
        require(reglas.errorFecha(e.fechaDeteccion,"La fecha")==null && (a.fechaNacimiento==null || fecha>=a.fechaNacimiento)) { "La detección no puede ser futura ni anterior al nacimiento" }
        require(e.estado in listOf("Activo","Recuperado","Fallecido","Pendiente")) { "Selecciona un estado válido" }
        if(nueva) insertar(e) else actualizar(e)
    }
    @Query("SELECT * FROM enfermedades")
    fun obtenerTodas(): Flow<List<Enfermedad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(enfermedad: Enfermedad)

    @Update
    suspend fun actualizar(enfermedad: Enfermedad)

    @Query("SELECT * FROM enfermedades WHERE idAnimal = :idAnimal")
    fun obtenerPorAnimal(idAnimal: String): Flow<List<Enfermedad>>
}
