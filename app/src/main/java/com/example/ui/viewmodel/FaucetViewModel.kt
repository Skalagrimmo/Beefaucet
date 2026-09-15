package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FaucetClaimEntity
import com.example.data.local.entity.FaucetSettingsEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.data.repository.FaucetRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

data class FaucetUiState(
    val settings: FaucetSettingsEntity = FaucetSettingsEntity(),
    val claims: List<FaucetClaimEntity> = emptyList(),
    val transactions: List<WalletTransactionEntity> = emptyList(),
    val timeRemainingMs: Long = 0L,
    val isFaucetReady: Boolean = true,
    val isClaiming: Boolean = false,
    val selectedCaptchaMode: CaptchaMode = CaptchaMode.SLIDER,
    val showClaimSuccessDialog: Boolean = false,
    val lastClaimReward: Double = 0.0,
    val lastTxHash: String = "",
    val lastAutoWithdrawal: WalletTransactionEntity? = null,
    val currentTab: String = "FAUCET"
)

enum class CaptchaMode {
    SLIDER, MATRIX, MATH
}

class FaucetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FaucetRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FaucetRepository(database.faucetDao())
        viewModelScope.launch {
            repository.getOrInitSettings()
        }
    }

    private val _uiState = MutableStateFlow(FaucetUiState())
    val uiState: StateFlow<FaucetUiState> = _uiState.asStateFlow()

    init {
        NotificationHelper.createNotificationChannel(application)

        // Combine DB flows into UI State
        viewModelScope.launch {
            combine(
                repository.settingsFlow,
                repository.allClaims,
                repository.allTransactions
            ) { settings, claims, transactions ->
                val currentSettings = settings ?: FaucetSettingsEntity()
                val now = System.currentTimeMillis()
                val remaining = max(0L, currentSettings.nextClaimEpochMs - now)
                val ready = remaining == 0L

                _uiState.update { current ->
                    current.copy(
                        settings = currentSettings,
                        claims = claims,
                        transactions = transactions,
                        timeRemainingMs = remaining,
                        isFaucetReady = ready
                    )
                }
            }.collect {}
        }

        // 1-second countdown ticker
        viewModelScope.launch {
            var previousReadyState = false
            while (isActive) {
                val nextClaim = _uiState.value.settings.nextClaimEpochMs
                val now = System.currentTimeMillis()
                val remaining = max(0L, nextClaim - now)
                val ready = remaining == 0L

                _uiState.update {
                    it.copy(
                        timeRemainingMs = remaining,
                        isFaucetReady = ready
                    )
                }

                // Push notification when transition from not-ready to ready happens
                if (!previousReadyState && ready && nextClaim > 0L) {
                    if (_uiState.value.settings.pushNotificationEnabled) {
                        NotificationHelper.sendFaucetReadyNotification(getApplication())
                    }
                    if (_uiState.value.settings.vibrationEnabled) {
                        NotificationHelper.triggerVibration(getApplication())
                    }
                }
                previousReadyState = ready
                delay(1000L)
            }
        }
    }

    fun setTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setCaptchaMode(mode: CaptchaMode) {
        _uiState.update { it.copy(selectedCaptchaMode = mode) }
    }

    fun onCaptchaSolved(context: Context) {
        if (_uiState.value.isClaiming) return

        viewModelScope.launch {
            _uiState.update { it.copy(isClaiming = true) }

            // Slight authentic cryptographic hashing simulation
            delay(500L)

            // Random claim reward between 0.0050 and 0.0125 BEE
            val rewardAmount = (50 + (0..75).random()) / 10000.0
            val captchaName = when (_uiState.value.selectedCaptchaMode) {
                CaptchaMode.SLIDER -> "Honeycomb Alignment"
                CaptchaMode.MATRIX -> "Pattern Matrix"
                CaptchaMode.MATH -> "Hex Nonce Checksum"
            }

            val (claim, autoWithdrawal) = repository.recordClaim(rewardAmount, captchaName)

            if (_uiState.value.settings.vibrationEnabled) {
                NotificationHelper.triggerVibration(context)
            }

            // If auto-withdrawal was triggered, trigger push notification!
            if (autoWithdrawal != null && _uiState.value.settings.pushNotificationEnabled) {
                NotificationHelper.sendAutoWithdrawalNotification(
                    context = context,
                    amount = autoWithdrawal.amount,
                    destination = autoWithdrawal.toAddress,
                    txHash = autoWithdrawal.txHash
                )
            }

            _uiState.update {
                it.copy(
                    isClaiming = false,
                    showClaimSuccessDialog = true,
                    lastClaimReward = rewardAmount,
                    lastTxHash = claim.txHash,
                    lastAutoWithdrawal = autoWithdrawal
                )
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showClaimSuccessDialog = false) }
    }

    fun updateReminderInterval(minutes: Int) {
        viewModelScope.launch {
            val s = _uiState.value.settings
            repository.updateSettings(s.copy(reminderIntervalMinutes = minutes))
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val s = _uiState.value.settings
            repository.updateSettings(s.copy(pushNotificationEnabled = enabled))
        }
    }

    fun toggleAutoWithdrawal(enabled: Boolean) {
        viewModelScope.launch {
            val s = _uiState.value.settings
            repository.updateSettings(s.copy(autoWithdrawalEnabled = enabled))
        }
    }

    fun updateAutoWithdrawalThreshold(threshold: Double) {
        viewModelScope.launch {
            val s = _uiState.value.settings
            repository.updateSettings(s.copy(autoWithdrawalThreshold = threshold))
        }
    }

    fun updateAutoWithdrawalDestination(address: String) {
        viewModelScope.launch {
            val s = _uiState.value.settings
            repository.updateSettings(s.copy(autoWithdrawalDestination = address))
        }
    }

    fun resetTimerForTesting() {
        viewModelScope.launch {
            repository.resetNextClaimNow()
        }
    }

    fun sendTestPushNotification(context: Context) {
        NotificationHelper.sendFaucetReadyNotification(context)
    }

    fun executeManualWithdrawal(
        amount: Double,
        destination: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.executeManualWithdrawal(amount, destination)
            result.fold(
                onSuccess = { tx ->
                    onComplete(true, "Transferred %.4f BEE to %s".format(amount, destination.take(8)))
                },
                onFailure = { error ->
                    onComplete(false, error.message ?: "Withdrawal failed")
                }
            )
        }
    }
}
