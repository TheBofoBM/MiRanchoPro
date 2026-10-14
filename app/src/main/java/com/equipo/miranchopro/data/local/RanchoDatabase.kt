package com.equipo.miranchopro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
        Enfermedad::class,
        Tarea::class,
        Embarazo::class
    ], 
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RanchoDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun loteDao(): LoteDao
    abstract fun animalDao(): AnimalDao
    abstract fun insumoDao(): InsumoDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun vacunacionDao(): VacunacionDao
    abstract fun enfermedadDao(): EnfermedadDao
    abstract fun tareaDao(): TareaDao
    abstract fun embarazoDao(): EmbarazoDao

    companion object {
        @Volatile
        private var INSTANCE: RanchoDatabase? = null

        internal fun builder(context: Context, name: String): RoomDatabase.Builder<RanchoDatabase> {
            return Room.databaseBuilder(
                context.applicationContext,
                RanchoDatabase::class.java,
                name
            ).addMigrations(*RanchoMigrations.ALL).addCallback(object : Callback() {
                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Conversión de credenciales antiguas; se conserva el acceso existente.
                    val pendientes = mutableListOf<Pair<String, String>>()
                    db.query("SELECT correo, contrasena FROM usuarios").use { cursor ->
                        while (cursor.moveToNext()) {
                            val clave = cursor.getString(1)
                            if (!com.equipo.miranchopro.utils.Claves.esHash(clave)) pendientes += cursor.getString(0) to clave
                        }
                    }
                    pendientes.forEach { (correo, clave) ->
                        db.execSQL("UPDATE usuarios SET contrasena = ? WHERE correo = ?", arrayOf(com.equipo.miranchopro.utils.Claves.proteger(clave), correo))
                    }
                }
            })
        }

        fun getDatabase(context: Context): RanchoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = builder(context, "rancho_database").build()
                INSTANCE = instance
                instance
            }
        }
    }
}
