package com.example.reservationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reservationsystem.data.local.entity.ReservaEntity
import com.example.reservationsystem.data.repository.AppRepository
import com.example.reservationsystem.domain.auth.UsuarioSesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MisReservasUiState(
    val cargando: Boolean = true,
    val reservas: List<ReservaEntity> = emptyList(),
    val error: String? = null,
    val cancelandoId: Int? = null,
    val mensaje: String? = null
)

class MisReservasViewModel(
    private val repository: AppRepository,
    private val usuario: UsuarioSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisReservasUiState())
    val uiState = _uiState.asStateFlow()

    private var consultaJob: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        consultaJob?.cancel()

        consultaJob = viewModelScope.launch {
            _uiState.update {
                it.copy(cargando = true, error = null)
            }

            try {
                repository.getMisReservas(usuario).collect { reservas ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            reservas = reservas,
                            error = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar tus reservas."
                    )
                }
            }
        }
    }

    fun cancelar(reservaId: Int) {
        if (_uiState.value.cancelandoId != null) return

        _uiState.update {
            it.copy(
                cancelandoId = reservaId,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                repository.cancelarMiReserva(
                    usuario = usuario,
                    reservaId = reservaId
                )

                _uiState.update {
                    it.copy(mensaje = "Solicitud cancelada correctamente.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mensaje = e.message
                            ?: "No se pudo cancelar la solicitud."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(cancelandoId = null)
                }
            }
        }
    }

    fun mensajeMostrado() {
        _uiState.update {
            it.copy(mensaje = null)
        }
    }
}

class MisReservasViewModelFactory(
    private val repository: AppRepository,
    private val usuario: UsuarioSesion
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(
            modelClass.isAssignableFrom(MisReservasViewModel::class.java)
        )

        return MisReservasViewModel(
            repository = repository,
            usuario = usuario
        ) as T
    }
}