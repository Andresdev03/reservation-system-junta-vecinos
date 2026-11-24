package com.example.reservationsystem.ui.navigation



sealed class AppScreen(val route: String) {
    data object Inicio : AppScreen("inicio")
    data object Movimientos : AppScreen("movimientos")
    data object Reservas : AppScreen("reservas")
    data object NuevoMovimiento : AppScreen("nuevo_movimiento")
    data object EditarMovimiento : AppScreen("editar_movimiento/{movimientoId}")
    data object NuevaReserva : AppScreen("nueva_reserva")
}