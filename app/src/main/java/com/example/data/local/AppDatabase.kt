package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FaucetDao
import com.example.data.local.entity.FaucetClaimEntity
import com.example.data.local.entity.FaucetSettingsEntity
import com.example.data.local.entity.WalletTransactionEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS faucet_settings_new (
                id INTEGER PRIMARY KEY NOT NULL,
                walletAddress TEXT NOT NULL,
                walletBalance REAL NOT NULL,
                totalClaimed REAL NOT NULL,
                claimStreak INTEGER NOT NULL,
                nextClaimEpochMs INTEGER NOT NULL,
                reminderIntervalMinutes INTEGER NOT NULL,
                pushNotificationEnabled INTEGER NOT NULL,
                soundEnabled INTEGER NOT NULL,
                vibrationEnabled INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO faucet_settings_new (
                id, walletAddress, walletBalance, totalClaimed, claimStreak,
                nextClaimEpochMs, reminderIntervalMinutes, pushNotificationEnabled,
                soundEnabled, vibrationEnabled
            )
            SELECT
                id, walletAddress, walletBalance, totalClaimed, claimStreak,
                nextClaimEpochMs, reminderIntervalMinutes, pushNotificationEnabled,
                soundEnabled, vibrationEnabled
            FROM faucet_settings
            """.trimIndent()
        )

        db.execSQL("DROP TABLE faucet_settings")
        db.execSQL("ALTER TABLE faucet_settings_new RENAME TO faucet_settings")
    }
}

@Database(
    entities = [
        FaucetClaimEntity::class,
        WalletTransactionEntity::class,
        FaucetSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun faucetDao(): FaucetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bee_faucet_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
