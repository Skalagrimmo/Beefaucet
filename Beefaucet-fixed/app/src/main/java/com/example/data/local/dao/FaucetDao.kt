package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FaucetClaimEntity
import com.example.data.local.entity.FaucetSettingsEntity
import com.example.data.local.entity.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FaucetDao {
    @Query("SELECT * FROM faucet_claims ORDER BY timestamp DESC")
    fun getAllClaims(): Flow<List<FaucetClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: FaucetClaimEntity): Long

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity): Long

    @Query("SELECT * FROM faucet_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<FaucetSettingsEntity?>

    @Query("SELECT * FROM faucet_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): FaucetSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: FaucetSettingsEntity)

    @Update
    suspend fun updateSettings(settings: FaucetSettingsEntity)
}
