package com.equipo.miranchopro.data.local.dao

import androidx.room.*
import com.equipo.miranchopro.data.model.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos")
    fun obtenerTodos(): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Medicamento?
    @Query("SELECT * FROM medicamentos WHERE id = :id") suspend fun porId(id: String): Medicamento?
    @Insert suspend fun movimiento(m: com.equipo.miranchopro.data.model.MovimientoExistencia)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(medicamento: Medicamento)

    @Update
    suspend fun actualizar(medicamento: Medicamento)

    @Delete
    suspend fun eliminar(medicamento: Medicamento)

    @Transaction
    suspend fun guardarValidado(medicamento: Medicamento, nuevo: Boolean, responsable: String = "", motivo: String = "Corrección de existencias") {
        require(medicamento.stock >= 0 && medicamento.nombre.isNotBlank() && medicamento.unidadMedida.isNotBlank()) { "Revisa nombre, unidad y existencias" }
        require(buscarPorNombre(medicamento.nombre)?.id.let { it == null || it == medicamento.id }) { "Este medicamento ya existe" }
        require(medicamento.stockMinimo >= 0) { "El mínimo no puede ser negativo" }
        val previo=porId(medicamento.id)
        require(nuevo || previo!=null) { "El medicamento ya no existe" }
        require(previo==null || previo.stock==0 || previo.unidadMedida==medicamento.unidadMedida) { "Vacía las existencias antes de cambiar la unidad" }
        if (nuevo) insertar(medicamento) else actualizar(medicamento)
        val diferencia=medicamento.stock.toDouble()-(previo?.stock ?: 0)
        if(previo==null || diferencia!=0.0) movimiento(com.equipo.miranchopro.data.model.MovimientoExistencia(productoId=medicamento.id,clase="Medicamento",nombre=medicamento.nombre,tipo=if(previo==null) "Saldo inicial" else "Ajuste",cantidad=diferencia,saldo=medicamento.stock.toDouble(),unidad=medicamento.unidadMedida,motivo=if(previo==null) "Alta de medicamento" else motivo,responsable=responsable))
    }

    @Transaction suspend fun eliminarValidado(m: Medicamento,responsable: String) {
        val actual=porId(m.id) ?: return
        movimiento(com.equipo.miranchopro.data.model.MovimientoExistencia(productoId=actual.id,clase="Medicamento",nombre=actual.nombre,tipo="Retiro",cantidad=-actual.stock.toDouble(),saldo=0.0,unidad=actual.unidadMedida,motivo="Producto retirado del inventario",responsable=responsable))
        eliminar(actual)
    }
    @Transaction suspend fun moverStock(id: String, cantidad: Int, entrada: Boolean, motivo: String, responsable: String) {
        require(cantidad>0 && motivo.isNotBlank()) { "Indica cantidad positiva y motivo" }
        val m=porId(id) ?: throw IllegalArgumentException("El medicamento ya no existe")
        val saldo=m.stock.toLong() + if(entrada) cantidad.toLong() else -cantidad.toLong()
        require(saldo in 0..Int.MAX_VALUE.toLong()) { "No hay existencias suficientes o la cantidad es demasiado grande" }
        actualizar(m.copy(stock=saldo.toInt()))
        movimiento(com.equipo.miranchopro.data.model.MovimientoExistencia(productoId=id,clase="Medicamento",nombre=m.nombre,tipo=if(entrada) "Entrada" else "Consumo",cantidad=if(entrada) cantidad.toDouble() else -cantidad.toDouble(),saldo=saldo.toDouble(),unidad=m.unidadMedida,motivo=motivo,responsable=responsable))
    }
}
