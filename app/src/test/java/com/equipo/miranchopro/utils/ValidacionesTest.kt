package com.equipo.miranchopro.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ValidacionesTest {
    @Test fun aceptaDecimalesConComaYPunto() {
        assertEquals(45.5, Validaciones.decimal("45,5")!!, 0.0)
        assertEquals(45.5, Validaciones.decimal(" 45.5 ")!!, 0.0)
    }
    @Test fun rechazaNegativosNoFinitosYTexto() {
        listOf("-5", "NaN", "Infinity", "1e999", "abc", "1,2.3", "").forEach { assertNull(it, Validaciones.decimal(it)) }
    }
    @Test fun rechazaFechasImposiblesYContenidoSobrante() {
        listOf("31/02/2026", "29/02/2025", "00/01/2026", "01/13/2026", "01/01/2026 extra", "1/1/2026").forEach { assertNull(it, Validaciones.fecha(it)) }
        assertNotNull(Validaciones.fecha("29/02/2024"))
    }
    @Test fun noInventaNacimientoDesconocido() {
        assertNull(Validaciones.fecha(""))
        assertEquals("", Validaciones.fechaTexto(null))
        assertEquals("Desconocida", Validaciones.edad(null))
        assertNull(Validaciones.errorFecha("", "Nacimiento", opcional = true))
    }
    @Test fun rechazaNacimientoFuturo() {
        assertNotNull(Validaciones.errorFecha("01/01/2999", "Nacimiento"))
        assertNull(Validaciones.errorFecha("01/01/2999", "Próxima dosis", permitirFutura = true))
    }
    @Test fun normalizaCorreosYRechazaFormatoIncompleto() {
        assertEquals("dueno@example.test", Validaciones.correo(" DUENO@EXAMPLE.TEST "))
        assertTrue(Validaciones.correoValido("dueno@example.test"))
        listOf("a@.", "a.b", "@example.test", "a@@example.test", "a@-host.test", "a b@example.test").forEach { assertFalse(it, Validaciones.correoValido(it)) }
    }
    @Test fun edadRespetaDiaDelCumpleanos() {
        val hoy = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 14, 12, 0, 0) }.timeInMillis
        assertEquals("11 meses", Validaciones.edad(Validaciones.fecha("15/10/2025"), hoy))
        assertEquals("1 año, 0 meses", Validaciones.edad(Validaciones.fecha("14/10/2025"), hoy))
    }
    @Test fun pesoInvalidoNoSeConvierteEnCero() {
        assertTrue(Validaciones.erroresAnimal("A1", "texto", "").containsKey("peso"))
        assertTrue(Validaciones.erroresAnimal("A1", "0", "").containsKey("peso"))
        assertTrue(Validaciones.erroresAnimal("A1", "45,5", "").isEmpty())
    }
    @Test fun protegeClaveConSalAleatoria() {
        val primera = Claves.proteger("Ganado2026")
        val segunda = Claves.proteger("Ganado2026")
        assertNotEquals(primera, segunda)
        assertFalse(primera.contains("Ganado2026"))
        assertTrue(Claves.verificar("Ganado2026", primera))
        assertFalse(Claves.verificar("incorrecta", primera))
    }
}
