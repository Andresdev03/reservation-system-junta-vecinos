package com.example.reservationsystem.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.reservationsystem.domain.model.EstadoReserva

@Entity(tableName = "reservas")
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val solicitante: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: EstadoReserva
)