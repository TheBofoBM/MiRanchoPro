package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.TareaDao
import com.equipo.miranchopro.data.model.Tarea
import kotlinx.coroutines.flow.Flow

class TareaRepository(private val tareaDao: TareaDao) {

    fun obtenerTodas(): Flow<List<Tarea>> = tareaDao.obtenerTodas()

    suspend fun agregarTarea(tarea: Tarea) {
        tareaDao.insertarTarea(tarea)
    }

    suspend fun actualizarTarea(tarea: Tarea) {
        tareaDao.actualizarTarea(tarea)
    }

    suspend fun eliminarTarea(tarea: Tarea) {
        tareaDao.eliminarTarea(tarea)
    }
}
