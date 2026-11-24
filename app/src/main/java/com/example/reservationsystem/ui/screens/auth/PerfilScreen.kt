package com.example.reservationsystem.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservationsystem.domain.auth.Permiso
import com.example.reservationsystem.domain.auth.UsuarioSesion

@Composable
fun PerfilScreen(
    usuario: UsuarioSesion,
    cargando: Boolean,
    mensaje: String?,
    onSolicitarReserva: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var confirmarSalida by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Mi perfil",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(usuario.nombre, style = MaterialTheme.typography.titleLarge)
        Text(usuario.correo)
        Text("Rol: ${usuario.rol.etiqueta}")

        Text(
            "Permisos",
            style = MaterialTheme.typography.titleMedium
        )

        usuario.rol.permisos.forEach { permiso ->
            Text("• ${permiso.descripcion}")
        }

        if (usuario.tienePermiso(Permiso.SOLICITAR_RESERVA)) {
            Button(
                onClick = onSolicitarReserva,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solicitar una reserva")
            }
        }

        mensaje?.let { Text(it) }

        OutlinedButton(
            onClick = { confirmarSalida = true },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        Text(
            "Modo demostración: las cuentas nuevas y la sesión " +
                    "no persisten al finalizar el proceso de la app.",
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSalida = false
                        onCerrarSesion()
                    }
                ) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}