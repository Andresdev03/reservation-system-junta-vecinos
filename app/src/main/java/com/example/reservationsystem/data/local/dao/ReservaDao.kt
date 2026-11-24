package com.example.reservationsystem.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.reservationsystem.data.local.entity.ReservaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {
    @Query(value = "SELECT * FROM reservas ORDER BY id DESC")
    fun getAllReservas(): Flow<List<ReservaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserva(reserva: ReservaEntity)

    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM reservas
        WHERE fecha = :fecha
          AND estado != 'RECHAZADA'
          AND horaInicio < :horaFin
          AND horaFin > :horaInicio
    )
""")
    suspend fun existeReservaConSolapamiento(
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean

    @Update
    suspend fun updateReserva(reserva: ReservaEntity)

    @Delete
    suspend fun deleteReserva(reserva: ReservaEntity)
}