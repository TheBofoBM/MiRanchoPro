package com.equipo.miranchopro.utils

import com.equipo.miranchopro.data.model.*
import org.junit.Assert.*
import org.junit.Test

class AgendaRanchoTest {
    private val trabajador=Usuario("empleado@example.test","","TRABAJADOR")
    @Test fun empleadoSoloVeSusTareasPendientes() {
        val tareas=listOf(Tarea(titulo="Propia",descripcion="",responsable=trabajador.correo,prioridad=Prioridad.MEDIA,fecha="01/10/2026"),Tarea(id=2,titulo="Ajena",descripcion="",responsable="otro@example.test",prioridad=Prioridad.MEDIA,fecha="01/10/2026"),Tarea(id=3,titulo="Hecha",descripcion="",responsable=trabajador.correo,prioridad=Prioridad.MEDIA,fecha="01/10/2026",estaHecha=true))
        val agenda=AgendaRancho.construir(emptyList(),tareas,emptyList(),emptyList(),trabajador)
        assertEquals(listOf("Propia"),agenda.map { it.titulo })
    }
    @Test fun animalesDeBajaNoGeneranAvisosDeVacuna() {
        val a=Animal(idArete="A",peso=300.0,estado="Baja")
        val v=Vacunacion(nombreVacuna="Vacuna",dosis="",fechaAplicacion="01/10/2026",idAnimal="A",proximaDosis="01/11/2026")
        assertTrue(AgendaRancho.construir(listOf(a),emptyList(),listOf(v),emptyList(),trabajador).isEmpty())
    }
    @Test fun fechaDePartoNoSeAnticipaDosVeces() {
        val a=Animal(idArete="A",peso=300.0);val fecha=Validaciones.fecha("14/10/2026")!!
        val g=Embarazo(vacaId="A",fechaInseminacion=1,tipo="Monta Natural",fechaProbableParto=fecha)
        assertEquals(fecha,AgendaRancho.construir(listOf(a),emptyList(),emptyList(),listOf(g),trabajador).single().fecha)
    }
    @Test fun intervaloCruzaMesSinSumarDuracionesFijas() {
        assertEquals("25/02/2024 – 10/03/2024",AgendaRancho.intervaloParto(Validaciones.fecha("03/03/2024")!!))
    }
}
