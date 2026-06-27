package com.dragonic.decryptor.domain.model

data class DecryptionRecord(
    val id: Long = 0,
    val algorithm: String,
    val inputData: String,
    val outputData: String,
    val keyHint: String = "",
    val ivUsed: String = "",
    val timeTakenMs: Long = 0,
    val isSuccess: Boolean,
    val isFile: Boolean = false,
    val fileName: String = "",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class SavedFile(
    val id: Long = 0,
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val savedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class AnalysisResult(
    val fileName: String = "",
    val fileSizeBytes: Long = 0,
    val fileType: String = "",
    val entropy: Double = 0.0,
    val possibleAlgorithms: List<String> = emptyList(),
    val confidence: Int = 0,
    val headerSignature: String = "",
    val isCompressed: Boolean = false,
    val entropyPoints: List<Float> = emptyList()
)

enum class ToolCategory { SYMMETRIC, ASYMMETRIC, HASH, DECODER, OTHER }

data class ToolItem(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val iconRes: Int
)

enum class DecryptAlgorithm(val label: String) {
    AUTO("Auto Detect"),
    AES_128("AES-128"),
    AES_192("AES-192"),
    AES_256("AES-256"),
    DES("DES"),
    TRIPLE_DES("3DES / Triple DES"),
    BLOWFISH("Blowfish"),
    RC4("RC4"),
    CHACHA20("ChaCha20"),
    XOR("XOR"),
    BASE64("Base64 Decode"),
    BASE32("Base32 Decode"),
    BASE16("Base16 Decode"),
    HEX("Hex Decode"),
    BINARY("Binary Decode"),
    OCTAL("Octal Decode"),
    ROT13("ROT13"),
    URL("URL Decode"),
    HTML("HTML Decode"),
    UNICODE("Unicode Decode"),
    JWT("JWT Decode"),
    GZIP("Gzip Decompress"),
    ZLIB("Zlib Decompress")
}
