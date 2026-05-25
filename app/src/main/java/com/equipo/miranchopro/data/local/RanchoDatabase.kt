package com.equipo.miranchopro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.equipo.miranchopro.data.local.dao.*
import com.equipo.miranchopro.data.model.*

@Database(
    entities = [
        Usuario::class, 
        Lote::class, 
        Animal::class, 
        Insumo::class, 
        Medicamento::class, 
        Vacunacion::class, 
        Enfermedad::class
    ], 
    version = 7, 
    exportSchema = false
)
abstract class RanchoDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun loteDao(): LoteDao
    abstract fun animalDao(): AnimalDao
    abstract fun insumoDao(): InsumoDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun vacunacionDao(): VacunacionDao
    abstract fun enfermedadDao(): EnfermedadDao

    companion object {
        @Volatile
        private var INSTANCE: RanchoDatabase? = null

        fun getDatabase(context: Context): RanchoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RanchoDatabase::class.java,
                    "rancho_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
