package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.FaucetDao
import com.example.data.local.entity.FaucetClaimEntity
import com.example.data.local.entity.FaucetSettingsEntity
import com.example.data.local.entity.WalletTransactionEntity

@Database(
    entities = [
        FaucetClaimEntity::class,
        WalletTransactionEntity::class,
        FaucetSettingsEntity::class
    ],
    version = 1,
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
