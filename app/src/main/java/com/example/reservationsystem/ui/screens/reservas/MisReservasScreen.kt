package com.example.reservationsystem.ui.screens.reservas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reservationsystem.domain.model.EstadoReserva
import com.example.reservationsystem.viewmodel.MisReservasViewModel

@Composable
fun MisReservasScreen(
    viewModel: MisReservasViewModel,
    onNuevaReserva: () -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var reservaACancelar by remember {
        mutableStateOf<Int?>(null)
    }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mis reservas",
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Volver")
                }

                Button(
                    onClick = onNuevaReserva,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Nueva")
                }
            }

            when {
                state.cargando -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                state.error != null -> {
                    Text(
                        text = state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(onClick = viewModel::cargar) {
                        Text("Reintentar")
                    }
                }

                state.reservas.isEmpty() -> {
                    Text("Todavía no tienes reservas.")
                    Text("Puedes crear una solicitud con el botón Nueva.")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = state.reservas,
                            key = { it.id }
                        ) { reserva ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = reserva.fecha,
                                        style =
                                            MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        "${reserva.horaInicio} a ${reserva.horaFin}"
                                    )

                                    Text(
                                        text = "Estado: ${
                                            when (reserva.estado) {
                                                EstadoReserva.PENDIENTE ->
                                                    "Pendiente"
                                                EstadoReserva.CONFIRMADA ->
                                                    "Confirmada"
                                                EstadoReserva.RECHAZADA ->
                                                    "Rechazada"
                                                EstadoReserva.CANCELADA ->
                                                    "Cancelada"
                                            }
                                        }"
                                    )

                                    if (
                                        reserva.estado ==
                                        EstadoReserva.PENDIENTE
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                reservaACancelar = reserva.id
                                            },
                                            enabled =
                                                state.cancelandoId == null,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                if (
                                                    state.cancelandoId ==
                                                    reserva.id
                                                ) {
                                                    "Cancelando…"
                                                } else {
                                                    "Cancelar solicitud"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    reservaACancelar?.let { reservaId ->
        AlertDialog(
            onDismissRequest = {
                reservaACancelar = null
            },
            title = {
                Text("Cancelar solicitud")
            },
            text = {
                Text(
                    "La solicitud quedará cancelada y el horario " +
                            "volverá a estar disponible."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reservaACancelar = null
                        viewModel.cancelar(reservaId)
                    }
                ) {
                    Text("Cancelar solicitud")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        reservaACancelar = null
                    }
                ) {
                    Text("Conservar")
                }
            }
        )
    }
}