package com.example.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    const val PREFIX = "QRSEC:v1:"
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128

    fun isEncryptedPayload(text: String): Boolean {
        return text.trim().startsWith(PREFIX)
    }

    /**
     * Encrypts plaintext with AES-256-GCM using a key derived from the passphrase via PBKDF2.
     * Returns "QRSEC:v1:<salt_base64>:<iv_base64>:<ciphertext_base64>"
     */
    fun encrypt(plainText: String, passphrase: CharArray): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKey = factory.generateSecret(spec)
        val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)

        return "$PREFIX$saltB64:$ivB64:$cipherB64"
    }

    /**
     * Decrypts encrypted payload using the provided passphrase.
     * Returns Result with decrypted string or error.
     */
    fun decrypt(encryptedPayload: String, passphrase: CharArray): Result<String> {
        return runCatching {
            val trimmed = encryptedPayload.trim()
            if (!trimmed.startsWith(PREFIX)) {
                throw IllegalArgumentException("Not a valid encrypted QR code")
            }
            val parts = trimmed.substring(PREFIX.length).split(":")
            if (parts.size != 3) {
                throw IllegalArgumentException("Malformed encrypted QR payload")
            }

            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipherBytes = Base64.decode(parts[2], Base64.NO_WRAP)

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(passphrase, salt, ITERATION_COUNT, KEY_LENGTH)
            val secretKey = factory.generateSecret(spec)
            val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        }
    }
}
