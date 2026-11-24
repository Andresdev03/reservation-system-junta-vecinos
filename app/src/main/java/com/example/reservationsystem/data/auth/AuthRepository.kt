package com.example.reservationsystem.data.auth

import com.example.reservationsystem.domain.auth.UsuarioSesion

interface AuthRepository {

    suspend fun iniciarSesion(
        correo: String,
        clave: String
    ): UsuarioSesion

    suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String
    ): UsuarioSesion

    suspend fun solicitarRecuperacion(correo: String): String

    suspend fun cerrarSesion()
}