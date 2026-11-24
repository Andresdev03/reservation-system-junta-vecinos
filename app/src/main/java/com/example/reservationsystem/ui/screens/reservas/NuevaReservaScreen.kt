package com.example.reservationsystem.ui.screens.reservas
/*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservationsystem.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaReservaScreen(
    viewModel: MainViewModel,
    onVolver: () -> Unit
) {
    var form by remember { mutableStateOf(ReservaFormState()) }
    val scope = rememberCoroutineScope()

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePickerInicio by remember { mutableStateOf(false) }
    var mostrarTimePickerFin by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerInicioState = rememberTimePickerState(is24Hour = true)
    val timePickerFinState = rememberTimePickerState(is24Hour = true)

    val formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nueva reserva",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = form.solicitante,
                onValueChange = {
                    form = form.copy(
                        solicitante = it,
                        solicitanteError = null,
                        duplicidadError = null
                    )
                },
                label = { Text("Solicitante") },
                isError = form.solicitanteError != null,
                supportingText = {
                    form.solicitanteError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.fecha,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha") },
                placeholder = { Text("Selecciona una fecha") },
                isError = form.fechaError != null,
                supportingText = {
                    form.fechaError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { mostrarDatePicker = true }) {
                        Text("Elegir")
                    }
                }
            )

            OutlinedTextField(
                value = form.horaInicio,
                onValueChange = { },
                readOnly = true,
                label = { Text("Hora de inicio") },
                placeholder = { Text("Selecciona hora") },
                isError = form.horaInicioError != null,
                supportingText = {
                    form.horaInicioError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { mostrarTimePickerInicio = true }) {
                        Text("Elegir")
                    }
                }
            )

            OutlinedTextField(
                value = form.horaFin,
                onValueChange = { },
                readOnly = true,
                label = { Text("Hora de fin") },
                placeholder = { Text("Selecciona hora") },
                isError = form.horaFinError != null || form.duplicidadError != null,
                supportingText = {
                    when {
                        form.horaFinError != null -> Text(form.horaFinError!!)
                        form.duplicidadError != null -> Text(form.duplicidadError!!)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { mostrarTimePickerFin = true }) {
                        Text("Elegir")
                    }
                }
            )

            Button(
                onClick = {
                    scope.launch {
                        val resultado = viewModel.guardarReserva(form)

                        val tieneErrores = resultado.solicitanteError != null ||
                                resultado.fechaError != null ||
                                resultado.horaInicioError != null ||
                                resultado.horaFinError != null ||
                                resultado.duplicidadError != null

                        if (tieneErrores) {
                            form = resultado
                        } else {
                            onVolver()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar reserva")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val fechaSeleccionada = datePickerState.selectedDateMillis

                        if (fechaSeleccionada != null) {
                            val fecha = Instant
                                .ofEpochMilli(fechaSeleccionada)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .format(formatoFecha)

                            form = form.copy(
                                fecha = fecha,
                                fechaError = null,
                                duplicidadError = null
                            )
                        }

                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarTimePickerInicio) {
        TimePickerDialogSimple(
            titulo = "Hora de inicio",
            onDismiss = { mostrarTimePickerInicio = false },
            onConfirm = {
                val hora = LocalTime.of(
                    timePickerInicioState.hour,
                    timePickerInicioState.minute
                ).format(formatoHora)

                form = form.copy(
                    horaInicio = hora,
                    horaInicioError = null,
                    duplicidadError = null
                )

                mostrarTimePickerInicio = false
            },
            content = {
                TimePicker(state = timePickerInicioState)
            }
        )
    }

    if (mostrarTimePickerFin) {
        TimePickerDialogSimple(
            titulo = "Hora de fin",
            onDismiss = { mostrarTimePickerFin = false },
            onConfirm = {
                val hora = LocalTime.of(
                    timePickerFinState.hour,
                    timePickerFinState.minute
                ).format(formatoHora)

                form = form.copy(
                    horaFin = hora,
                    horaFinError = null,
                    duplicidadError = null
                )

                mostrarTimePickerFin = false
            },
            content = {
                TimePicker(state = timePickerFinState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogSimple(
    titulo: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(titulo)
        },
        text = {
            content()
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
*/


import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservationsystem.domain.auth.UsuarioSesion
import com.example.reservationsystem.viewmodel.MainViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaReservaScreen(
    viewModel: MainViewModel,
    usuario: UsuarioSesion,
    onVolver: () -> Unit
) {
    var form by remember(usuario.id) {
        mutableStateOf(
            ReservaFormState().copy(
                solicitante = usuario.nombre
            )
        )
    }

    var guardando by remember { mutableStateOf(false) }
    var errorGuardado by remember { mutableStateOf<String?>(null) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePickerInicio by remember { mutableStateOf(false) }
    var mostrarTimePickerFin by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val datePickerState = rememberDatePickerState()
    val timePickerInicioState = rememberTimePickerState(
        is24Hour = true
    )
    val timePickerFinState = rememberTimePickerState(
        is24Hour = true
    )

    val formatoFecha = remember {
        DateTimeFormatter.ISO_LOCAL_DATE
    }

    val formatoHora = remember {
        DateTimeFormatter.ofPattern("HH:mm")
    }

    // Evita abandonar el formulario durante una escritura en curso.
    BackHandler(enabled = guardando) { }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nueva reserva",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "La solicitud se realizará con los datos de tu perfil.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = usuario.nombre,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Solicitante") },
                    supportingText = {
                        Text("Este dato pertenece a tu cuenta.")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                CampoSeleccionReserva(
                    valor = form.fecha,
                    etiqueta = "Fecha",
                    placeholder = "Selecciona una fecha",
                    error = form.fechaError,
                    habilitado = !guardando,
                    onElegir = {
                        mostrarDatePicker = true
                    }
                )

                CampoSeleccionReserva(
                    valor = form.horaInicio,
                    etiqueta = "Hora de inicio",
                    placeholder = "Selecciona una hora",
                    error = form.horaInicioError,
                    habilitado = !guardando,
                    onElegir = {
                        mostrarTimePickerInicio = true
                    }
                )

                CampoSeleccionReserva(
                    valor = form.horaFin,
                    etiqueta = "Hora de fin",
                    placeholder = "Selecciona una hora",
                    error = form.horaFinError ?: form.duplicidadError,
                    habilitado = !guardando,
                    onElegir = {
                        mostrarTimePickerFin = true
                    }
                )

                form.solicitanteError?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                errorGuardado?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (guardando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onVolver,
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (!guardando) {
                            guardando = true
                            errorGuardado = null

                            // El nombre no depende de un campo editable.
                            val formularioAEnviar = form.copy(
                                solicitante = usuario.nombre,
                                solicitanteError = null
                            )

                            scope.launch {
                                var completado = false

                                try {
                                    val resultado = viewModel.guardarReserva(
                                        formularioAEnviar
                                    )

                                    form = resultado

                                    val tieneErrores =
                                        resultado.solicitanteError != null ||
                                                resultado.fechaError != null ||
                                                resultado.horaInicioError != null ||
                                                resultado.horaFinError != null ||
                                                resultado.duplicidadError != null

                                    completado = !tieneErrores
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    errorGuardado =
                                        "No se pudo completar el guardado. " +
                                                "Tus datos siguen en el formulario."
                                } finally {
                                    guardando = false
                                }

                                if (completado) {
                                    onVolver()
                                }
                            }
                        }
                    },
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (guardando) "Guardando…" else "Guardar"
                    )
                }
            }
        }
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                mostrarDatePicker = false
            },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val fecha = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .format(formatoFecha)

                            form = form.copy(
                                fecha = fecha,
                                fechaError = null,
                                duplicidadError = null
                            )

                            errorGuardado = null
                        }

                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarTimePickerInicio) {
        TimePickerDialogSimple(
            titulo = "Hora de inicio",
            onDismiss = {
                mostrarTimePickerInicio = false
            },
            onConfirm = {
                val hora = LocalTime.of(
                    timePickerInicioState.hour,
                    timePickerInicioState.minute
                ).format(formatoHora)

                form = form.copy(
                    horaInicio = hora,
                    horaInicioError = null,
                    duplicidadError = null
                )

                errorGuardado = null
                mostrarTimePickerInicio = false
            }
        ) {
            TimePicker(state = timePickerInicioState)
        }
    }

    if (mostrarTimePickerFin) {
        TimePickerDialogSimple(
            titulo = "Hora de fin",
            onDismiss = {
                mostrarTimePickerFin = false
            },
            onConfirm = {
                val hora = LocalTime.of(
                    timePickerFinState.hour,
                    timePickerFinState.minute
                ).format(formatoHora)

                form = form.copy(
                    horaFin = hora,
                    horaFinError = null,
                    duplicidadError = null
                )

                errorGuardado = null
                mostrarTimePickerFin = false
            }
        ) {
            TimePicker(state = timePickerFinState)
        }
    }
}

@Composable
private fun CampoSeleccionReserva(
    valor: String,
    etiqueta: String,
    placeholder: String,
    error: String?,
    habilitado: Boolean,
    onElegir: () -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        readOnly = true,
        enabled = habilitado,
        label = { Text(etiqueta) },
        placeholder = { Text(placeholder) },
        isError = error != null,
        supportingText = {
            error?.let { Text(it) }
        },
        trailingIcon = {
            TextButton(
                onClick = onElegir,
                enabled = habilitado
            ) {
                Text("Elegir")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TimePickerDialogSimple(
    titulo: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(titulo)
        },
        text = {
            content()
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}