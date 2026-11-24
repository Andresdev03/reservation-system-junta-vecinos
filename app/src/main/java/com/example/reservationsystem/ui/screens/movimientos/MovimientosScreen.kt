package com.example.reservationsystem.ui.screens.movimientos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reservationsystem.domain.model.TipoMovimiento
import com.example.reservationsystem.viewmodel.MainViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.reservationsystem.data.local.entity.MovimientoEntity
import androidx.compose.ui.res.painterResource
import com.example.reservationsystem.R
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect


@Composable
fun MovimientosScreen(
    viewModel: MainViewModel,
    onAgregarMovimiento: () -> Unit,
    onEditarMovimiento: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filtroSeleccionado by viewModel.filtroSeleccionado.collectAsState()
    val hayMovimientos = state.movimientos.isNotEmpty()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensajeExitoMovimiento by viewModel.mensajeExitoMovimiento.collectAsStateWithLifecycle()

    var movimientoAEliminar by remember { mutableStateOf<MovimientoEntity?>(null) }

    LaunchedEffect(mensajeExitoMovimiento) {
        mensajeExitoMovimiento?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.mensajeExitoMovimientoMostrado()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarMovimiento) {
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
                visible = !hayMovimientos,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyMovimientosState(
                    onAgregarMovimiento = onAgregarMovimiento
                )
            }

            AnimatedVisibility(
                visible = hayMovimientos,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumenMovimientosCard(
                        ingresos = state.totalIngresos,
                        gastos = state.totalGastos,
                        saldo = state.saldo,
                        cantidadMovimientos = state.movimientos.size
                    )

                    Row {
                        FilterChip(
                            selected = filtroSeleccionado == FiltroMovimiento.TODOS,
                            onClick = { viewModel.cambiarFiltroMovimiento(FiltroMovimiento.TODOS) },
                            label = { Text("Todos") }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilterChip(
                            selected = filtroSeleccionado == FiltroMovimiento.INGRESOS,
                            onClick = { viewModel.cambiarFiltroMovimiento(FiltroMovimiento.INGRESOS) },
                            label = { Text("Ingresos") }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilterChip(
                            selected = filtroSeleccionado == FiltroMovimiento.GASTOS,
                            onClick = { viewModel.cambiarFiltroMovimiento(FiltroMovimiento.GASTOS) },
                            label = { Text("Gastos") }
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Movimientos registrados",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        items(state.movimientos) { movimiento ->
                            MovimientoItem(
                                movimiento = movimiento,
                                onEditarClick = {
                                    onEditarMovimiento(movimiento.id)
                                },
                                onEliminarClick = {
                                    movimientoAEliminar = movimiento
                                }
                            )
                        }
                    }
                }
            }

            movimientoAEliminar?.let { movimiento ->
                ConfirmarEliminarMovimientoDialog(
                    movimiento = movimiento,
                    onDismiss = {
                        movimientoAEliminar = null
                    },
                    onConfirm = {
                        viewModel.eliminarMovimiento(movimiento)
                        movimientoAEliminar = null
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyMovimientosState(
    onAgregarMovimiento: () -> Unit
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
                    text = "No hay movimientos registrados",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Agrega un ingreso o gasto para comenzar a llevar el control financiero.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(onClick = onAgregarMovimiento) {
                    Text("Agregar primer movimiento")
                }
            }
        }
    }
}

@Composable
private fun ResumenMovimientosCard(
    ingresos: Double,
    gastos: Double,
    saldo: Double,
    cantidadMovimientos: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Resumen financiero",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Ingresos: $$ingresos",
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Gastos: $$gastos",
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Saldo actual: $$saldo",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Movimientos registrados: $cantidadMovimientos"
            )
        }
    }
}

@Composable
private fun MovimientoItem(
    movimiento: MovimientoEntity,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
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
                    text = movimiento.descripcion,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Monto: $${movimiento.monto}"
                )

                Text(
                    text = "Fecha: ${movimiento.fecha}"
                )

                Text(
                    text = "Tipo: ${movimiento.tipo}",
                    color = if (movimiento.tipo == TipoMovimiento.INGRESO) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                movimiento.rutaComprobante?.let { ruta ->
                    AsyncImage(
                        model = ruta,
                        contentDescription = "Comprobante del movimiento",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column {
                IconButton(onClick = onEditarClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                        contentDescription = "Editar movimiento"
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmarEliminarMovimientoDialog(
    movimiento: MovimientoEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Eliminar movimiento")
        },
        text = {
            Text("¿Deseas eliminar el movimiento \"${movimiento.descripcion}\"?")
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