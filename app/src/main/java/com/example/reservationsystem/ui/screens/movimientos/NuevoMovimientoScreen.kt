package com.example.reservationsystem.ui.screens.movimientos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.reservationsystem.domain.model.TipoMovimiento
import com.example.reservationsystem.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Row
import androidx.core.content.ContextCompat
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.ui.Alignment
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.material3.LinearProgressIndicator
import com.example.reservationsystem.domain.auth.UsuarioSesion
import com.example.reservationsystem.domain.model.ResultadoOperacion
import kotlinx.coroutines.CancellationException

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoMovimientoScreen(
    viewModel: MainViewModel,
    usuario: UsuarioSesion,
    onVolver: () -> Unit,
    movimientoId: Int? = null
) {
    val context = LocalContext.current

    var form by remember(usuario.id, movimientoId) {
        mutableStateOf(MovimientoFormState())
    }
    var uriFoto by remember { mutableStateOf<Uri?>(null) }
    var tipoExpandido by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            uriFoto?.let { uri ->
                form = form.copy(
                    rutaComprobante = uri.toString()
                )
            }
        }
    }

    var guardando by remember { mutableStateOf(false) }

    var cargando by remember(usuario.id, movimientoId) {
        mutableStateOf(movimientoId != null)
    }

    var errorCarga by remember { mutableStateOf<String?>(null) }
    var errorGuardado by remember { mutableStateOf<String?>(null) }
    var intentoCarga by remember { mutableStateOf(0) }

    val ocupado = cargando || guardando

    BackHandler(enabled = ocupado) { }

    fun crearUriComprobante(): Uri {
        val carpeta = File(context.filesDir, "comprobantes")

        if (!carpeta.exists()) {
            carpeta.mkdirs()
        }

        val nombreArchivo = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date()) + ".jpg"

        val archivo = File(carpeta, nombreArchivo)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = crearUriComprobante()
            uriFoto = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(movimientoId, usuario.id, intentoCarga) {
        val id = movimientoId

        if (id == null) {
            cargando = false
        } else {
            cargando = true
            errorCarga = null

            try {
                val existente = viewModel.obtenerFormularioMovimientoPorId(
                    id = id,
                    usuario = usuario
                )

                if (existente == null) {
                    errorCarga = "El movimiento ya no existe."
                } else {
                    form = existente
                    uriFoto = existente.rutaComprobante?.let {
                        Uri.parse(it)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorCarga = "No se pudo cargar el movimiento."
            } finally {
                cargando = false
            }
        }
    }

    Scaffold { padding ->
        /*
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {*/
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Mueve aquí los campos existentes, incluido el dropdown.
                // Deja aquí el botón de cámara, la vista previa y los errores.

                Text(
                    text = if (form.esEdicion) "Editar movimiento" else "Nuevo movimiento",
                    style = MaterialTheme.typography.headlineSmall
                )

                if (ocupado) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                errorCarga?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )

                    OutlinedButton(
                        onClick = { intentoCarga++ },
                        enabled = !ocupado
                    ) {
                        Text("Reintentar carga")
                    }
                }

                errorGuardado?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = form.descripcion,
                    onValueChange = {
                        form = form.copy(
                            descripcion = it,
                            descripcionError = null
                        )
                    },
                    label = { Text("Descripción") },
                    isError = form.descripcionError != null,
                    supportingText = {
                        form.descripcionError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ocupado && errorCarga == null
                )

                OutlinedTextField(
                    value = form.monto,
                    onValueChange = {
                        form = form.copy(
                            monto = it,
                            montoError = null
                        )
                    },
                    label = { Text("Monto") },
                    isError = form.montoError != null,
                    supportingText = {
                        form.montoError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ocupado && errorCarga == null,
                )

                OutlinedTextField(
                    value = form.fecha,
                    onValueChange = {
                        form = form.copy(
                            fecha = it,
                            fechaError = null
                        )
                    },
                    label = { Text("Fecha") },
                    placeholder = { Text("2026-10-26") },
                    isError = form.fechaError != null,
                    supportingText = {
                        form.fechaError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ocupado && errorCarga == null,
                )

                Text("Tipo de movimiento")

                ExposedDropdownMenuBox(
                    expanded = tipoExpandido,
                    onExpandedChange = {
                        if (!ocupado && errorCarga == null) {
                            tipoExpandido = it
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = when (form.tipo) {
                            TipoMovimiento.INGRESO -> "Ingreso"
                            TipoMovimiento.GASTO -> "Gasto"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipo de movimiento") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = tipoExpandido
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !ocupado && errorCarga == null,
                    )

                    ExposedDropdownMenu(
                        expanded = tipoExpandido,
                        onDismissRequest = {
                            tipoExpandido = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ingreso") },
                            onClick = {
                                form = form.copy(tipo = TipoMovimiento.INGRESO)
                                tipoExpandido = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Gasto") },
                            onClick = {
                                form = form.copy(tipo = TipoMovimiento.GASTO)
                                tipoExpandido = false
                            }
                        )
                    }
                }

                Text("Seleccionado: ${form.tipo}")

                OutlinedButton(
                    onClick = {
                        val permisoConcedido = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (permisoConcedido) {
                            val uri = crearUriComprobante()
                            uriFoto = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ocupado && errorCarga == null,
                ) {
                    Text("Tomar foto del comprobante")
                }

                uriFoto?.let { uri ->
                    val rutaComprobante = form.rutaComprobante

                    AnimatedVisibility(
                        visible = !rutaComprobante.isNullOrBlank(),
                        enter = fadeIn(animationSpec = tween(durationMillis = 2000)) +
                                expandVertically(
                                    animationSpec = tween(durationMillis = 2000),
                                    expandFrom = Alignment.Top
                                )
                    ) {
                        AsyncImage(
                            model = rutaComprobante?.let { Uri.parse(it) },
                            contentDescription = "Comprobante del movimiento",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(120.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            /*
            Button(
                onClick = {
                    val resultado = viewModel.agregarMovimiento(form)

                    val tieneErrores = resultado.descripcionError != null ||
                            resultado.montoError != null ||
                            resultado.fechaError != null

                    if (tieneErrores) {
                        form = resultado
                    } else {
                        onVolver()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (form.esEdicion) "Actualizar" else "Guardar")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }

             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.weight(1f),
                    enabled = !ocupado,
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (!ocupado && errorCarga == null) {
                            guardando = true
                            errorGuardado = null
                            tipoExpandido = false

                            val formularioAEnviar = form

                            scope.launch {
                                var salir = false

                                try {
                                    when (
                                        val resultado = viewModel.guardarMovimiento(
                                            form = formularioAEnviar,
                                            usuario = usuario
                                        )
                                    ) {
                                        is ResultadoOperacion.Exito -> {
                                            salir = true
                                        }

                                        is ResultadoOperacion.Validacion -> {
                                            form = resultado.formulario
                                        }

                                        is ResultadoOperacion.Error -> {
                                            errorGuardado = resultado.mensaje
                                        }
                                    }
                                } finally {
                                    guardando = false
                                }

                                if (salir) {
                                    onVolver()
                                }
                            }
                        }
                    },
                    enabled = !ocupado && errorCarga == null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when {
                            guardando -> "Guardando…"
                            form.esEdicion -> "Actualizar"
                            else -> "Guardar"
                        }
                    )
                }
            }
        }
    }
}