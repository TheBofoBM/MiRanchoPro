package com.equipo.miranchopro.data.repository

import com.equipo.miranchopro.data.local.dao.LoteDao
import com.equipo.miranchopro.data.model.Lote
import kotlinx.coroutines.flow.Flow

class LoteRepository(private val loteDao: LoteDao, private val responsable: () -> String = { "" }) {

    fun obtenerTodosLosLotes(): Flow<List<Lote>> {
        return loteDao.obtenerTodosLosLotes()
    }

    suspend fun insertarLote(lote: Lote) {
        loteDao.guardarValidado(lote)
    }

    suspend fun actualizarLote(lote: Lote) {
        loteDao.guardarValidado(lote)
    }

    suspend fun eliminarLote(lote: Lote) {
        loteDao.eliminarValidado(lote)
    }

    suspend fun mover(origen: Lote, destino: Lote) = loteDao.moverValidado(origen.id, destino.id, responsable())
}
