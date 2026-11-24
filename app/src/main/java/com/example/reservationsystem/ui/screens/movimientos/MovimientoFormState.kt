package com.example.reservationsystem.ui.screens.movimientos

import com.example.reservationsystem.domain.model.TipoMovimiento

data class MovimientoFormState(
    val id: Int? = null,
    val descripcion: String = "",
    val monto: String = "",
    val fecha: String = "",
    val tipo: TipoMovimiento = TipoMovimiento.INGRESO,
    val rutaComprobante: String? = null,
    val descripcionError: String? = null,
    val montoError: String? = null,
    val fechaError: String? = null
) {
    val esEdicion: Boolean
        get() = id != null
}