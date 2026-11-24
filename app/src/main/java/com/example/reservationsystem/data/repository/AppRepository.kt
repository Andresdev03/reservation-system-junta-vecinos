package com.example.reservationsystem.data.repository

import com.example.reservationsystem.data.local.dao.MovimientoDao
import com.example.reservationsystem.data.local.dao.ReservaDao
import com.example.reservationsystem.data.local.entity.MovimientoEntity
import com.example.reservationsystem.data.local.entity.ReservaEntity
import com.example.reservationsystem.domain.model.EstadoReserva
import com.example.reservationsystem.domain.auth.Permiso
import com.example.reservationsystem.domain.auth.UsuarioSesion

class AppRepository(
    private val movimientoDao: MovimientoDao,
    private val reservaDao: ReservaDao
) {
    fun getMovimientos() = movimientoDao.getAllMovimientos()
    fun getReservas() = reservaDao.getAllReservas()
    fun getTotalIngresos() = movimientoDao.getTotalIngresos()
    fun getTotalGastos() = movimientoDao.getTotalGastos()

    suspend fun insertMovimiento(
        movimiento: MovimientoEntity,
        usuario: UsuarioSesion
    ) {
        require(usuario.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)) {
            "Tu cuenta no puede modificar movimientos."
        }

        movimientoDao.insertMovimiento(movimiento)
    }

    suspend fun updateMovimiento(
        movimiento: MovimientoEntity,
        usuario: UsuarioSesion
    ) {
        require(usuario.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)) {
            "Tu cuenta no puede modificar movimientos."
        }

        val actualizados = movimientoDao.updateMovimiento(movimiento)

        check(actualizados == 1) {
            "El movimiento ya no existe o no pudo actualizarse."
        }
    }

    suspend fun deleteMovimiento(
        movimiento: MovimientoEntity,
        usuario: UsuarioSesion
    ) {
        require(usuario.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)) {
            "Tu cuenta no puede modificar movimientos."
        }

        val eliminados = movimientoDao.deleteMovimiento(movimiento)

        check(eliminados == 1) {
            "El movimiento ya no existe o no pudo eliminarse."
        }
    }

    suspend fun insertReserva(reserva: ReservaEntity) {
        reservaDao.insertReserva(reserva)
    }

    suspend fun getMovimientoById(id: Int): MovimientoEntity? {
        return movimientoDao.getMovimientoById(id)
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

    suspend fun deleteReserva(
        reserva: ReservaEntity,
        usuario: UsuarioSesion
    ) {
        require(usuario.tienePermiso(Permiso.GESTIONAR_RESERVAS)) {
            "Tu cuenta no puede administrar reservas."
        }

        reservaDao.deleteReserva(reserva)
    }

    suspend fun deleteReserva(reserva: ReservaEntity) {
        reservaDao.deleteReserva(reserva)
    }

    fun getMisReservas(usuario: UsuarioSesion) =
        reservaDao.getReservasPorUsuario(usuario.id)

    suspend fun crearSolicitudReserva(
        usuario: UsuarioSesion,
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean {
        require(usuario.tienePermiso(Permiso.SOLICITAR_RESERVA)) {
            "Tu cuenta no puede solicitar reservas."
        }

        require(usuario.id.isNotBlank()) {
            "No se pudo identificar al usuario."
        }

        return reservaDao.insertarSiDisponible(
            ReservaEntity(
                solicitanteId = usuario.id,
                solicitante = usuario.nombre,
                fecha = fecha.trim(),
                horaInicio = horaInicio.trim(),
                horaFin = horaFin.trim(),
                estado = EstadoReserva.PENDIENTE
            )
        )
    }

    suspend fun cancelarMiReserva(
        usuario: UsuarioSesion,
        reservaId: Int
    ) {
        require(usuario.id.isNotBlank()) {
            "No se pudo identificar al usuario."
        }

        val actualizadas = reservaDao.cancelarPendientePropia(
            reservaId = reservaId,
            usuarioId = usuario.id
        )

        check(actualizadas == 1) {
            "No se pudo cancelar: la solicitud ya no está pendiente " +
                    "o no pertenece a tu cuenta."
        }
    }

    suspend fun resolverSolicitudPendiente(
        reservaId: Int,
        nuevoEstado: EstadoReserva,
        usuario: UsuarioSesion
    ) {
        require(usuario.tienePermiso(Permiso.GESTIONAR_RESERVAS)) {
            "Tu cuenta no puede administrar reservas."
        }

        require(
            nuevoEstado == EstadoReserva.CONFIRMADA ||
                    nuevoEstado == EstadoReserva.RECHAZADA
        ) {
            "Cambio de estado no permitido."
        }

        val actualizadas = reservaDao.cambiarEstadoPendiente(
            reservaId = reservaId,
            nuevoEstado = nuevoEstado
        )

        check(actualizadas == 1) {
            "La solicitud ya no está pendiente. Revisa su estado actual."
        }
    }
}