package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.MedicamentoDao
import com.equipo.miranchopro.data.local.dao.VacunacionDao
import com.equipo.miranchopro.data.local.dao.EnfermedadDao
import com.equipo.miranchopro.data.model.Medicamento
import com.equipo.miranchopro.data.model.Vacunacion
import com.equipo.miranchopro.data.model.Enfermedad
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class SaludRepository(
    private val medicamentoDao: MedicamentoDao,
    private val vacunacionDao: VacunacionDao,
    private val enfermedadDao: EnfermedadDao,
    private val animalDao: com.equipo.miranchopro.data.local.dao.AnimalDao,
    private val responsable: () -> String = { "" }
) {
    fun obtenerAnimales() = animalDao.obtenerTodos()
    private suspend fun validarAnimal(id: String) {
        val animal = animalDao.obtenerPorId(id) ?: throw IllegalArgumentException("Selecciona un animal existente")
        require(animal.estado !in listOf("Baja","Pendiente")) { "Selecciona un animal activo" }
    }
    private fun validarMedicamento(m: Medicamento) {
        require(m.nombre.isNotBlank() && m.unidadMedida.isNotBlank() && m.stock>=0) { "Revisa nombre, unidad y existencias" }
    }
    // Medicamentos
    fun obtenerMedicamentos(): Flow<List<Medicamento>> = medicamentoDao.obtenerTodos()
    suspend fun buscarMedicamentoPorNombre(nombre: String): Medicamento? = medicamentoDao.buscarPorNombre(nombre)
    suspend fun insertarMedicamento(medicamento: Medicamento) {
        validarMedicamento(medicamento)
        require(medicamentoDao.buscarPorNombre(medicamento.nombre)?.id.let { it==null || it==medicamento.id }) { "Este medicamento ya existe" }
        medicamentoDao.guardarValidado(medicamento, true, responsable())
    }
    suspend fun actualizarMedicamento(medicamento: Medicamento) {
        validarMedicamento(medicamento)
        require(medicamentoDao.buscarPorNombre(medicamento.nombre)?.id.let { it==null || it==medicamento.id }) { "Este medicamento ya existe" }
        medicamentoDao.guardarValidado(medicamento, false, responsable())
    }
    suspend fun eliminarMedicamento(medicamento: Medicamento) = medicamentoDao.eliminarValidado(medicamento,responsable())

    // Vacunaciones
    fun obtenerVacunaciones(): Flow<List<Vacunacion>> = vacunacionDao.obtenerTodas()
    suspend fun registrarVacunacion(vacunacion: Vacunacion) {
        validarAnimal(vacunacion.idAnimal)
        require(vacunacion.nombreVacuna.isNotBlank() && vacunacion.dosis.isNotBlank()) { "Vacuna y dosis son obligatorias" }
        val reglas = com.equipo.miranchopro.utils.Validaciones
        require(reglas.errorFecha(vacunacion.fechaAplicacion,"La fecha")==null) { "Ingresa una fecha de aplicación válida" }
        val proxima=vacunacion.proximaDosis
        if (!proxima.isNullOrBlank()) require(reglas.errorFecha(proxima,"La próxima fecha",permitirFutura=true)==null && reglas.fecha(proxima)!!>=reglas.fecha(vacunacion.fechaAplicacion)!!) { "La próxima fecha debe ser válida y posterior a la aplicación" }
        vacunacionDao.registrarValidado(vacunacion)
    }

    // Enfermedades
    fun obtenerEnfermedades(): Flow<List<Enfermedad>> = enfermedadDao.obtenerTodas()
    suspend fun registrarEnfermedad(enfermedad: Enfermedad) { validarEnfermedad(enfermedad); enfermedadDao.guardarValidado(enfermedad,true) }
    suspend fun actualizarEnfermedad(enfermedad: Enfermedad) { validarEnfermedad(enfermedad); enfermedadDao.guardarValidado(enfermedad,false) }
    private suspend fun validarEnfermedad(e: Enfermedad) {
        validarAnimal(e.idAnimal)
        require(e.nombre.isNotBlank() && e.sintomas.isNotBlank()) { "Nombre y síntomas son obligatorios" }
        require(com.equipo.miranchopro.utils.Validaciones.errorFecha(e.fechaDeteccion,"La fecha")==null) { "Ingresa una fecha de detección válida" }
    }

    suspend fun registrarAlertaEnfermedadRapida(): Result<String> {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaActual = sdf.format(Date())
            val idAlerta = "ALERTA-${System.currentTimeMillis() % 10000}"
            
            val enfermedadPendiente = Enfermedad(
                id = UUID.randomUUID().toString(),
                nombre = "Alerta Rápida",
                sintomas = "Por definir (Reportado via Sensor)",
                tratamiento = "Pendiente de revisión",
                idAnimal = "Pndte. Arete", // Marcado para completar después
                fechaDeteccion = fechaActual,
                estado = "Pendiente",
                notas = "Reporte generado por sensor de movimiento"
            )
            
            enfermedadDao.insertar(enfermedadPendiente)
            Result.success(idAlerta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
