package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.utils.Validaciones
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {
    @Query("SELECT * FROM pesajes ORDER BY fecha DESC, registradoEn DESC, rowid DESC")
    fun pesajes(): Flow<List<Pesaje>>
    @Query("SELECT * FROM eventos_animales ORDER BY fecha DESC, id DESC")
    fun eventos(): Flow<List<EventoAnimal>>
    @Query("SELECT * FROM partos ORDER BY fecha DESC, id DESC")
    fun partos(): Flow<List<Parto>>
    @Query("SELECT * FROM movimientos_existencias ORDER BY fecha DESC, id DESC")
    fun movimientos(): Flow<List<MovimientoExistencia>>
    @Query("SELECT * FROM animales WHERE idArete = :id")
    suspend fun animal(id: String): Animal?
    @Query("SELECT COUNT(*) FROM animales WHERE LOWER(TRIM(idArete)) = LOWER(TRIM(:id))") suspend fun existeArete(id: String): Int
    @Query("SELECT * FROM embarazos WHERE id = :id")
    suspend fun gestacion(id: Int): Embarazo?
    @Query("SELECT * FROM embarazos WHERE estado = 'Activo'")
    fun gestaciones(): Flow<List<Embarazo>>
    @Query("SELECT * FROM embarazos ORDER BY fechaInseminacion DESC, id DESC") fun historialGestaciones(): Flow<List<Embarazo>>
    @Query("SELECT * FROM pesajes WHERE animalId = :id ORDER BY fecha DESC, registradoEn DESC, rowid DESC LIMIT 1")
    suspend fun ultimoPeso(id: String): Pesaje?
    @Insert suspend fun insertar(pesaje: Pesaje)
    @Insert suspend fun insertar(evento: EventoAnimal)
    @Insert suspend fun insertar(parto: Parto)
    @Insert suspend fun insertar(animal: Animal)
    @Update suspend fun actualizar(animal: Animal)
    @Query("UPDATE embarazos SET estado = 'Finalizado' WHERE id = :id AND estado = 'Activo'")
    suspend fun finalizar(id: Int): Int
    @Query("SELECT COUNT(*) FROM animales WHERE ubicacion = :nombre AND estado NOT IN ('Baja','Pendiente')")
    suspend fun ocupacion(nombre: String): Int
    @Query("SELECT * FROM lotes WHERE nombre = :nombre LIMIT 1")
    suspend fun lote(nombre: String): Lote?
    @Query("SELECT COUNT(*) FROM partos WHERE madreId = :madre AND fecha >= :desde AND fecha < :hasta") suspend fun partosDelDia(madre: String,desde: Long,hasta: Long): Int

    @Transaction
    suspend fun registrarPesaje(p: Pesaje) {
        val a = animal(p.animalId) ?: throw IllegalArgumentException("El animal ya no existe")
        validarFecha(a, p.fecha)
        require(p.peso.isFinite() && p.peso > 0) { "El peso debe ser mayor que cero" }
        insertar(p.copy(fecha=com.equipo.miranchopro.utils.AgendaRancho.inicioDia(p.fecha)))
        if (ultimoPeso(a.idArete)?.id == p.id) actualizar(a.copy(peso = p.peso))
    }

    @Transaction
    suspend fun registrarParto(p: Parto, crias: List<Animal>) {
        val madre = animal(p.madreId) ?: throw IllegalArgumentException("Selecciona una madre existente")
        require(madre.tipo in listOf("Vaca", "Vaquilla", "Becerra") && madre.estado !in listOf("Baja", "Pendiente")) { "Selecciona una hembra activa" }
        validarFecha(madre, p.fecha)
        val dia=com.equipo.miranchopro.utils.AgendaRancho.inicioDia(p.fecha)
        require(partosDelDia(madre.idArete,dia,com.equipo.miranchopro.utils.AgendaRancho.sumarDias(dia,1))==0) { "Ya existe un parto de esta madre en esa fecha; revisa el historial" }
        require(p.resultado in listOf("Crías vivas", "Sin crías vivas")) { "Selecciona el resultado" }
        require((p.resultado == "Crías vivas" && crias.isNotEmpty()) || (p.resultado == "Sin crías vivas" && crias.isEmpty())) { "Revisa las crías y el resultado del parto" }
        require(crias.size <= 4 && crias.map { it.idArete.trim().lowercase() }.distinct().size == crias.size) { "Revisa los aretes de las crías" }
        p.gestacionId?.let { id ->
            val g = gestacion(id) ?: throw IllegalArgumentException("La gestación ya no existe")
            require(g.vacaId == madre.idArete && g.estado == "Activo" && p.fecha >= g.fechaInseminacion) { "El parto debe corresponder a una gestación activa y ser posterior a la monta" }
            check(finalizar(id) == 1)
        }
        insertar(p)
        crias.forEach { cria ->
            require(Validaciones.erroresAnimal(cria.idArete, cria.peso.toString(), Validaciones.fechaTexto(p.fecha)).isEmpty()) { "Revisa arete y peso de cada cría" }
            require(existeArete(cria.idArete) == 0 && cria.tipo in listOf("Becerro", "Becerra") && cria.estado=="Sano") { "El arete ya existe o la categoría de la cría no es válida" }
            if (cria.ubicacion != "Sin Lote") {
                val l = lote(cria.ubicacion) ?: throw IllegalArgumentException("El lote de la cría ya no existe")
                require(ocupacion(l.nombre) < l.capacidadMaxima) { "El lote no tiene espacio para las crías" }
            }
            insertar(cria.copy(madreId = madre.idArete, partoId = p.id, fechaNacimiento = p.fecha, origen = "De parto"))
            insertar(Pesaje(animalId = cria.idArete, fecha = p.fecha, peso = cria.peso, notas = "Peso al nacer", responsable = p.responsable))
        }
        insertar(EventoAnimal(animalId = madre.idArete, fecha = p.fecha, tipo = "Parto", detalle = "${p.resultado} · ${crias.size} crías. ${p.notas}", responsable = p.responsable))
    }

    fun validarFecha(a: Animal, fecha: Long) {
        require(fecha >= Validaciones.fecha("01/01/1900")!! && fecha <= Validaciones.finDeHoy()) { "Ingresa una fecha válida que no sea futura" }
        require(a.fechaNacimiento == null || fecha >= a.fechaNacimiento) { "La fecha no puede ser anterior al nacimiento" }
        require(a.estado != "Pendiente" && (a.estado!="Baja" || a.fechaBaja!=null) && (a.fechaBaja == null || fecha <= a.fechaBaja)) { "La fecha no puede ser posterior a la baja" }
    }
}
