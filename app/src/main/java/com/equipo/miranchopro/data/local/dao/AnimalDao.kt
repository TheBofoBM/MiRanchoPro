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

    @Query("SELECT * FROM lotes WHERE nombre = :nombre LIMIT 1")
    suspend fun lotePorNombre(nombre: String): com.equipo.miranchopro.data.model.Lote?

    @Query("SELECT COUNT(*) FROM animales WHERE ubicacion = :nombre AND estado NOT IN ('Baja','Pendiente') AND idArete != :excluir")
    suspend fun ocupacion(nombre: String, excluir: String): Int

    suspend fun validarLote(animal: Animal) {
        if (animal.estado in listOf("Baja", "Pendiente") || animal.ubicacion == "Sin Lote") return
        val lote = lotePorNombre(animal.ubicacion) ?: throw IllegalArgumentException("Selecciona un lote existente o Sin Lote")
        require(ocupacion(lote.nombre, animal.idArete) < lote.capacidadMaxima) { "El lote no tiene capacidad disponible" }
    }

    @Transaction
    suspend fun insertarValidado(animal: Animal) {
        require(obtenerPorId(animal.idArete) == null) { "Este arete ya está registrado" }
        validarLote(animal)
        insertar(animal)
    }

    @Transaction
    suspend fun actualizarValidado(animal: Animal) {
        require(obtenerPorId(animal.idArete) != null) { "El animal ya no está disponible" }
        validarLote(animal)
        actualizar(animal)
    }

    @Transaction
    suspend fun completarNacimientoPendiente(idTemporal: String, animal: Animal) {
        require(idTemporal != animal.idArete && animal.estado != "Pendiente") {
            "El nacimiento debe tener un arete definitivo y un estado completo"
        }
        check(eliminarPendientePorId(idTemporal) == 1) {
            "El nacimiento pendiente ya no está disponible"
        }
        // Si el arete está duplicado o falla la inserción, se revierte también el borrado.
        insertarValidado(animal)
    }
}
