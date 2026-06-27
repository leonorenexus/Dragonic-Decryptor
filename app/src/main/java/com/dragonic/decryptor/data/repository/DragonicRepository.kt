package com.dragonic.decryptor.data.repository

import com.dragonic.decryptor.data.db.*
import com.dragonic.decryptor.domain.model.DecryptionRecord
import com.dragonic.decryptor.domain.model.SavedFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DragonicRepository(
    private val historyDao: DecryptionHistoryDao,
    private val savedFileDao: SavedFileDao
) {
    // ─── HISTORY ─────────────────────────────────────────────────
    fun getAllHistory(): Flow<List<DecryptionRecord>> =
        historyDao.getAllHistory().map { list -> list.map { it.toDomain() } }

    fun getSuccessHistory(): Flow<List<DecryptionRecord>> =
        historyDao.getSuccessHistory().map { list -> list.map { it.toDomain() } }

    fun getFailedHistory(): Flow<List<DecryptionRecord>> =
        historyDao.getFailedHistory().map { list -> list.map { it.toDomain() } }

    fun getFileHistory(): Flow<List<DecryptionRecord>> =
        historyDao.getFileHistory().map { list -> list.map { it.toDomain() } }

    fun getFavoriteHistory(): Flow<List<DecryptionRecord>> =
        historyDao.getFavoriteHistory().map { list -> list.map { it.toDomain() } }

    fun searchHistory(query: String): Flow<List<DecryptionRecord>> =
        historyDao.searchHistory(query).map { list -> list.map { it.toDomain() } }

    suspend fun insertHistory(record: DecryptionRecord): Long =
        historyDao.insert(record.toEntity())

    suspend fun toggleFavoriteHistory(id: Long, isFavorite: Boolean) =
        historyDao.toggleFavorite(id, isFavorite)

    suspend fun deleteHistory(id: Long) = historyDao.delete(id)
    suspend fun clearAllHistory() = historyDao.clearAll()

    // ─── FILES ───────────────────────────────────────────────────
    fun getAllFiles(): Flow<List<SavedFile>> =
        savedFileDao.getAllFiles().map { list -> list.map { it.toDomain() } }

    fun searchFiles(query: String): Flow<List<SavedFile>> =
        savedFileDao.searchFiles(query).map { list -> list.map { it.toDomain() } }

    suspend fun insertFile(file: SavedFile): Long = savedFileDao.insert(file.toEntity())
    suspend fun toggleFavoriteFile(id: Long, isFavorite: Boolean) = savedFileDao.toggleFavorite(id, isFavorite)
    suspend fun deleteFile(id: Long) = savedFileDao.delete(id)
    suspend fun clearAllFiles() = savedFileDao.clearAll()
}

// ─── MAPPERS ─────────────────────────────────────────────────────
fun DecryptionHistoryEntity.toDomain() = DecryptionRecord(id, algorithm, inputData, outputData, keyHint, ivUsed, timeTakenMs, isSuccess, isFile, fileName, isFavorite, timestamp)
fun DecryptionRecord.toEntity() = DecryptionHistoryEntity(id, algorithm, inputData, outputData, keyHint, ivUsed, timeTakenMs, isSuccess, isFile, fileName, isFavorite, timestamp)
fun SavedFileEntity.toDomain() = SavedFile(id, name, path, size, mimeType, savedAt, isFavorite)
fun SavedFile.toEntity() = SavedFileEntity(id, name, path, size, mimeType, savedAt, isFavorite)
