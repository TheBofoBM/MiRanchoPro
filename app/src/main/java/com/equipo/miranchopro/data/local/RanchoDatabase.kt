package com.equipo.miranchopro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.equipo.miranchopro.data.local.dao.AnimalDao
import com.equipo.miranchopro.data.local.dao.LoteDao
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.data.model.Usuario

// Incrementamos la versión de 3 a 4 debido al cambio en el modelo Animal
@Database(entities = [Usuario::class, Lote::class, Animal::class], version = 4, exportSchema = false)
abstract class RanchoDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun loteDao(): LoteDao
    abstract fun animalDao(): AnimalDao

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
                .fallbackToDestructiveMigration() // Esto permitirá que la app no truene al cambiar la versión
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
