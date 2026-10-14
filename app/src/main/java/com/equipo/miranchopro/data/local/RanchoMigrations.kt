package com.equipo.miranchopro.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Actualizaciones aditivas: ninguna migración elimina tablas o registros. */
object RanchoMigrations {
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            agregarFechaNacimientoSiFalta(db)
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // La versión 10 fue una versión de desarrollo sin esquema exportado.
            agregarFechaNacimientoSiFalta(db)
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `embarazos` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `vacaId` TEXT NOT NULL,
                    `fechaInseminacion` INTEGER NOT NULL,
                    `tipo` TEXT NOT NULL,
                    `fechaProbableParto` INTEGER NOT NULL,
                    `estado` TEXT NOT NULL,
                    `notas` TEXT NOT NULL,
                    FOREIGN KEY(`vacaId`) REFERENCES `animales`(`idArete`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_embarazos_vacaId` ON `embarazos` (`vacaId`)")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)

    private fun agregarFechaNacimientoSiFalta(db: SupportSQLiteDatabase) {
        val existe = db.query("PRAGMA table_info(`animales`)").use { cursor ->
            val nombreColumna = cursor.getColumnIndexOrThrow("name")
            var encontrada = false
            while (cursor.moveToNext()) {
                if (cursor.getString(nombreColumna) == "fechaNacimiento") {
                    encontrada = true
                    break
                }
            }
            encontrada
        }
        if (!existe) {
            // NULL conserva la distinción entre nacimiento desconocido y registro.
            db.execSQL("ALTER TABLE `animales` ADD COLUMN `fechaNacimiento` INTEGER")
        }
    }
}
