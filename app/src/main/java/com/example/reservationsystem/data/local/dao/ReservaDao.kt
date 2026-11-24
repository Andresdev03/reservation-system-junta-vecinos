package com.example.reservationsystem.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.reservationsystem.data.local.entity.ReservaEntity
import com.example.reservationsystem.domain.model.EstadoReserva
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReservaDao {

    @Query("SELECT * FROM reservas ORDER BY id DESC")
    abstract fun getAllReservas(): Flow<List<ReservaEntity>>

    @Query(
        """
        SELECT * FROM reservas
        WHERE solicitanteId = :usuarioId
        ORDER BY fecha DESC, horaInicio DESC, id DESC
        """
    )
    abstract fun getReservasPorUsuario(
        usuarioId: String
    ): Flow<List<ReservaEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertReserva(reserva: ReservaEntity)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM reservas
            WHERE fecha = :fecha
              AND estado IN ('PENDIENTE', 'CONFIRMADA')
              AND horaInicio < :horaFin
              AND horaFin > :horaInicio
        )
        """
    )
    abstract suspend fun existeReservaConSolapamiento(
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean

    @Transaction
    open suspend fun insertarSiDisponible(
        reserva: ReservaEntity
    ): Boolean {
        val ocupada = existeReservaConSolapamiento(
            fecha = reserva.fecha,
            horaInicio = reserva.horaInicio,
            horaFin = reserva.horaFin
        )

        if (ocupada) return false

        insertReserva(reserva)
        return true
    }

    @Query(
        """
        UPDATE reservas
        SET estado = 'CANCELADA'
        WHERE id = :reservaId
          AND solicitanteId = :usuarioId
          AND estado = 'PENDIENTE'
        """
    )
    abstract suspend fun cancelarPendientePropia(
        reservaId: Int,
        usuarioId: String
    ): Int

    @Query(
        """
        UPDATE reservas
        SET estado = :nuevoEstado
        WHERE id = :reservaId
          AND estado = 'PENDIENTE'
        """
    )
    abstract suspend fun cambiarEstadoPendiente(
        reservaId: Int,
        nuevoEstado: EstadoReserva
    ): Int

    @Update
    abstract suspend fun updateReserva(reserva: ReservaEntity)

    @Delete
    abstract suspend fun deleteReserva(reserva: ReservaEntity)
}