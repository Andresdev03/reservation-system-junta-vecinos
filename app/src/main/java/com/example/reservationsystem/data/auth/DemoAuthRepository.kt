package com.example.reservationsystem.data.auth

import com.example.reservationsystem.domain.auth.Rol
import com.example.reservationsystem.domain.auth.UsuarioSesion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class DemoAuthRepository : AuthRepository {

    private data class Cuenta(
        val usuario: UsuarioSesion,
        val sal: ByteArray,
        val verificador: ByteArray
    )

    private val cuentas = mutableMapOf<String, Cuenta>()

    private fun normalizar(correo: String): String {
        return correo.trim().lowercase(Locale.ROOT)
    }

    private fun generarVerificador(
        clave: String,
        sal: ByteArray
    ): ByteArray {
        val caracteres = clave.toCharArray()
        val especificacion = PBEKeySpec(
            caracteres,
            sal,
            120_000,
            256
        )

        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacion)
                .encoded
        } finally {
            especificacion.clearPassword()
            caracteres.fill('\u0000')
        }
    }

    private fun nuevaCuenta(
        nombre: String,
        correo: String,
        clave: String,
        rol: Rol
    ): Cuenta {
        val sal = ByteArray(16).also {
            SecureRandom().nextBytes(it)
        }

        return Cuenta(
            usuario = UsuarioSesion(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                correo = normalizar(correo),
                rol = rol
            ),
            sal = sal,
            verificador = generarVerificador(clave, sal)
        )
    }

    override suspend fun iniciarSesion(
        correo: String,
        clave: String
    ): UsuarioSesion = withContext(Dispatchers.Default) {
        val correoNormalizado = normalizar(correo)

        // Cuentas ficticias exclusivas de esta implementación de demostración.
        val perfilDemo = when (correoNormalizado) {
            "vecino@demo.cl" -> "Vecino de prueba" to Rol.VECINO
            "tesoreria@demo.cl" -> "Tesorería de prueba" to Rol.TESORERIA
            "directiva@demo.cl" -> "Directiva de prueba" to Rol.DIRECTIVA
            else -> null
        }

        val cuenta = cuentas[correoNormalizado] ?: if (perfilDemo != null) {
            nuevaCuenta(
                nombre = perfilDemo.first,
                correo = correoNormalizado,
                clave = "Demo12345!",
                rol = perfilDemo.second
            ).also {
                cuentas[correoNormalizado] = it
            }
        } else {
            throw IllegalArgumentException("Correo o contraseña incorrectos.")
        }

        val coincide = MessageDigest.isEqual(
            cuenta.verificador,
            generarVerificador(clave, cuenta.sal)
        )

        require(coincide) {
            "Correo o contraseña incorrectos."
        }

        cuenta.usuario
    }

    override suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String
    ): UsuarioSesion = withContext(Dispatchers.Default) {
        val correoNormalizado = normalizar(correo)

        val reservado = correoNormalizado in setOf(
            "vecino@demo.cl",
            "tesoreria@demo.cl",
            "directiva@demo.cl"
        )

        require(!reservado && correoNormalizado !in cuentas) {
            "No se pudo registrar este correo. Prueba iniciar sesión."
        }

        val cuenta = nuevaCuenta(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            clave = clave,
            rol = Rol.VECINO
        )

        cuentas[correoNormalizado] = cuenta
        cuenta.usuario
    }

    override suspend fun solicitarRecuperacion(
        correo: String
    ): String {
        return "Modo demostración: el formulario funciona, " +
                "pero todavía no se envían correos ni se cambia la contraseña. " +
                "Conectaremos este proceso al microservicio."
    }

    override suspend fun cerrarSesion() = Unit
}