package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faucet_settings")
data class FaucetSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    // TODO: Real wallet address generation or integration required (e.g. BIP-39/BIP-44 or Web3 connection)
    val walletAddress: String = "",
    // TODO: Real balance fetch from on-chain RPC / node API required
    val walletBalance: Double = 0.0,
    // TODO: Real total claimed amount tracking from blockchain/backend API required
    val totalClaimed: Double = 0.0,
    val claimStreak: Int = 0,
    val nextClaimEpochMs: Long = 0L,
    val reminderIntervalMinutes: Int = 5,
    val pushNotificationEnabled: Boolean = true,
    val autoWithdrawalEnabled: Boolean = false,
    // TODO: User-defined threshold, no hardcoded default
    val autoWithdrawalThreshold: Double = 0.0,
    // TODO: Real user-provided payout destination address required
    val autoWithdrawalDestination: String = "",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
