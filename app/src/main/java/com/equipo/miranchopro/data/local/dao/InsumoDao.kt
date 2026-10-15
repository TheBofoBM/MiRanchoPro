package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InsumoDao {
    @Query("SELECT * FROM insumos")
    fun obtenerTodos(): Flow<List<Insumo>>

    @Query("SELECT * FROM insumos WHERE id = :id") suspend fun porId(id: String): Insumo?
    @Query("SELECT COUNT(*) FROM insumos WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) AND id != :id") suspend fun duplicados(nombre: String, id: String): Int
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertarDatos(insumo: Insumo)
    @Insert suspend fun movimiento(movimiento: MovimientoExistencia)

    @Update
    suspend fun actualizarInsumo(insumo: Insumo)

    @Delete
    suspend fun borrarDatos(insumo: Insumo)

    @Query("UPDATE insumos SET cantidad = cantidad - :gasto WHERE id = :idInsumo AND :gasto > 0 AND cantidad >= :gasto")
    suspend fun descontarDatos(idInsumo: String, gasto: Double): Int

    @Query("UPDATE insumos SET cantidad = cantidad + :cantidad WHERE id = :id AND :cantidad > 0 AND cantidad + :cantidad <= 1.7976931348623157E308")
    suspend fun sumarDatos(id: String, cantidad: Double): Int

    @Transaction suspend fun insertarInsumo(insumo: Insumo, responsable: String = "") {
        require(insumo.nombre.isNotBlank() && insumo.unidadMedida.isNotBlank() && insumo.cantidad.isFinite() && insumo.cantidad >= 0 && insumo.stockMinimo.isFinite() && insumo.stockMinimo >= 0) { "Revisa nombre, unidad y cantidades" }
        require(duplicados(insumo.nombre, insumo.id) == 0) { "Este insumo ya existe" }
        val previo = porId(insumo.id)
        require(previo == null || previo.cantidad == 0.0 || previo.unidadMedida == insumo.unidadMedida) { "Vacía las existencias antes de cambiar la unidad" }
        if (previo == null) insertarDatos(insumo) else actualizarInsumo(insumo)
        val diferencia = insumo.cantidad - (previo?.cantidad ?: 0.0)
        if (previo == null || diferencia != 0.0) movimiento(MovimientoExistencia(productoId=insumo.id, clase="Insumo", nombre=insumo.nombre, tipo=if(previo==null) "Saldo inicial" else "Ajuste", cantidad=diferencia, saldo=insumo.cantidad, unidad=insumo.unidadMedida, motivo=if(previo==null) "Alta de insumo" else "Corrección de existencias", responsable=responsable))
    }
    @Transaction suspend fun descontarStock(idInsumo: String, gasto: Double, responsable: String = "", motivo: String = "Consumo registrado"): Int {
        if(!gasto.isFinite() || gasto<=0) return 0
        require(motivo.isNotBlank()) { "Indica el motivo" }
        val cambio=descontarDatos(idInsumo,gasto)
        if(cambio==1) porId(idInsumo)!!.let { movimiento(MovimientoExistencia(productoId=it.id,clase="Insumo",nombre=it.nombre,tipo="Consumo",cantidad=-gasto,saldo=it.cantidad,unidad=it.unidadMedida,motivo=motivo,responsable=responsable)) }
        return cambio
    }
    @Transaction suspend fun sumarStock(id: String, cantidad: Double, responsable: String = "", motivo: String = "Abastecimiento registrado"): Int {
        if(!cantidad.isFinite() || cantidad<=0) return 0
        require(motivo.isNotBlank()) { "Indica el motivo" }
        val cambio=sumarDatos(id,cantidad)
        if(cambio==1) porId(id)!!.let { movimiento(MovimientoExistencia(productoId=it.id,clase="Insumo",nombre=it.nombre,tipo="Entrada",cantidad=cantidad,saldo=it.cantidad,unidad=it.unidadMedida,motivo=motivo,responsable=responsable)) }
        return cambio
    }
    @Transaction suspend fun eliminarInsumo(insumo: Insumo, responsable: String = "") {
        val actual=porId(insumo.id) ?: return
        movimiento(MovimientoExistencia(productoId=actual.id,clase="Insumo",nombre=actual.nombre,tipo="Retiro",cantidad=-actual.cantidad,saldo=0.0,unidad=actual.unidadMedida,motivo="Producto retirado del inventario",responsable=responsable))
        borrarDatos(actual)
    }
}
