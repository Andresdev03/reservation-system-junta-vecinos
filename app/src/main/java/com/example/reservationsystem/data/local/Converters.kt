package com.example.reservationsystem.data.local

import androidx.room.TypeConverter
import com.example.reservationsystem.domain.model.EstadoReserva
import com.example.reservationsystem.domain.model.TipoMovimiento

class Converters {

    @TypeConverter
    fun fromTipoMovimiento(value: TipoMovimiento): String = value.name

    @TypeConverter
    fun toTipoMovimiento(value: String): TipoMovimiento = TipoMovimiento.valueOf(value)

    @TypeConverter
    fun fromEstadoReserva(value: EstadoReserva): String = value.name

    @TypeConverter
    fun toEstadoReserva(value: String): EstadoReserva = EstadoReserva.valueOf(value)
}