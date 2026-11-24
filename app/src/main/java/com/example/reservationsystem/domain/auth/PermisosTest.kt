package com.example.reservationsystem.domain.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class PermisosTest {

    private fun usuario(rol: Rol) = UsuarioSesion(
        id = "usuario-prueba",
        nombre = "Usuario de prueba",
        correo = "prueba@example.com",
        rol = rol
    )

    @Test
    fun vecinoNoAccedeAlDetalleFinanciero() {
        val vecino = usuario(Rol.VECINO)

        assertFalse(
            vecino.tienePermiso(Permiso.VER_MOVIMIENTOS)
        )
        assertFalse(
            vecino.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)
        )
    }

    @Test
    fun tesoreriaConsultaYGestionaMovimientos() {
        val tesoreria = usuario(Rol.TESORERIA)

        assertTrue(
            tesoreria.tienePermiso(Permiso.VER_MOVIMIENTOS)
        )
        assertTrue(
            tesoreria.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)
        )
    }

    @Test
    fun directivaConsultaPeroNoModificaMovimientos() {
        val directiva = usuario(Rol.DIRECTIVA)

        assertTrue(
            directiva.tienePermiso(Permiso.VER_MOVIMIENTOS)
        )
        assertFalse(
            directiva.tienePermiso(Permiso.GESTIONAR_MOVIMIENTOS)
        )
    }

    @Test
    fun directivaConservaGestionDeReservas() {
        val directiva = usuario(Rol.DIRECTIVA)

        assertTrue(
            directiva.tienePermiso(Permiso.GESTIONAR_RESERVAS)
        )
    }
}