package com.equipo.miranchopro.data.local

import androidx.room.TypeConverter
import com.equipo.miranchopro.data.model.Prioridad

class Converters {
    @TypeConverter
    fun fromPrioridad(prioridad: Prioridad): String {
        return prioridad.name
    }

    @TypeConverter
    fun toPrioridad(value: String): Prioridad {
        return Prioridad.valueOf(value)
    }
}
