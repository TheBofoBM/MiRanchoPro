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
    private val enfermedadDao: EnfermedadDao
) {
    // Medicamentos
    fun obtenerMedicamentos(): Flow<List<Medicamento>> = medicamentoDao.obtenerTodos()
    suspend fun insertarMedicamento(medicamento: Medicamento) = medicamentoDao.insertar(medicamento)
    suspend fun actualizarMedicamento(medicamento: Medicamento) = medicamentoDao.actualizar(medicamento)
    suspend fun eliminarMedicamento(medicamento: Medicamento) = medicamentoDao.eliminar(medicamento)

    // Vacunaciones
    fun obtenerVacunaciones(): Flow<List<Vacunacion>> = vacunacionDao.obtenerTodas()
    suspend fun registrarVacunacion(vacunacion: Vacunacion) = vacunacionDao.insertar(vacunacion)

    // Enfermedades
    fun obtenerEnfermedades(): Flow<List<Enfermedad>> = enfermedadDao.obtenerTodas()
    suspend fun registrarEnfermedad(enfermedad: Enfermedad) = enfermedadDao.insertar(enfermedad)
    suspend fun actualizarEnfermedad(enfermedad: Enfermedad) = enfermedadDao.actualizar(enfermedad)

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
