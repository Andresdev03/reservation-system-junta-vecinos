package com.example.reservationsystem.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reservationsystem.data.local.entity.MovimientoEntity
import com.example.reservationsystem.data.local.entity.ReservaEntity
import com.example.reservationsystem.data.repository.AppRepository
import com.example.reservationsystem.domain.model.EstadoReserva
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.reservationsystem.ui.screens.movimientos.MovimientoFormState
import com.example.reservationsystem.ui.screens.movimientos.FiltroMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import com.example.reservationsystem.ui.screens.reservas.ReservaFormState
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import com.example.reservationsystem.domain.auth.Permiso
import com.example.reservationsystem.domain.auth.UsuarioSesion
import kotlinx.coroutines.CancellationException
import com.example.reservationsystem.domain.model.ResultadoOperacion
data class DashboardUiState(
    val movimientos: List<MovimientoEntity> = emptyList(),
    val reservas: List<ReservaEntity> = emptyList(),
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val mensajeExito: String? = null,
    val movimientosFiltrados: List<MovimientoEntity> = emptyList()
) {
    val saldo: Double get() = totalIngresos - totalGastos
}
class MainViewModel(
    private val repository: AppRepository,
    private val filtroMovimiento: MutableStateFlow<FiltroMovimiento> = MutableStateFlow(FiltroMovimiento.TODOS),
    private  val _mensajeExito: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    private val _mensajeExitoMovimiento: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    private val _mensajeExitoReserva: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    val mensajeExitoMovimiento: StateFlow<String?> = _mensajeExitoMovimiento,
    val filtroSeleccionado: MutableStateFlow<FiltroMovimiento> = filtroMovimiento,
    val mensajeExito: StateFlow<String?> = _mensajeExito,
    val mensajeExitoReserva: StateFlow<String?> = _mensajeExitoReserva
) : ViewModel() {

    val uiState = combine(
        repository.getMovimientos(),
        repository.getReservas(),
        repository.getTotalIngresos(),
        repository.getTotalGastos(),
        filtroMovimiento
    ) { movimientos, reservas, ingresos, gastos, filtroActual ->

        val movimientosFiltrados = when (filtroActual) {
            FiltroMovimiento.TODOS -> movimientos
            FiltroMovimiento.INGRESOS -> movimientos.filter { it.tipo.name == "INGRESO" }
            FiltroMovimiento.GASTOS -> movimientos.filter { it.tipo.name == "GASTO" }
        }

        DashboardUiState(
            movimientos = movimientos,
            reservas = reservas,
            totalIngresos = ingresos,
            totalGastos = gastos,
            movimientosFiltrados = movimientosFiltrados
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validarHora(hora: String): LocalTime? {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return try {
            LocalTime.parse(hora.trim(), formatter)
        } catch (e: Exception) {
            null
        }
    }

    fun mensajeExitoMostrado() {
        _mensajeExito.value = null
    }

    fun mensajeExitoReservaMostrado() {
        _mensajeExitoReserva.value = null
    }

    fun mensajeExitoMovimientoMostrado() {
        _mensajeExitoMovimiento.value = null
    }

    /*
    fun agregarMovimiento(
        descripcion: String,
        monto: String,
        tipo: TipoMovimiento,
        fecha: String
    ): String? {
        if (descripcion.isBlank()) return "La descripción es obligatoria"
        if (monto.isBlank()) return "El monto es obligatorio"

        val montoDouble = monto.toDoubleOrNull()
            ?: return "El monto debe ser numérico"

        if (montoDouble <= 0) return "El monto debe ser mayor que 0"

        viewModelScope.launch {
            repository.insertMovimiento(
                MovimientoEntity(
                    descripcion = descripcion,
                    monto = montoDouble,
                    tipo = tipo,
                    fecha = fecha
                )
            )
        }

        return null
    }
     */

    fun limpiarEstadoTemporal() {
        filtroMovimiento.value = FiltroMovimiento.TODOS
        _mensajeExito.value = null
        _mensajeExitoMovimiento.value = null
        _mensajeExitoReserva.value = null
    }

    fun eliminarReserva(
        reserva: ReservaEntity,
        usuario: UsuarioSesion
    ) {
        viewModelScope.launch {
            try {
                repository.deleteReserva(
                    reserva = reserva,
                    usuario = usuario
                )

                _mensajeExitoReserva.value = "Reserva eliminada correctamente."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeExitoReserva.value =
                    "No se pudo eliminar la reserva."
            }
        }
    }

    suspend fun obtenerFormularioMovimientoPorId(
        id: Int,
        usuario: UsuarioSesion
    ): MovimientoFormState? {
        require(usuario.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)) {
            "Tu cuenta no puede editar movimientos."
        }

        val movimiento = repository.getMovimientoById(id) ?: return null

        return MovimientoFormState(
            id = movimiento.id,
            descripcion = movimiento.descripcion,
            monto = movimiento.monto.toString(),
            fecha = movimiento.fecha,
            tipo = movimiento.tipo,
            rutaComprobante = movimiento.rutaComprobante
        )
    }
    fun cambiarFiltroMovimiento(nuevoFiltro: FiltroMovimiento) {
        filtroMovimiento.update { nuevoFiltro }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun guardarReserva(
        form: ReservaFormState,
        usuario: UsuarioSesion
    ): ReservaFormState {
        require(usuario.tienePermiso(Permiso.SOLICITAR_RESERVA)) {
            "Tu cuenta no puede solicitar reservas."
        }
        val solicitanteError =
            if (form.solicitante.isBlank()) "El solicitante es obligatorio" else null

        val fechaError = when {
            form.fecha.isBlank() -> "La fecha es obligatoria"

            else -> {
                val formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

                try {
                    val fechaSeleccionada = LocalDate.parse(form.fecha, formatoFecha)
                    val horaFinSeleccionada = LocalTime.parse(form.horaFin, formatoHora)

                    val finReserva = LocalDateTime.of(
                        fechaSeleccionada,
                        horaFinSeleccionada
                    )

                    val ahora = LocalDateTime.now()

                    when {
                        fechaSeleccionada.isBefore(LocalDate.now()) ->
                            "No puedes crear una reserva en una fecha pasada"

                        finReserva.isBefore(ahora) ->
                            "No puedes crear una reserva cuyo horario ya finalizó"

                        else -> null
                    }
                } catch (e: Exception) {
                    "Fecha u hora inválida"
                }
            }
        }


        val horaInicioError =
            if (form.horaInicio.isBlank()) "La hora de inicio es obligatoria" else null

        val horaFinError =
            if (form.horaFin.isBlank()) "La hora de fin es obligatoria" else null

        if (solicitanteError != null || fechaError != null ||
            horaInicioError != null || horaFinError != null
        ) {
            return form.copy(
                solicitanteError = solicitanteError,
                fechaError = fechaError,
                horaInicioError = horaInicioError,
                horaFinError = horaFinError,
                duplicidadError = null
            )
        }

        val inicio = validarHora(form.horaInicio)
        val fin = validarHora(form.horaFin)

        if (inicio == null || fin == null) {
            return form.copy(
                solicitanteError = null,
                fechaError = null,
                horaInicioError = if (inicio == null) "Usa formato HH:mm" else null,
                horaFinError = if (fin == null) "Usa formato HH:mm" else null,
                duplicidadError = null
            )
        }

        if (fin <= inicio) {
            return form.copy(
                solicitanteError = null,
                fechaError = null,
                horaFinError = "La hora de fin debe ser posterior a la hora de inicio",
                duplicidadError = null
            )
        }

        val creada = repository.crearSolicitudReserva(
            usuario = usuario,
            fecha = form.fecha,
            horaInicio = form.horaInicio,
            horaFin = form.horaFin
        )

        if (!creada) {
            return form.copy(
                solicitanteError = null,
                fechaError = null,
                horaInicioError = null,
                horaFinError = null,
                duplicidadError = "El horario se solapa con una reserva existente"
            )
        }

        _mensajeExito.value = "Reserva creada correctamente"

        return ReservaFormState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun guardarMovimiento(
        form: MovimientoFormState,
        usuario: UsuarioSesion
    ): ResultadoOperacion<MovimientoFormState> {
        if (!usuario.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)) {
            return ResultadoOperacion.Error(
                "Tu cuenta no tiene permisos para guardar movimientos."
            )
        }

        val monto = form.monto.trim().toDoubleOrNull()

        val descripcionError = if (form.descripcion.isBlank()) {
            "La descripción es obligatoria."
        } else {
            null
        }

        val montoError = when {
            form.monto.isBlank() ->
                "El monto es obligatorio."

            monto == null || !monto.isFinite() ->
                "Ingresa un monto válido."

            monto <= 0.0 ->
                "El monto debe ser mayor que cero."

            else -> null
        }

        val fechaError = when {
            form.fecha.isBlank() -> "La fecha es obligatoria."

            else -> {
                try {
                    LocalDate.parse(
                        form.fecha.trim(),
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    null
                } catch (e: java.time.format.DateTimeParseException) {
                    "Usa una fecha válida con formato yyyy-MM-dd."
                }
            }
        }

        val formularioValidado = form.copy(
            descripcionError = descripcionError,
            montoError = montoError,
            fechaError = fechaError
        )

        if (
            descripcionError != null ||
            montoError != null ||
            fechaError != null
        ) {
            return ResultadoOperacion.Validacion(formularioValidado)
        }

        return try {
            val movimiento = MovimientoEntity(
                id = form.id ?: 0,
                descripcion = form.descripcion.trim(),
                monto = requireNotNull(monto),
                tipo = form.tipo,
                fecha = form.fecha.trim(),
                rutaComprobante = form.rutaComprobante
            )

            if (form.esEdicion) {
                repository.updateMovimiento(
                    movimiento = movimiento,
                    usuario = usuario
                )
            } else {
                repository.insertMovimiento(
                    movimiento = movimiento,
                    usuario = usuario
                )
            }

            _mensajeExitoMovimiento.value = if (form.esEdicion) {
                "Movimiento actualizado correctamente."
            } else {
                "Movimiento guardado correctamente."
            }

            ResultadoOperacion.Exito(formularioValidado)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoOperacion.Error(
                "No se pudo guardar el movimiento. " +
                        "Tus datos siguen en el formulario."
            )
        }
    }

    fun limpiarErroresMovimiento(form: MovimientoFormState): MovimientoFormState {
        return form.copy(
            descripcionError = null,
            montoError = null,
            fechaError = null
        )
    }

    /*
    fun agregarReserva(
        solicitante: String,
        fecha: String,
        horario: String
    ): String? {
        if (solicitante.isBlank()) return "El solicitante es obligatorio"
        if (fecha.isBlank()) return "La fecha es obligatoria"
        if (horario.isBlank()) return "El horario es obligatorio"

        viewModelScope.launch {
            repository.insertReserva(
                ReservaEntity(
                    solicitante = solicitante,
                    fecha = fecha,
                    horario = horario,
                    estado = EstadoReserva.PENDIENTE
                )
            )
        }

        return null
    }
    */

    fun confirmarReserva(
        reserva: ReservaEntity,
        usuario: UsuarioSesion
    ) {
        resolverSolicitud(
            reservaId = reserva.id,
            nuevoEstado = EstadoReserva.CONFIRMADA,
            usuario = usuario
        )
    }

    fun rechazarReserva(
        reserva: ReservaEntity,
        usuario: UsuarioSesion
    ) {
        resolverSolicitud(
            reservaId = reserva.id,
            nuevoEstado = EstadoReserva.RECHAZADA,
            usuario = usuario
        )
    }


    private fun resolverSolicitud(
        reservaId: Int,
        nuevoEstado: EstadoReserva,
        usuario: UsuarioSesion
    ) {
        viewModelScope.launch {
            try {
                repository.resolverSolicitudPendiente(
                    reservaId = reservaId,
                    nuevoEstado = nuevoEstado,
                    usuario = usuario
                )

                _mensajeExitoReserva.value =
                    if (nuevoEstado == EstadoReserva.CONFIRMADA) {
                        "Reserva confirmada correctamente"
                    } else {
                        "Reserva rechazada correctamente"
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeExitoReserva.value =
                    e.message ?: "No se pudo actualizar la solicitud."
            }
        }
    }

    // Deletes
    suspend fun eliminarMovimiento(
        movimiento: MovimientoEntity,
        usuario: UsuarioSesion
    ): ResultadoOperacion<Unit> {
        if (!usuario.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)) {
            return ResultadoOperacion.Error(
                "Tu cuenta no tiene permisos para eliminar movimientos."
            )
        }

        return try {
            repository.deleteMovimiento(
                movimiento = movimiento,
                usuario = usuario
            )

            _mensajeExitoMovimiento.value =
                "Movimiento eliminado correctamente."

            ResultadoOperacion.Exito(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoOperacion.Error(
                "No se pudo eliminar el movimiento. Actualiza la lista e inténtalo nuevamente."
            )
        }
    }

    fun crearMisReservasFactory(
        usuario: UsuarioSesion
    ): ViewModelProvider.Factory {
        return MisReservasViewModelFactory(
            repository = repository,
            usuario = usuario
        )
    }
}

class MainViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}