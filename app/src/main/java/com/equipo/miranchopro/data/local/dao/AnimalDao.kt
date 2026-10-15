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

    @Insert suspend fun evento(evento: com.equipo.miranchopro.data.model.EventoAnimal)
    @Insert suspend fun pesaje(pesaje: com.equipo.miranchopro.data.model.Pesaje)
    @Query("SELECT MAX(fecha) FROM pesajes WHERE animalId = :id") suspend fun fechaUltimoPesaje(id: String): Long?
    @Query("SELECT COUNT(*) FROM animales WHERE LOWER(TRIM(idArete)) = LOWER(TRIM(:id))") suspend fun existeArete(id: String): Int
    @Query("SELECT COUNT(*) FROM embarazos WHERE vacaId = :id AND estado = 'Activo'") suspend fun gestacionesActivas(id: String): Int
    @Query("SELECT MAX(fechaInseminacion) FROM embarazos WHERE vacaId = :id") suspend fun ultimaMonta(id: String): Long?
    @Query("SELECT fechaAplicacion FROM vacunaciones WHERE idAnimal = :id UNION ALL SELECT fechaDeteccion FROM enfermedades WHERE idAnimal = :id") suspend fun fechasSalud(id: String): List<String>
    @Query("SELECT MAX(fecha) FROM partos WHERE madreId = :id") suspend fun ultimoParto(id: String): Long?
    @Query("SELECT MIN(fecha) FROM (SELECT fecha FROM pesajes WHERE animalId = :id UNION ALL SELECT fechaInseminacion AS fecha FROM embarazos WHERE vacaId = :id UNION ALL SELECT fecha FROM partos WHERE madreId = :id)") suspend fun primerEvento(id: String): Long?
    @Query("SELECT COUNT(*) FROM animales WHERE madreId = :id") suspend fun numeroCrias(id: String): Int

    suspend fun validarCronologia(a: Animal) {
        require(a.tipo in listOf("Vaca", "Toro", "Becerro", "Becerra", "Novillo", "Vaquilla")) { "Selecciona una categoría válida" }
        val nacimiento = a.fechaNacimiento
        if (nacimiento != null) {
            val fechas = fechasSalud(a.idArete).mapNotNull { com.equipo.miranchopro.utils.Validaciones.fecha(it) } + listOfNotNull(primerEvento(a.idArete))
            require(fechas.none { it < nacimiento }) { "El nacimiento no puede ser posterior a eventos ya registrados" }
        }
        if (gestacionesActivas(a.idArete) > 0) {
            require(a.estado != "Baja") { "Cierra o interrumpe la gestación antes de dar de baja" }
            require(a.tipo in listOf("Vaca", "Vaquilla", "Becerra")) { "No se puede cambiar a macho mientras tenga una gestación activa" }
        }
        require(numeroCrias(a.idArete)==0 || a.tipo in listOf("Vaca", "Vaquilla", "Becerra")) { "Un animal con crías registradas debe conservar una categoría de hembra" }
        a.madreId?.let { id ->
            val madre = obtenerPorId(id) ?: throw IllegalArgumentException("La madre ya no existe")
            require(id != a.idArete && madre.tipo in listOf("Vaca", "Vaquilla", "Becerra")) { "La madre debe ser una hembra diferente" }
            require(nacimiento == null || madre.fechaNacimiento == null || nacimiento > madre.fechaNacimiento) { "La cría debe nacer después que la madre" }
        }
        a.fechaBaja?.let { baja ->
            require(baja <= com.equipo.miranchopro.utils.Validaciones.finDeHoy() && (nacimiento == null || baja >= nacimiento)) { "La fecha de baja no es válida" }
            require((listOfNotNull(ultimaMonta(a.idArete), ultimoParto(a.idArete), fechaUltimoPesaje(a.idArete)) + fechasSalud(a.idArete).mapNotNull { com.equipo.miranchopro.utils.Validaciones.fecha(it) }).none { it > baja }) { "La baja no puede preceder a eventos registrados" }
        }
    }

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
    suspend fun insertarValidado(animal: Animal, responsable: String = "") {
        require(existeArete(animal.idArete) == 0) { "Este arete ya está registrado" }
        validarCronologia(animal)
        validarLote(animal)
        insertar(animal)
        evento(com.equipo.miranchopro.data.model.EventoAnimal(animalId = animal.idArete, tipo = "Alta", detalle = "Registro de ${animal.tipo} en ${animal.ubicacion}", responsable = responsable))
        if (animal.peso > 0) pesaje(com.equipo.miranchopro.data.model.Pesaje(animalId = animal.idArete, fecha = com.equipo.miranchopro.utils.AgendaRancho.inicioDia(System.currentTimeMillis()), peso = animal.peso, notas = "Peso al registrar", responsable = responsable))
    }

    @Transaction
    suspend fun actualizarValidado(animal: Animal, responsable: String = "") {
        val anterior = obtenerPorId(animal.idArete) ?: throw IllegalArgumentException("El animal ya no está disponible")
        require(anterior.estado!="Baja" || animal.peso==anterior.peso) { "No cambies el peso actual de un animal dado de baja" }
        validarCronologia(animal)
        validarLote(animal)
        actualizar(animal)
        if (anterior.peso != animal.peso && animal.peso > 0) pesaje(com.equipo.miranchopro.data.model.Pesaje(animalId = animal.idArete, fecha = com.equipo.miranchopro.utils.AgendaRancho.inicioDia(System.currentTimeMillis()), peso = animal.peso, notas = "Peso actualizado en la ficha", responsable = responsable))
        if (anterior.ubicacion != animal.ubicacion) evento(com.equipo.miranchopro.data.model.EventoAnimal(animalId = animal.idArete, tipo = "Traslado", detalle = "${anterior.ubicacion} → ${animal.ubicacion}", responsable = responsable))
        if (anterior.estado != animal.estado) evento(com.equipo.miranchopro.data.model.EventoAnimal(animalId = animal.idArete, fecha = animal.fechaBaja ?: System.currentTimeMillis(), tipo = "Estado", detalle = "${anterior.estado} → ${animal.estado}. ${animal.motivoBaja.orEmpty()}", responsable = responsable))
    }

    @Transaction
    suspend fun completarNacimientoPendiente(idTemporal: String, animal: Animal, responsable: String = "") {
        require(idTemporal != animal.idArete && animal.estado != "Pendiente") {
            "El nacimiento debe tener un arete definitivo y un estado completo"
        }
        check(eliminarPendientePorId(idTemporal) == 1) {
            "El nacimiento pendiente ya no está disponible"
        }
        // Si el arete está duplicado o falla la inserción, se revierte también el borrado.
        insertarValidado(animal,responsable)
    }
}
