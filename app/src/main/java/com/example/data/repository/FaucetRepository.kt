package com.example.data.repository

import com.example.data.local.dao.FaucetDao
import com.example.data.local.entity.FaucetClaimEntity
import com.example.data.local.entity.FaucetSettingsEntity
import com.example.data.local.entity.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom

class FaucetRepository(private val faucetDao: FaucetDao) {

    val allClaims: Flow<List<FaucetClaimEntity>> = faucetDao.getAllClaims()
    val allTransactions: Flow<List<WalletTransactionEntity>> = faucetDao.getAllTransactions()
    val settingsFlow: Flow<FaucetSettingsEntity?> = faucetDao.getSettingsFlow()

    private val random = SecureRandom()

    private fun generateRandomTxHash(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val sb = StringBuilder("0x")
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

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
        val txHash = generateRandomTxHash()
        val claim = FaucetClaimEntity(
            amountBee = 0.0,
            captchaType = faucetName,
            txHash = txHash
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
     * Executes a faucet claim, credits wallet, calculates next claim countdown,
     * and triggers automated withdrawal if balance >= threshold.
     */
    suspend fun recordClaim(
        amount: Double,
        captchaType: String
    ): Pair<FaucetClaimEntity, WalletTransactionEntity?> {
        val currentSettings = getOrInitSettings()
        val txHash = generateRandomTxHash()

        val claim = FaucetClaimEntity(
            amountBee = amount,
            captchaType = captchaType,
            txHash = txHash
        )
        faucetDao.insertClaim(claim)

        // Record credit in wallet ledger
        val claimTx = WalletTransactionEntity(
            type = "FAUCET_CLAIM",
            amount = amount,
            toAddress = currentSettings.walletAddress,
            txHash = txHash,
            status = "CONFIRMED",
            note = "Reward for $captchaType verification"
        )
        faucetDao.insertTransaction(claimTx)

        val updatedBalance = currentSettings.walletBalance + amount
        val updatedTotal = currentSettings.totalClaimed + amount
        val nextClaimMs = System.currentTimeMillis() + (currentSettings.reminderIntervalMinutes * 60 * 1000L)

        var autoWithdrawalTx: WalletTransactionEntity? = null
        var finalBalance = updatedBalance

        // Check Automated Withdrawal Trigger
        if (currentSettings.autoWithdrawalEnabled &&
            updatedBalance >= currentSettings.autoWithdrawalThreshold &&
            currentSettings.autoWithdrawalDestination.isNotBlank()
        ) {
            val withdrawAmount = currentSettings.autoWithdrawalThreshold
            finalBalance = (updatedBalance - withdrawAmount).coerceAtLeast(0.0)
            val withdrawTxHash = generateRandomTxHash()

            autoWithdrawalTx = WalletTransactionEntity(
                type = "AUTO_WITHDRAWAL",
                amount = withdrawAmount,
                toAddress = currentSettings.autoWithdrawalDestination,
                txHash = withdrawTxHash,
                status = "CONFIRMED",
                note = "Auto-threshold triggered (>= ${currentSettings.autoWithdrawalThreshold} BEE)"
            )
            faucetDao.insertTransaction(autoWithdrawalTx)
        }

        val updatedSettings = currentSettings.copy(
            walletBalance = finalBalance,
            totalClaimed = updatedTotal,
            claimStreak = currentSettings.claimStreak + 1,
            nextClaimEpochMs = nextClaimMs
        )
        faucetDao.insertOrUpdateSettings(updatedSettings)

        return Pair(claim, autoWithdrawalTx)
    }

    suspend fun executeManualWithdrawal(
        amount: Double,
        destinationAddress: String
    ): Result<WalletTransactionEntity> {
        val currentSettings = getOrInitSettings()
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        }
        if (amount > currentSettings.walletBalance) {
            return Result.failure(IllegalStateException("Insufficient balance"))
        }
        if (destinationAddress.isBlank() || !destinationAddress.startsWith("0x") || destinationAddress.length < 10) {
            return Result.failure(IllegalArgumentException("Invalid crypto destination address (must start with 0x)"))
        }

        val txHash = generateRandomTxHash()
        val withdrawalTx = WalletTransactionEntity(
            type = "MANUAL_WITHDRAWAL",
            amount = amount,
            toAddress = destinationAddress,
            txHash = txHash,
            status = "CONFIRMED",
            note = "Manual wallet payout"
        )
        faucetDao.insertTransaction(withdrawalTx)

        val newBalance = (currentSettings.walletBalance - amount).coerceAtLeast(0.0)
        faucetDao.insertOrUpdateSettings(currentSettings.copy(walletBalance = newBalance))

        return Result.success(withdrawalTx)
    }

    suspend fun resetNextClaimNow() {
        val settings = getOrInitSettings()
        faucetDao.insertOrUpdateSettings(settings.copy(nextClaimEpochMs = 0L))
    }
}
