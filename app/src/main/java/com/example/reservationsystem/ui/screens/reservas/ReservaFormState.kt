package com.example.reservationsystem.ui.screens.reservas


data class ReservaFormState(
    val solicitante: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val solicitanteError: String? = null,
    val fechaError: String? = null,
    val horaInicioError: String? = null,
    val horaFinError: String? = null,
    val duplicidadError: String? = null
)