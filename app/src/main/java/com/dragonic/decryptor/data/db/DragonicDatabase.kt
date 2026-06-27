package com.dragonic.decryptor.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DecryptionHistoryEntity::class, SavedFileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DragonicDatabase : RoomDatabase() {

    abstract fun historyDao(): DecryptionHistoryDao
    abstract fun savedFileDao(): SavedFileDao

    companion object {
        @Volatile private var INSTANCE: DragonicDatabase? = null

        fun getInstance(context: Context): DragonicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DragonicDatabase::class.java,
                    "dragonic_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
