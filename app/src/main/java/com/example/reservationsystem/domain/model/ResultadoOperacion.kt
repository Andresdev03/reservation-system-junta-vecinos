package com.example.reservationsystem.domain.model

sealed class ResultadoOperacion<out T> {

    data class Exito<T>(
        val valor: T
    ) : ResultadoOperacion<T>()

    data class Validacion<T>(
        val formulario: T
    ) : ResultadoOperacion<T>()

    data class Error(
        val mensaje: String
    ) : ResultadoOperacion<Nothing>()
}