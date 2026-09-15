package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faucet_claims")
data class FaucetClaimEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amountBee: Double,
    val captchaType: String,
    val txHash: String,
    val faucetSource: String = "Bee Faucet Hive-1"
)
