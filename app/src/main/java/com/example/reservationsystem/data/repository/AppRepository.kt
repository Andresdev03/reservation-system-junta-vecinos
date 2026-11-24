package com.example.reservationsystem.data.repository

import com.example.reservationsystem.data.local.dao.MovimientoDao
import com.example.reservationsystem.data.local.dao.ReservaDao
import com.example.reservationsystem.data.local.entity.MovimientoEntity
import com.example.reservationsystem.data.local.entity.ReservaEntity
import com.example.reservationsystem.domain.model.EstadoReserva


class AppRepository(
    private val movimientoDao: MovimientoDao,
    private val reservaDao: ReservaDao
) {
    fun getMovimientos() = movimientoDao.getAllMovimientos()
    fun getReservas() = reservaDao.getAllReservas()
    fun getTotalIngresos() = movimientoDao.getTotalIngresos()
    fun getTotalGastos() = movimientoDao.getTotalGastos()

    suspend fun insertMovimiento(movimiento: MovimientoEntity) {
        movimientoDao.insertMovimiento(movimiento)
    }

    suspend fun insertReserva(reserva: ReservaEntity) {
        reservaDao.insertReserva(reserva)
    }

    suspend fun deleteMovimiento(movimiento: MovimientoEntity) {
        movimientoDao.deleteMovimiento(movimiento)
    }

    suspend fun getMovimientoById(id: Int): MovimientoEntity? {
        return movimientoDao.getMovimientoById(id)
    }

    suspend fun updateMovimiento(movimiento: MovimientoEntity) {
        movimientoDao.updateMovimiento(movimiento)
    }

    suspend fun existeReservaConSolapamiento(
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean {
        return reservaDao.existeReservaConSolapamiento(
            fecha = fecha,
            horaInicio = horaInicio,
            horaFin = horaFin
        )
    }

    suspend fun updateReserva(reserva: ReservaEntity) {
        reservaDao.updateReserva(reserva)
    }

    suspend fun deleteReserva(reserva: ReservaEntity) {
        reservaDao.deleteReserva(reserva)
    }
}