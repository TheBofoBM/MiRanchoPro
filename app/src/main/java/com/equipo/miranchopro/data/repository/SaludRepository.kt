package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.SaludDao
import com.equipo.miranchopro.data.model.RegistroSalud
import kotlinx.coroutines.flow.Flow

class SaludRepository(private val saludDao: SaludDao) {
    fun obtenerTodos(): Flow<List<RegistroSalud>> = saludDao.obtenerTodos()

    fun obtenerPorAnimal(arete: String): Flow<List<RegistroSalud>> = saludDao.obtenerPorAnimal(arete)

    suspend fun insertar(registro: RegistroSalud) {
        saludDao.insertar(registro)
    }
}