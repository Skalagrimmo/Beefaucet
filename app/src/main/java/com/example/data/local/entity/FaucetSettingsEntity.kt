package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faucet_settings")
data class FaucetSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val walletAddress: String = "0xBee94Fa7842c67E4890d2C1d6E39Af76326E1E09",
    val walletBalance: Double = 0.0325,
    val totalClaimed: Double = 0.0825,
    val claimStreak: Int = 3,
    val nextClaimEpochMs: Long = 0L,
    val reminderIntervalMinutes: Int = 5,
    val pushNotificationEnabled: Boolean = true,
    val autoWithdrawalEnabled: Boolean = true,
    val autoWithdrawalThreshold: Double = 0.0500,
    val autoWithdrawalDestination: String = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
