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

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE animales ADD COLUMN madreId TEXT")
            db.execSQL("ALTER TABLE animales ADD COLUMN partoId TEXT")
            db.execSQL("ALTER TABLE animales ADD COLUMN fechaBaja INTEGER")
            db.execSQL("ALTER TABLE animales ADD COLUMN motivoBaja TEXT")
            db.execSQL("ALTER TABLE medicamentos ADD COLUMN stockMinimo INTEGER NOT NULL DEFAULT 5")
            db.execSQL("ALTER TABLE medicamentos ADD COLUMN fechaCaducidad INTEGER")
            db.execSQL("ALTER TABLE insumos ADD COLUMN fechaCaducidad INTEGER")
            db.execSQL("ALTER TABLE vacunaciones ADD COLUMN recordatorioCompletado INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS pesajes (id TEXT NOT NULL PRIMARY KEY, animalId TEXT NOT NULL, fecha INTEGER NOT NULL, peso REAL NOT NULL, notas TEXT NOT NULL, responsable TEXT NOT NULL, registradoEn INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX index_pesajes_animalId ON pesajes(animalId)")
            db.execSQL("CREATE TABLE IF NOT EXISTS eventos_animales (id TEXT NOT NULL PRIMARY KEY, animalId TEXT NOT NULL, fecha INTEGER NOT NULL, tipo TEXT NOT NULL, detalle TEXT NOT NULL, responsable TEXT NOT NULL)")
            db.execSQL("CREATE INDEX index_eventos_animales_animalId ON eventos_animales(animalId)")
            db.execSQL("CREATE TABLE IF NOT EXISTS partos (id TEXT NOT NULL PRIMARY KEY, madreId TEXT NOT NULL, gestacionId INTEGER, fecha INTEGER NOT NULL, resultado TEXT NOT NULL, notas TEXT NOT NULL, responsable TEXT NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX index_partos_gestacionId ON partos(gestacionId)")
            db.execSQL("CREATE INDEX index_partos_madreId ON partos(madreId)")
            db.execSQL("CREATE TABLE IF NOT EXISTS movimientos_existencias (id TEXT NOT NULL PRIMARY KEY, productoId TEXT NOT NULL, clase TEXT NOT NULL, nombre TEXT NOT NULL, fecha INTEGER NOT NULL, tipo TEXT NOT NULL, cantidad REAL NOT NULL, saldo REAL NOT NULL, unidad TEXT NOT NULL, motivo TEXT NOT NULL, responsable TEXT NOT NULL)")
            db.execSQL("CREATE INDEX index_movimientos_existencias_productoId ON movimientos_existencias(productoId)")
            // El saldo previo es un punto de partida, no un movimiento histórico inventado.
            db.execSQL("INSERT INTO movimientos_existencias SELECT 'inicial-i-' || id, id, 'Insumo', nombre, strftime('%s','now')*1000, 'Saldo inicial', cantidad, cantidad, unidadMedida, 'Saldo al actualizar la aplicación', '' FROM insumos")
            db.execSQL("INSERT INTO movimientos_existencias SELECT 'inicial-m-' || id, id, 'Medicamento', nombre, strftime('%s','now')*1000, 'Saldo inicial', stock, stock, unidadMedida, 'Saldo al actualizar la aplicación', '' FROM medicamentos")
            db.execSQL("INSERT INTO pesajes SELECT 'referencia-' || idArete, idArete, strftime('%s','now','localtime','start of day','utc')*1000, peso, 'Peso disponible al actualizar; no representa un pesaje histórico', '', strftime('%s','now')*1000 FROM animales WHERE peso > 0 AND estado NOT IN ('Baja','Pendiente')")
        }
    }
    val ALL: Array<Migration> = arrayOf(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)

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
