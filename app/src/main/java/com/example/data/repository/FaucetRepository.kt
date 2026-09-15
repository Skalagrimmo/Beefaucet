package com.example.data.repository

import com.example.data.local.dao.FaucetDao
import com.example.data.local.entity.FaucetClaimEntity
import com.example.data.local.entity.FaucetSettingsEntity
import com.example.data.local.entity.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

class FaucetRepository(private val faucetDao: FaucetDao) {

    val allClaims: Flow<List<FaucetClaimEntity>> = faucetDao.getAllClaims()
    val allTransactions: Flow<List<WalletTransactionEntity>> = faucetDao.getAllTransactions()
    val settingsFlow: Flow<FaucetSettingsEntity?> = faucetDao.getSettingsFlow()

    suspend fun getOrInitSettings(): FaucetSettingsEntity {
        val existing = faucetDao.getSettingsDirect()
        if (existing != null) {
            return existing
        }
        val initial = FaucetSettingsEntity()
        faucetDao.insertOrUpdateSettings(initial)
        return initial
    }

    suspend fun insertClaimOnly(faucetName: String, url: String): FaucetClaimEntity {
        // TODO: Real on-chain transaction hash and reward amount should be retrieved from faucet API/RPC
        val claim = FaucetClaimEntity(
            amountBee = 0.0,
            captchaType = faucetName,
            txHash = "" // TODO: Real on-chain transaction hash from faucet backend/RPC
        )
        faucetDao.insertClaim(claim)

        val currentSettings = getOrInitSettings()
        faucetDao.insertOrUpdateSettings(
            currentSettings.copy(
                claimStreak = currentSettings.claimStreak + 1,
                nextClaimEpochMs = System.currentTimeMillis() + 60_000L
            )
        )
        return claim
    }

    suspend fun updateSettings(settings: FaucetSettingsEntity) {
        faucetDao.insertOrUpdateSettings(settings)
    }

    /**
     * Executes a faucet claim.
     * TODO: Requires real network call to faucet smart contract or backend service to claim tokens.
     */
    suspend fun recordClaim(
        amount: Double,
        captchaType: String
    ): Pair<FaucetClaimEntity, WalletTransactionEntity?> {
        // TODO: Real on-chain transaction and network call required
        val currentSettings = getOrInitSettings()
        val claim = FaucetClaimEntity(
            amountBee = amount,
            captchaType = captchaType,
            txHash = "" // TODO: Real transaction hash from blockchain RPC
        )
        faucetDao.insertClaim(claim)

        // TODO: Real transaction receipt verification needed before recording confirmed status
        val claimTx = WalletTransactionEntity(
            type = "FAUCET_CLAIM",
            amount = amount,
            toAddress = currentSettings.walletAddress,
            txHash = "", // TODO: Real on-chain txHash
            status = "", // TODO: Real confirmation status ("PENDING" / "CONFIRMED")
            note = "Claim for $captchaType verification"
        )
        faucetDao.insertTransaction(claimTx)

        return Pair(claim, null)
    }

    suspend fun updateWalletBalance(balance: Double) {
        val current = getOrInitSettings()
        faucetDao.insertOrUpdateSettings(current.copy(walletBalance = balance))
    }

    suspend fun updateWalletAddress(address: String) {
        val current = getOrInitSettings()
        faucetDao.insertOrUpdateSettings(current.copy(walletAddress = address))
    }

    suspend fun resetNextClaimNow() {
        val settings = getOrInitSettings()
        faucetDao.insertOrUpdateSettings(settings.copy(nextClaimEpochMs = 0L))
    }
}
