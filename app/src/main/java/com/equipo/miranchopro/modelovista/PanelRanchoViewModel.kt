package com.equipo.miranchopro.modelovista

import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.RanchoDatabase
import com.equipo.miranchopro.data.model.*
import kotlinx.coroutines.flow.*

class PanelRanchoViewModel(private val db: RanchoDatabase, private val responsable: () -> String) : OperacionViewModel() {
    val animales=db.animalDao().obtenerTodos().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val tareas=db.tareaDao().obtenerTodas().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val vacunas=db.vacunacionDao().obtenerTodas().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val enfermedades=db.enfermedadDao().obtenerTodas().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val gestaciones=db.historialDao().gestaciones().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val historialGestaciones=db.historialDao().historialGestaciones().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val pesajes=db.historialDao().pesajes().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val eventos=db.historialDao().eventos().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val partos=db.historialDao().partos().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val movimientos=db.historialDao().movimientos().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val insumos=db.insumoDao().obtenerTodos().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    val medicamentos=db.medicamentoDao().obtenerTodos().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    fun pesar(id: String,fecha: Long,peso: Double,notas: String) = operar { db.historialDao().registrarPesaje(Pesaje(animalId=id,fecha=fecha,peso=peso,notas=notas,responsable=responsable())) }
    fun parto(madre: String,gestacion: Int?,fecha: Long,resultado: String,notas: String,crias: List<Animal>) = operar { db.historialDao().registrarParto(Parto(madreId=madre,gestacionId=gestacion,fecha=fecha,resultado=resultado,notas=notas,responsable=responsable()),crias) }
    fun gestacion(id: String, fecha: Long,tipo: String) = operar {
        db.embarazoDao().registrarValidado(Embarazo(vacaId=id,fechaInseminacion=fecha,tipo=tipo,fechaProbableParto=com.equipo.miranchopro.utils.AgendaRancho.sumarDias(fecha,283)))
    }
    fun cerrarGestacion(id: String) = operar { db.embarazoDao().cerrarValidado(id,"Interrumpido",responsable()) }
    fun moverMedicamento(id: String,cantidad: Int,entrada: Boolean,motivo: String) = operar { db.medicamentoDao().moverStock(id,cantidad,entrada,motivo,responsable()) }
    fun moverInsumo(id: String,cantidad: Double,entrada: Boolean,motivo: String) = operar { require((if(entrada) db.insumoDao().sumarStock(id,cantidad,responsable(),motivo) else db.insumoDao().descontarStock(id,cantidad,responsable(),motivo))==1) { "Revisa el producto y las existencias disponibles" } }
    fun atenderRecordatorio(id: String) = operar { db.vacunacionDao().atenderRecordatorio(id) }
    fun cancelarPendiente(id: String) = operar { require(db.animalDao().eliminarPendientePorId(id)==1) { "El nacimiento ya cambió; revisa el listado" } }
    fun configurarInsumo(i: Insumo) = operar { db.insumoDao().insertarInsumo(i,responsable()) }
    fun configurarMedicamento(m: Medicamento) = operar { db.medicamentoDao().guardarValidado(m,false,responsable()) }
}
