package com.equipo.miranchopro.utils

import com.equipo.miranchopro.data.model.*
import java.util.Calendar

data class PendienteRancho(val id: String, val fecha: Long, val titulo: String, val detalle: String, val animalId: String? = null, val tipo: String)
object AgendaRancho {
    fun inicioDia(fecha: Long): Long = Calendar.getInstance().apply { timeInMillis=fecha;set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0) }.timeInMillis
    fun sumarDias(fecha: Long, dias: Int): Long = Calendar.getInstance().apply { timeInMillis=fecha;add(Calendar.DAY_OF_YEAR,dias) }.timeInMillis
    fun construir(animales: List<Animal>, tareas: List<Tarea>, vacunas: List<Vacunacion>, gestaciones: List<Embarazo>, usuario: Usuario?, anticipacion: Int = 7): List<PendienteRancho> {
        if(usuario==null) return emptyList()
        val activos=animales.filter { it.estado !in listOf("Baja","Pendiente") }.associateBy { it.idArete }
        return buildList {
            tareas.filter { !it.estaHecha && (usuario.rol.equals("Administrador",true) || it.responsable.equals(usuario.correo,true)) }.forEach { t ->
                Validaciones.fecha(t.fecha ?: "")?.let { add(PendienteRancho("t-${t.id}",it,t.titulo,"Tarea · ${t.responsable}",tipo="Tarea")) }
            }
            vacunas.filter { it.idAnimal in activos && !it.recordatorioCompletado }.forEach { v -> Validaciones.fecha(v.proximaDosis ?: "")?.let { add(PendienteRancho("v-${v.id}",it,"Próxima aplicación: ${v.nombreVacuna}","Arete ${v.idAnimal}",v.idAnimal,"Salud")) } }
            gestaciones.filter { it.estado=="Activo" && it.vacaId in activos }.forEach { g ->
                add(PendienteRancho("g-${g.id}",g.fechaProbableParto,"Seguimiento de parto","Arete ${g.vacaId} · ventana orientativa ${intervaloParto(g.fechaProbableParto)}",g.vacaId,"Parto"))
            }
        }.sortedWith(compareBy<PendienteRancho> { it.fecha }.thenBy { it.id })
    }
    fun intervaloParto(fecha: Long): String = "${Validaciones.fechaTexto(sumarDias(fecha,-7))} – ${Validaciones.fechaTexto(sumarDias(fecha,7))}"
}
