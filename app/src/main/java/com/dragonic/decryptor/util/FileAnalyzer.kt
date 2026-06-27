package com.dragonic.decryptor.util

import com.dragonic.decryptor.domain.model.AnalysisResult
import kotlin.math.log2

object FileAnalyzer {

    fun analyze(data: ByteArray, fileName: String = ""): AnalysisResult {
        val entropy = calculateEntropy(data)
        val fileType = detectFileType(data, fileName)
        val algorithms = guessPossibleAlgorithms(data, entropy)
        val confidence = calculateConfidence(data, entropy, algorithms)
        val signature = getHeaderSignature(data)
        val entropyPoints = calculateEntropyPoints(data)

        return AnalysisResult(
            fileName = fileName,
            fileSizeBytes = data.size.toLong(),
            fileType = fileType,
            entropy = entropy,
            possibleAlgorithms = algorithms,
            confidence = confidence,
            headerSignature = signature,
            isCompressed = isLikelyCompressed(data),
            entropyPoints = entropyPoints
        )
    }

    fun analyzeText(text: String): AnalysisResult {
        return analyze(text.toByteArray(Charsets.UTF_8), "text_input.txt")
    }

    private fun calculateEntropy(data: ByteArray): Double {
        if (data.isEmpty()) return 0.0
        val freq = IntArray(256)
        for (b in data) freq[b.toInt() and 0xFF]++
        var entropy = 0.0
        val n = data.size.toDouble()
        for (f in freq) {
            if (f > 0) {
                val p = f / n
                entropy -= p * log2(p)
            }
        }
        return entropy
    }

    private fun calculateEntropyPoints(data: ByteArray, segments: Int = 20): List<Float> {
        if (data.size < segments) return List(segments) { calculateEntropy(data).toFloat() }
        val segSize = data.size / segments
        return (0 until segments).map { i ->
            val segment = data.copyOfRange(i * segSize, minOf((i + 1) * segSize, data.size))
            calculateEntropy(segment).toFloat()
        }
    }

    private fun detectFileType(data: ByteArray, fileName: String): String {
        if (data.isEmpty()) return "Empty"
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when {
            data.size >= 4 && data[0] == 0x50.toByte() && data[1] == 0x4B.toByte() -> "ZIP Archive"
            data.size >= 3 && data[0] == 0x1F.toByte() && data[1] == 0x8B.toByte() -> "GZIP Compressed"
            data.size >= 4 && data[0] == 0x25.toByte() && data[1] == 0x50.toByte() && data[2] == 0x44.toByte() -> "PDF Document"
            data.size >= 4 && data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() -> "JPEG Image"
            data.size >= 4 && data[0] == 0x89.toByte() && data[1] == 0x50.toByte() -> "PNG Image"
            ext in listOf("txt", "log", "md") -> "Text File"
            ext in listOf("json") -> "JSON Data"
            ext in listOf("xml") -> "XML Data"
            ext in listOf("enc", "dat", "bin") -> "Binary / Encrypted"
            ext in listOf("db", "sqlite") -> "Database File"
            isTextData(data) -> "Plain Text"
            else -> "Binary File"
        }
    }

    private fun isTextData(data: ByteArray): Boolean {
        var textCount = 0
        for (b in data.take(512)) {
            val c = b.toInt() and 0xFF
            if (c in 32..126 || c == 9 || c == 10 || c == 13) textCount++
        }
        return textCount.toDouble() / minOf(data.size, 512) > 0.85
    }

    private fun guessPossibleAlgorithms(data: ByteArray, entropy: Double): List<String> {
        val list = mutableListOf<String>()
        return when {
            entropy > 7.5 -> {
                list.add("AES-256-CBC"); list.add("AES-128-CBC")
                list.add("Blowfish"); list.add("ChaCha20"); list
            }
            entropy > 6.0 -> {
                list.add("DES"); list.add("3DES")
                list.add("RC4"); list.add("XOR"); list
            }
            entropy < 4.0 -> {
                list.add("Base64"); list.add("ROT13")
                list.add("Caesar Cipher"); list.add("XOR"); list
            }
            else -> {
                list.add("AES / Blowfish"); list.add("RC4"); list
            }
        }
    }

    private fun calculateConfidence(data: ByteArray, entropy: Double, algos: List<String>): Int {
        return when {
            entropy > 7.8 -> 95
            entropy > 7.0 -> 85
            entropy > 6.0 -> 70
            entropy > 4.0 -> 55
            else -> 40
        }
    }

    private fun getHeaderSignature(data: ByteArray): String {
        if (data.isEmpty()) return "N/A"
        val headerBytes = data.take(minOf(8, data.size))
        return headerBytes.joinToString(" ") { String.format("%02X", it.toInt() and 0xFF) }
    }

    private fun isLikelyCompressed(data: ByteArray): Boolean {
        if (data.size < 2) return false
        return (data[0] == 0x1F.toByte() && data[1] == 0x8B.toByte()) ||
               (data[0] == 0x50.toByte() && data[1] == 0x4B.toByte()) ||
               (data[0] == 0x78.toByte() && (data[1] == 0x9C.toByte() || data[1] == 0xDA.toByte()))
    }

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> String.format("%.2f MB", bytes / (1024.0 * 1024))
    }
}
