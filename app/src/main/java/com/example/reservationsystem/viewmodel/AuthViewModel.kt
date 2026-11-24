package com.example.reservationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reservationsystem.data.auth.AuthRepository
import com.example.reservationsystem.data.auth.DemoAuthRepository
import com.example.reservationsystem.domain.auth.UsuarioSesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val usuario: UsuarioSesion? = null,
    val cargando: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val mensaje: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private fun validarCorreo(correo: String): String? {
        val formato = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

        return when {
            correo.isBlank() -> "Ingresa tu correo."
            !formato.matches(correo.trim()) -> "Ingresa un correo válido."
            else -> null
        }
    }

    private fun validarClave(clave: String): String? {
        return when {
            clave.isBlank() -> "Ingresa tu contraseña."
            clave.length < 8 -> "Usa al menos 8 caracteres."
            else -> null
        }
    }

    fun limpiarMensajes() {
        if (_uiState.value.cargando) return

        _uiState.value = _uiState.value.copy(
            errores = emptyMap(),
            mensaje = null
        )
    }

    private fun ejecutar(
        operacion: suspend () -> Unit
    ) {
        if (_uiState.value.cargando) return

        _uiState.value = _uiState.value.copy(
            cargando = true,
            errores = emptyMap(),
            mensaje = null
        )

        viewModelScope.launch {
            try {
                operacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    mensaje = e.message ?: "No se pudo completar la operación."
                )
            } finally {
                _uiState.value = _uiState.value.copy(cargando = false)
            }
        }
    }

    fun iniciarSesion(correo: String, clave: String) {
        if (_uiState.value.cargando) return

        val errores = mutableMapOf<String, String>()

        validarCorreo(correo)?.let { errores["correo"] = it }
        if (clave.isBlank()) {
            errores["clave"] = "Ingresa tu contraseña."
        }

        if (errores.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                errores = errores,
                mensaje = null
            )
            return
        }

        ejecutar {
            val usuario = repository.iniciarSesion(correo.trim(), clave)
            _uiState.value = _uiState.value.copy(usuario = usuario)
        }
    }

    fun registrar(
        nombre: String,
        correo: String,
        clave: String,
        confirmacion: String
    ) {
        if (_uiState.value.cargando) return

        val errores = mutableMapOf<String, String>()

        if (nombre.trim().length < 3) {
            errores["nombre"] = "Ingresa un nombre de al menos 3 caracteres."
        }

        validarCorreo(correo)?.let { errores["correo"] = it }
        validarClave(clave)?.let { errores["clave"] = it }

        if (confirmacion != clave || confirmacion.isBlank()) {
            errores["confirmacion"] = "Las contraseñas deben coincidir."
        }

        if (errores.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                errores = errores,
                mensaje = null
            )
            return
        }

        ejecutar {
            val usuario = repository.registrar(
                nombre.trim(),
                correo.trim(),
                clave
            )
            _uiState.value = _uiState.value.copy(usuario = usuario)
        }
    }

    fun recuperar(correo: String) {
        if (_uiState.value.cargando) return

        val error = validarCorreo(correo)

        if (error != null) {
            _uiState.value = _uiState.value.copy(
                errores = mapOf("correo" to error),
                mensaje = null
            )
            return
        }

        ejecutar {
            val mensaje = repository.solicitarRecuperacion(correo.trim())
            _uiState.value = _uiState.value.copy(mensaje = mensaje)
        }
    }

    fun cerrarSesion() {
        ejecutar {
            repository.cerrarSesion()
            _uiState.value = AuthUiState()
        }
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository = DemoAuthRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java))
        return AuthViewModel(repository) as T
    }
}