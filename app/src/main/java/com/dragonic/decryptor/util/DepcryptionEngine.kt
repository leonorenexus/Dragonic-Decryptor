package com.dragonic.decryptor.util

import android.util.Base64
import com.dragonic.decryptor.domain.model.DecryptAlgorithm
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

object DecryptionEngine {

    data class DecryptResult(
        val success: Boolean,
        val output: String,
        val error: String = "",
        val timeTakenMs: Long = 0
    )

    fun decrypt(
        algorithm: DecryptAlgorithm,
        input: String,
        key: String = "",
        iv: String = ""
    ): DecryptResult {
        val start = System.currentTimeMillis()
        return try {
            val output = when (algorithm) {
                DecryptAlgorithm.AUTO -> autoDecrypt(input, key)
                DecryptAlgorithm.AES_128, DecryptAlgorithm.AES_192, DecryptAlgorithm.AES_256 ->
                    aesDecrypt(input, key, iv, algorithm)
                DecryptAlgorithm.DES -> desDecrypt(input, key, iv)
                DecryptAlgorithm.TRIPLE_DES -> tripleDesDecrypt(input, key, iv)
                DecryptAlgorithm.RC4 -> rc4Decrypt(input, key)
                DecryptAlgorithm.XOR -> xorDecrypt(input, key)
                DecryptAlgorithm.BASE64 -> base64Decode(input)
                DecryptAlgorithm.BASE32 -> base32Decode(input)
                DecryptAlgorithm.BASE16 -> base16Decode(input)
                DecryptAlgorithm.HEX -> hexDecode(input)
                DecryptAlgorithm.BINARY -> binaryDecode(input)
                DecryptAlgorithm.OCTAL -> octalDecode(input)
                DecryptAlgorithm.ROT13 -> rot13(input)
                DecryptAlgorithm.URL -> urlDecode(input)
                DecryptAlgorithm.HTML -> htmlDecode(input)
                DecryptAlgorithm.UNICODE -> unicodeDecode(input)
                DecryptAlgorithm.JWT -> jwtDecode(input)
                DecryptAlgorithm.GZIP -> gzipDecompress(input)
                DecryptAlgorithm.ZLIB -> zlibDecompress(input)
                DecryptAlgorithm.BLOWFISH -> blowfishDecrypt(input, key, iv)
                DecryptAlgorithm.CHACHA20 -> "ChaCha20 requires native library (not available offline)"
            }
            DecryptResult(true, output, timeTakenMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            DecryptResult(false, "", e.message ?: "Unknown error", System.currentTimeMillis() - start)
        }
    }

    // ─── AUTO DETECT ──────────────────────────────────────────────
    private fun autoDecrypt(input: String, key: String): String {
        val trimmed = input.trim()

        // Try Base64
        try {
            val decoded = base64Decode(trimmed)
            if (decoded.isNotBlank() && decoded.all { it.code in 32..126 || it == '\n' || it == '\r' || it == '\t' }) {
                return "[Auto-Base64]\n$decoded"
            }
        } catch (_: Exception) {}

        // Try Hex
        if (trimmed.matches(Regex("[0-9a-fA-F\\s]+"))) {
            try {
                val decoded = hexDecode(trimmed)
                if (decoded.isNotBlank()) return "[Auto-Hex]\n$decoded"
            } catch (_: Exception) {}
        }

        // Try ROT13
        val rot = rot13(trimmed)
        if (rot != trimmed) return "[Auto-ROT13]\n$rot"

        // Try URL decode
        val urlDec = urlDecode(trimmed)
        if (urlDec != trimmed) return "[Auto-URL]\n$urlDec"

        // Try JWT
        if (trimmed.count { it == '.' } == 2) {
            try { return "[Auto-JWT]\n${jwtDecode(trimmed)}" } catch (_: Exception) {}
        }

        return "Could not auto-detect encoding. Please select an algorithm manually."
    }

    // ─── AES ──────────────────────────────────────────────────────
    private fun aesDecrypt(input: String, key: String, ivStr: String, algo: DecryptAlgorithm): String {
        val keyBytes = deriveKey(key, when (algo) {
            DecryptAlgorithm.AES_128 -> 16
            DecryptAlgorithm.AES_192 -> 24
            else -> 32
        })
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipherData = Base64.decode(input.trim(), Base64.DEFAULT)

        return if (ivStr.isNotBlank()) {
            val iv = IvParameterSpec(hexToBytes(ivStr))
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
            String(cipher.doFinal(cipherData), StandardCharsets.UTF_8)
        } else {
            // Try ECB first, then CBC with first 16 bytes as IV
            try {
                val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                String(cipher.doFinal(cipherData), StandardCharsets.UTF_8)
            } catch (_: Exception) {
                val ivBytes = cipherData.copyOfRange(0, 16)
                val data = cipherData.copyOfRange(16, cipherData.size)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ivBytes))
                String(cipher.doFinal(data), StandardCharsets.UTF_8)
            }
        }
    }

    // ─── DES ──────────────────────────────────────────────────────
    private fun desDecrypt(input: String, key: String, ivStr: String): String {
        val keyBytes = deriveKey(key, 8)
        val secretKey = SecretKeySpec(keyBytes, "DES")
        val cipherData = Base64.decode(input.trim(), Base64.DEFAULT)
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        val iv = if (ivStr.isNotBlank()) IvParameterSpec(hexToBytes(ivStr)) else IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return String(cipher.doFinal(cipherData), StandardCharsets.UTF_8)
    }

    // ─── 3DES ─────────────────────────────────────────────────────
    private fun tripleDesDecrypt(input: String, key: String, ivStr: String): String {
        val keyBytes = deriveKey(key, 24)
        val secretKey = SecretKeySpec(keyBytes, "DESede")
        val cipherData = Base64.decode(input.trim(), Base64.DEFAULT)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = if (ivStr.isNotBlank()) IvParameterSpec(hexToBytes(ivStr)) else IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return String(cipher.doFinal(cipherData), StandardCharsets.UTF_8)
    }

    // ─── Blowfish ─────────────────────────────────────────────────
    private fun blowfishDecrypt(input: String, key: String, ivStr: String): String {
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8).let {
            if (it.size > 56) it.copyOfRange(0, 56) else it
        }
        val secretKey = SecretKeySpec(keyBytes, "Blowfish")
        val cipherData = Base64.decode(input.trim(), Base64.DEFAULT)
        val cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding")
        val iv = if (ivStr.isNotBlank()) IvParameterSpec(hexToBytes(ivStr)) else IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return String(cipher.doFinal(cipherData), StandardCharsets.UTF_8)
    }

    // ─── RC4 ──────────────────────────────────────────────────────
    private fun rc4Decrypt(input: String, key: String): String {
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        val data = Base64.decode(input.trim(), Base64.DEFAULT)
        val s = IntArray(256) { it }
        var j = 0
        for (i in 0..255) {
            j = (j + s[i] + keyBytes[i % keyBytes.size].toInt()) and 0xFF
            val tmp = s[i]; s[i] = s[j]; s[j] = tmp
        }
        var i = 0; j = 0
        val result = ByteArray(data.size)
        for (k in data.indices) {
            i = (i + 1) and 0xFF
            j = (j + s[i]) and 0xFF
            val tmp = s[i]; s[i] = s[j]; s[j] = tmp
            result[k] = (data[k].toInt() xor s[(s[i] + s[j]) and 0xFF]).toByte()
        }
        return String(result, StandardCharsets.UTF_8)
    }

    // ─── XOR ──────────────────────────────────────────────────────
    private fun xorDecrypt(input: String, key: String): String {
        val data = Base64.decode(input.trim(), Base64.DEFAULT)
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        val result = ByteArray(data.size)
        for (i in data.indices) {
            result[i] = (data[i] xor keyBytes[i % keyBytes.size])
        }
        return String(result, StandardCharsets.UTF_8)
    }

    // ─── ENCODINGS ────────────────────────────────────────────────
    fun base64Decode(input: String): String {
        val cleaned = input.trim().replace("\\s".toRegex(), "")
        return String(Base64.decode(cleaned, Base64.DEFAULT or Base64.NO_WRAP), StandardCharsets.UTF_8)
    }

    fun base32Decode(input: String): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = input.trim().uppercase().replace("=", "")
        var bits = 0
        var value = 0
        val result = ByteArrayOutputStream()
        for (c in cleaned) {
            val idx = alphabet.indexOf(c)
            if (idx < 0) continue
            value = (value shl 5) or idx
            bits += 5
            if (bits >= 8) {
                bits -= 8
                result.write((value shr bits) and 0xFF)
            }
        }
        return String(result.toByteArray(), StandardCharsets.UTF_8)
    }

    fun base16Decode(input: String): String = hexDecode(input)

    fun hexDecode(input: String): String {
        val hex = input.trim().replace("\\s".toRegex(), "").replace("0x", "").replace(":", "")
        val bytes = ByteArray(hex.length / 2)
        for (i in bytes.indices) {
            bytes[i] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return String(bytes, StandardCharsets.UTF_8)
    }

    fun binaryDecode(input: String): String {
        val parts = input.trim().split("\\s+".toRegex())
        return parts.joinToString("") { bits ->
            bits.toInt(2).toChar().toString()
        }
    }

    fun octalDecode(input: String): String {
        val parts = input.trim().split("\\s+".toRegex())
        return parts.joinToString("") { oct ->
            oct.toInt(8).toChar().toString()
        }
    }

    fun rot13(input: String): String = input.map { c ->
        when {
            c in 'a'..'z' -> ((c - 'a' + 13) % 26 + 'a'.code).toChar()
            c in 'A'..'Z' -> ((c - 'A' + 13) % 26 + 'A'.code).toChar()
            else -> c
        }
    }.joinToString("")

    fun urlDecode(input: String): String =
        URLDecoder.decode(input.trim(), "UTF-8")

    fun htmlDecode(input: String): String =
        input.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("&#(\\d+);")) { mr -> mr.groupValues[1].toInt().toChar().toString() }
            .replace(Regex("&#x([0-9a-fA-F]+);")) { mr -> mr.groupValues[1].toInt(16).toChar().toString() }

    fun unicodeDecode(input: String): String =
        input.replace(Regex("\\\\u([0-9a-fA-F]{4})")) { mr ->
            mr.groupValues[1].toInt(16).toChar().toString()
        }

    fun jwtDecode(input: String): String {
        val parts = input.trim().split(".")
        if (parts.size < 2) throw IllegalArgumentException("Not a valid JWT")
        val header = String(Base64.decode(padBase64(parts[0]), Base64.DEFAULT), StandardCharsets.UTF_8)
        val payload = String(Base64.decode(padBase64(parts[1]), Base64.DEFAULT), StandardCharsets.UTF_8)
        return "Header:\n$header\n\nPayload:\n$payload\n\n[Signature not verified - offline mode]"
    }

    fun gzipDecompress(input: String): String {
        val compressed = Base64.decode(input.trim(), Base64.DEFAULT)
        val gzis = java.util.zip.GZIPInputStream(ByteArrayInputStream(compressed))
        return gzis.bufferedReader(StandardCharsets.UTF_8).readText()
    }

    fun zlibDecompress(input: String): String {
        val compressed = Base64.decode(input.trim(), Base64.DEFAULT)
        val inflater = java.util.zip.Inflater()
        inflater.setInput(compressed)
        val out = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!inflater.finished()) {
            val count = inflater.inflate(buf)
            out.write(buf, 0, count)
        }
        inflater.end()
        return String(out.toByteArray(), StandardCharsets.UTF_8)
    }

    // ─── HELPERS ──────────────────────────────────────────────────
    private fun deriveKey(key: String, length: Int): ByteArray {
        if (key.length >= length) return key.toByteArray(StandardCharsets.UTF_8).copyOfRange(0, length)
        val padded = ByteArray(length)
        val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
        for (i in 0 until length) padded[i] = keyBytes[i % keyBytes.size]
        return padded
    }

    private fun hexToBytes(hex: String): ByteArray {
        val cleaned = hex.trim().replace("\\s".toRegex(), "")
        return ByteArray(cleaned.length / 2) { i ->
            cleaned.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    private fun padBase64(s: String): String {
        val pad = (4 - s.length % 4) % 4
        return s + "=".repeat(pad)
    }
}
