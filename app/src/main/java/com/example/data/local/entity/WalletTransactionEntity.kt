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
    // TODO: Real transaction hash returned by blockchain RPC/node upon network broadcast
    val txHash: String = "",
    // TODO: Real transaction confirmation status from on-chain receipt ("PENDING", "CONFIRMED", "FAILED")
    val status: String = "",
    val note: String = ""
)
