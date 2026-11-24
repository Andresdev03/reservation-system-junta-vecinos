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
import com.example.reservationsystem.domain.model.TipoMovimiento
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
data class DashboardUiState(
    val movimientos: List<MovimientoEntity> = emptyList(),
    val reservas: List<ReservaEntity> = emptyList(),
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val mensajeExito: String? = null
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
            totalGastos = gastos
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

    fun eliminarReserva(reserva: ReservaEntity) {
        viewModelScope.launch {
            repository.deleteReserva(reserva)
            _mensajeExitoReserva.value = "Reserva eliminada correctamente"
        }
    }

    suspend fun obtenerFormularioMovimientoPorId(id: Int): MovimientoFormState? {
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

    fun agregarMovimiento(form: MovimientoFormState): MovimientoFormState {
        val descripcionError =
            if (form.descripcion.isBlank()) "La descripción es obligatoria" else null

        val montoError = when {
            form.monto.isBlank() -> "El monto es obligatorio"
            form.monto.toDoubleOrNull() == null -> "Ingresa un monto válido"
            form.monto.toDoubleOrNull()!! <= 0.0 -> "El monto debe ser mayor que 0"
            else -> null
        }

        val fechaError =
            if (form.fecha.isBlank()) "La fecha es obligatoria" else null

        val formularioConErrores = form.copy(
            descripcionError = descripcionError,
            montoError = montoError,
            fechaError = fechaError
        )

        val formularioValido = descripcionError == null &&
                montoError == null &&
                fechaError == null

        if (!formularioValido) return formularioConErrores

        viewModelScope.launch {
            val movimiento = MovimientoEntity(
                id = form.id ?: 0,
                descripcion = form.descripcion.trim(),
                monto = form.monto.toDouble(),
                tipo = form.tipo,
                fecha = form.fecha.trim(),
                rutaComprobante = form.rutaComprobante
            )

            if (form.esEdicion) {
                repository.updateMovimiento(movimiento)
                _mensajeExitoMovimiento.value = "Movimiento actualizado correctamente"
            } else {
                repository.insertMovimiento(movimiento)
                _mensajeExitoMovimiento.value = "Movimiento guardado correctamente"
            }
        }

        return MovimientoFormState(tipo = form.tipo)
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

    fun confirmarReserva(reserva: ReservaEntity) {
        resolverSolicitud(
            reservaId = reserva.id,
            nuevoEstado = EstadoReserva.CONFIRMADA
        )
    }

    fun rechazarReserva(reserva: ReservaEntity) {
        resolverSolicitud(
            reservaId = reserva.id,
            nuevoEstado = EstadoReserva.RECHAZADA
        )
    }


    private fun resolverSolicitud(
        reservaId: Int,
        nuevoEstado: EstadoReserva
    ) {
        viewModelScope.launch {
            try {
                repository.resolverSolicitudPendiente(
                    reservaId = reservaId,
                    nuevoEstado = nuevoEstado
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
    fun eliminarMovimiento(movimiento: MovimientoEntity) {
        viewModelScope.launch {
            repository.deleteMovimiento(movimiento)
            _mensajeExitoMovimiento.value = "Movimiento eliminado correctamente"
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