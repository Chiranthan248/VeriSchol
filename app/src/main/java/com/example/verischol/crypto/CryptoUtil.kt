package com.example.verischol.crypto

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    private val secretKey: SecretKey =
        SecretKeySpec("1234567890123456".toByteArray(), "AES")

    private val iv = IvParameterSpec("abcdefghijklmnop".toByteArray())

    fun encrypt(input: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        val encrypted = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val decoded = Base64.decode(encoded, Base64.NO_WRAP)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted)
    }
}
