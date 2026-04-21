package com.equipo.miranchopro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.equipo.miranchopro.data.local.dao.LoteDao
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.data.model.Usuario

@Database(entities = [Usuario::class, Lote::class], version = 1, exportSchema = false)
abstract class RanchoDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun loteDao(): LoteDao

    companion object {
        @Volatile
        private var INSTANCE: RanchoDatabase? = null

        fun getDatabase(context: Context): RanchoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RanchoDatabase::class.java,
                    "rancho_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}