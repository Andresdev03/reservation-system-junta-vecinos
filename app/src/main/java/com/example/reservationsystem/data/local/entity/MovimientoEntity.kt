package com.example.reservationsystem.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.reservationsystem.domain.model.TipoMovimiento

@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: TipoMovimiento,
    val fecha: String,
    val rutaComprobante: String? = null
)