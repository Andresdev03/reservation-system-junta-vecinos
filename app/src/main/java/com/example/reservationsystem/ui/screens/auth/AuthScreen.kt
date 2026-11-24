package com.example.reservationsystem.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.reservationsystem.viewmodel.AuthUiState

private enum class ModoAuth {
    LOGIN, REGISTRO, RECUPERACION
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegistro: (String, String, String, String) -> Unit,
    onRecuperar: (String) -> Unit,
    onLimpiar: () -> Unit
) {
    var modo by remember { mutableStateOf(ModoAuth.LOGIN) }

    // Las contraseñas no se guardan con rememberSaveable.
    var nombre by remember(modo) { mutableStateOf("") }
    var correo by remember(modo) { mutableStateOf("") }
    var clave by remember(modo) { mutableStateOf("") }
    var confirmacion by remember(modo) { mutableStateOf("") }

    fun cambiarModo(nuevoModo: ModoAuth) {
        if (!state.cargando) {
            onLimpiar()
            modo = nuevoModo
        }
    }

    BackHandler(enabled = modo != ModoAuth.LOGIN || state.cargando) {
        if (!state.cargando) cambiarModo(ModoAuth.LOGIN)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Gestión vecinal",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = when (modo) {
                    ModoAuth.LOGIN -> "Iniciar sesión"
                    ModoAuth.REGISTRO -> "Crear cuenta"
                    ModoAuth.RECUPERACION -> "Recuperar contraseña"
                },
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Versión de demostración sin conexión al servidor.",
                style = MaterialTheme.typography.bodySmall
            )

            if (modo == ModoAuth.REGISTRO) {
                Text("Las cuentas nuevas se registran como Vecino/a.")

                CampoAuth(
                    valor = nombre,
                    onCambio = { nombre = it },
                    etiqueta = "Nombre",
                    error = state.errores["nombre"],
                    habilitado = !state.cargando
                )
            }

            CampoAuth(
                valor = correo,
                onCambio = { correo = it },
                etiqueta = "Correo electrónico",
                error = state.errores["correo"],
                habilitado = !state.cargando,
                tipoTeclado = KeyboardType.Email
            )

            if (modo != ModoAuth.RECUPERACION) {
                CampoAuth(
                    valor = clave,
                    onCambio = { clave = it },
                    etiqueta = "Contraseña",
                    error = state.errores["clave"],
                    habilitado = !state.cargando,
                    esClave = true
                )
            }

            if (modo == ModoAuth.REGISTRO) {
                CampoAuth(
                    valor = confirmacion,
                    onCambio = { confirmacion = it },
                    etiqueta = "Confirmar contraseña",
                    error = state.errores["confirmacion"],
                    habilitado = !state.cargando,
                    esClave = true
                )
            }

            state.mensaje?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.cargando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    when (modo) {
                        ModoAuth.LOGIN -> onLogin(correo, clave)
                        ModoAuth.REGISTRO -> onRegistro(
                            nombre,
                            correo,
                            clave,
                            confirmacion
                        )
                        ModoAuth.RECUPERACION -> onRecuperar(correo)
                    }
                },
                enabled = !state.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (modo) {
                        ModoAuth.LOGIN -> "Ingresar"
                        ModoAuth.REGISTRO -> "Crear cuenta"
                        ModoAuth.RECUPERACION -> "Solicitar recuperación"
                    }
                )
            }

            if (modo == ModoAuth.LOGIN) {
                OutlinedButton(
                    onClick = { cambiarModo(ModoAuth.REGISTRO) },
                    enabled = !state.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear una cuenta")
                }

                TextButton(
                    onClick = { cambiarModo(ModoAuth.RECUPERACION) },
                    enabled = !state.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Olvidé mi contraseña")
                }
            } else {
                OutlinedButton(
                    onClick = { cambiarModo(ModoAuth.LOGIN) },
                    enabled = !state.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al inicio de sesión")
                }
            }
        }
    }
}

@Composable
private fun CampoAuth(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    habilitado: Boolean,
    esClave: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    var mostrarClave by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        enabled = habilitado,
        singleLine = true,
        isError = error != null,
        supportingText = {
            error?.let { Text(it) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esClave) KeyboardType.Password else tipoTeclado
        ),
        visualTransformation = if (esClave && !mostrarClave) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (esClave) {
            {
                TextButton(
                    onClick = { mostrarClave = !mostrarClave },
                    enabled = habilitado
                ) {
                    Text(if (mostrarClave) "Ocultar" else "Ver")
                }
            }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    )
}