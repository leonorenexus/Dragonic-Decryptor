package com.dragonic.decryptor

import android.app.Application
import com.dragonic.decryptor.data.db.DragonicDatabase
import com.dragonic.decryptor.data.repository.DragonicRepository

class DragonicApp : Application() {

    val database by lazy { DragonicDatabase.getInstance(this) }
    val repository by lazy {
        DragonicRepository(
            database.historyDao(),
            database.savedFileDao()
        )
    }
}
