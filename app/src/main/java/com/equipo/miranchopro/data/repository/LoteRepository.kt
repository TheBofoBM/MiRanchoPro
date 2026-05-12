package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.LoteDao
import com.equipo.miranchopro.data.model.Lote
import kotlinx.coroutines.flow.Flow

class LoteRepository(private val loteDao: LoteDao) {

    fun obtenerTodosLosLotes(): Flow<List<Lote>> {
        return loteDao.obtenerTodosLosLotes()
    }

    suspend fun insertarLote(lote: Lote) {
        loteDao.insertar(lote)
    }

    suspend fun actualizarLote(lote: Lote) {
        loteDao.actualizar(lote)
    }

    suspend fun eliminarLote(lote: Lote) {
        loteDao.eliminar(lote)
    }
}
