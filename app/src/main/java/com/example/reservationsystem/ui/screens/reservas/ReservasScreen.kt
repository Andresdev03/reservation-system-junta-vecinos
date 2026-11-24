package com.example.reservationsystem.ui.screens.reservas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reservationsystem.R
import com.example.reservationsystem.data.local.entity.ReservaEntity
import com.example.reservationsystem.domain.model.EstadoReserva
import com.example.reservationsystem.viewmodel.MainViewModel

@Composable
fun ReservasScreen(
    viewModel: MainViewModel,
    onAgregarReserva: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensajeExitoReserva by viewModel.mensajeExitoReserva.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var reservaAEliminar by remember { mutableStateOf<ReservaEntity?>(null) }

    val hayReservas = state.reservas.isNotEmpty()

    LaunchedEffect(mensajeExitoReserva) {
        mensajeExitoReserva?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.mensajeExitoReservaMostrado()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarReserva) {
                Text("+")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            AnimatedVisibility(
                visible = !hayReservas,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyReservasState(onAgregarReserva = onAgregarReserva)
            }

            AnimatedVisibility(
                visible = hayReservas,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Reservas registradas",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    items(state.reservas) { reserva ->
                        ReservaItem(
                            reserva = reserva,
                            onConfirmar = { viewModel.confirmarReserva(reserva) },
                            onRechazar = { viewModel.rechazarReserva(reserva) },
                            onEliminar = {
                                reservaAEliminar = reserva
                            }
                        )
                    }
                }
            }

            reservaAEliminar?.let { reserva ->
                ConfirmarEliminarReservaDialog(
                    reserva = reserva,
                    onDismiss = {
                        reservaAEliminar = null
                    },
                    onConfirm = {
                        viewModel.eliminarReserva(reserva)
                        reservaAEliminar = null
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyReservasState(
    onAgregarReserva: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No hay reservas registradas",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Registra una nueva solicitud de arriendo para comenzar el control de disponibilidad.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(onClick = onAgregarReserva) {
                    Text("Agregar primera reserva")
                }
            }
        }
    }
}

@Composable
private fun ReservaItem(
    reserva: ReservaEntity,
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = reserva.solicitante,
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Fecha: ${reserva.fecha}")
                Text("Hora inicio: ${reserva.horaInicio}")
                Text("Hora fin: ${reserva.horaFin}")

                Text(
                    text = "Estado: ${reserva.estado}",
                    color = when (reserva.estado) {
                        EstadoReserva.PENDIENTE ->
                            MaterialTheme.colorScheme.tertiary

                        EstadoReserva.CONFIRMADA ->
                            MaterialTheme.colorScheme.primary

                        EstadoReserva.RECHAZADA ->
                            MaterialTheme.colorScheme.error

                        EstadoReserva.CANCELADA ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (reserva.estado == EstadoReserva.PENDIENTE) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onConfirmar,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Confirmar")
                        }

                        OutlinedButton(
                            onClick = onRechazar,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Rechazar")
                        }
                    }
                }
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    painter = painterResource(id = R.drawable.delete_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                    contentDescription = "Eliminar reserva"
                )
            }
        }
    }
}

@Composable
private fun ConfirmarEliminarReservaDialog(
    reserva: ReservaEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Eliminar reserva")
        },
        text = {
            Text("¿Deseas eliminar la reserva de ${reserva.solicitante}?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}