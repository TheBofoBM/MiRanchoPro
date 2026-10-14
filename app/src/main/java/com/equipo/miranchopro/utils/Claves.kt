package com.equipo.miranchopro.utils

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Claves {
    fun esHash(valor: String) = valor.startsWith("pbkdf2-sha256:") || valor.startsWith("pbkdf2-sha1:")
    fun proteger(clave: String): String {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        // SHA-256 está disponible desde Android 26. El respaldo permite Android 24/25.
        val sha256 = runCatching { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256") }.isSuccess
        val algoritmo = if (sha256) "pbkdf2-sha256" else "pbkdf2-sha1"
        val rondas = if (sha256) 600000 else 1400000
        return "$algoritmo:$rondas:${hex(sal)}:${hex(derivar(clave, sal, rondas, algoritmo))}"
    }
    fun verificar(clave: String, guardada: String): Boolean {
        if (!esHash(guardada)) return MessageDigest.isEqual(clave.toByteArray(Charsets.UTF_8), guardada.toByteArray(Charsets.UTF_8))
        return runCatching {
            val partes = guardada.split(':')
            val rondas = partes[1].toInt()
            require(partes.size == 4 && rondas in 10000..2000000 && partes[2].length == 32 && partes[3].length == 64)
            MessageDigest.isEqual(derivar(clave, bytes(partes[2]), rondas, partes[0]), bytes(partes[3]))
        }.getOrDefault(false)
    }
    private fun derivar(clave: String, sal: ByteArray, rondas: Int, algoritmo: String): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), sal, rondas, 256)
        val nombre = if (algoritmo == "pbkdf2-sha256") "PBKDF2WithHmacSHA256" else "PBKDF2WithHmacSHA1"
        return try { SecretKeyFactory.getInstance(nombre).generateSecret(spec).encoded } finally { spec.clearPassword() }
    }
    private fun hex(bytes: ByteArray) = bytes.joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }
    private fun bytes(hex: String) = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
