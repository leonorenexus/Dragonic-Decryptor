package com.dragonic.decryptor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decryption_history")
data class DecryptionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(tableName = "saved_files")
data class SavedFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val savedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
