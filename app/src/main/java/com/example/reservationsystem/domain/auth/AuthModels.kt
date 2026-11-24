package com.example.reservationsystem.domain.auth

enum class Permiso(val descripcion: String) {
    VER_INICIO("Consultar el resumen comunitario"),
    SOLICITAR_RESERVA("Solicitar reservas"),
    GESTIONAR_RESERVAS("Administrar reservas"),
    VER_MOVIMIENTOS("Consultar movimientos"),
    GESTIONAR_MOVIMIENTOS("Crear, editar y eliminar movimientos")
}

enum class Rol(
    val etiqueta: String,
    val permisos: Set<Permiso>
) {
    VECINO(
        etiqueta = "Vecino/a",
        permisos = setOf(
            Permiso.VER_INICIO,
            Permiso.SOLICITAR_RESERVA
        )
    ),

    TESORERIA(
        etiqueta = "Tesorería",
        permisos = setOf(
            Permiso.VER_INICIO,
            Permiso.SOLICITAR_RESERVA,
            Permiso.VER_MOVIMIENTOS,
            Permiso.GESTIONAR_MOVIMIENTOS
        )
    ),

    DIRECTIVA(
        etiqueta = "Directiva / Secretaría",
        permisos = setOf(
            Permiso.VER_INICIO,
            Permiso.SOLICITAR_RESERVA,
            Permiso.GESTIONAR_RESERVAS,
            Permiso.VER_MOVIMIENTOS
        )
    )
}

data class UsuarioSesion(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: Rol
) {
    fun tienePermiso(permiso: Permiso): Boolean {
        return permiso in rol.permisos
    }
}