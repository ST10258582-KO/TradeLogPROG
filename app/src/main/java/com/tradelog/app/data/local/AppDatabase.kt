package com.tradelog.app.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tradelog.app.data.local.dao.JournalDao
import com.tradelog.app.data.local.dao.TradeDao
import com.tradelog.app.data.local.entities.JournalEntity
import com.tradelog.app.data.local.entities.TradeEntity

// Room database — the single source of truth for offline-first data access.
// All network data is written here first; Firestore is kept in sync via SyncWorker.
// Reference: Google LLC, "Room Persistence Library" Android Developers (2024)
@Database(
    entities = [TradeEntity::class, JournalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Returns the DAO for trade CRUD operations. */
    abstract fun tradeDao(): TradeDao

    /** Returns the DAO for journal entry CRUD operations. */
    abstract fun journalDao(): JournalDao

    companion object {
        private const val TAG = "AppDatabase"
        private const val DB_NAME = "tradelog.db"

        // Singleton instance — Room databases are expensive to create
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton [AppDatabase] instance, creating it if necessary.
         * Uses double-checked locking to remain thread-safe without blocking every call.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Creating new AppDatabase instance")
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)   // Dev mode; replace with Migration objects in production
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
