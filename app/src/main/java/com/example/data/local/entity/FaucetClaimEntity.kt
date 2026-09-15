package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faucet_claims")
data class FaucetClaimEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    // TODO: Real reward amount queried from on-chain transaction or faucet API
    val amountBee: Double = 0.0,
    val captchaType: String,
    // TODO: Real on-chain transaction hash returned by faucet service/node
    val txHash: String = "",
    val faucetSource: String = "beefaucet.org"
)
