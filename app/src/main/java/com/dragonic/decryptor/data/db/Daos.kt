package com.dragonic.decryptor.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DecryptionHistoryDao {

    @Query("SELECT * FROM decryption_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<DecryptionHistoryEntity>>

    @Query("SELECT * FROM decryption_history WHERE isSuccess = 1 ORDER BY timestamp DESC")
    fun getSuccessHistory(): Flow<List<DecryptionHistoryEntity>>

    @Query("SELECT * FROM decryption_history WHERE isSuccess = 0 ORDER BY timestamp DESC")
    fun getFailedHistory(): Flow<List<DecryptionHistoryEntity>>

    @Query("SELECT * FROM decryption_history WHERE isFile = 1 ORDER BY timestamp DESC")
    fun getFileHistory(): Flow<List<DecryptionHistoryEntity>>

    @Query("SELECT * FROM decryption_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHistory(): Flow<List<DecryptionHistoryEntity>>

    @Query("SELECT * FROM decryption_history WHERE algorithm LIKE '%' || :query || '%' OR fileName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<DecryptionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DecryptionHistoryEntity): Long

    @Update
    suspend fun update(entity: DecryptionHistoryEntity)

    @Query("UPDATE decryption_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM decryption_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM decryption_history")
    suspend fun clearAll()
}

@Dao
interface SavedFileDao {

    @Query("SELECT * FROM saved_files ORDER BY savedAt DESC")
    fun getAllFiles(): Flow<List<SavedFileEntity>>

    @Query("SELECT * FROM saved_files WHERE name LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchFiles(query: String): Flow<List<SavedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedFileEntity): Long

    @Query("UPDATE saved_files SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM saved_files WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM saved_files")
    suspend fun clearAll()
}
