package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "FAUCET_CLAIM", "AUTO_WITHDRAWAL", "MANUAL_WITHDRAWAL"
    val amount: Double,
    val toAddress: String,
    val txHash: String,
    val status: String = "CONFIRMED", // "CONFIRMED", "PROCESSING"
    val note: String = ""
)
