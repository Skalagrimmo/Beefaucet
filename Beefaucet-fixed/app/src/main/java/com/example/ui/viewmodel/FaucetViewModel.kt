package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FaucetClaimEntity
import com.example.data.local.entity.FaucetSettingsEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.data.remote.CoinGeckoPriceService
import com.example.data.remote.RpcNetwork
import com.example.data.remote.Web3BalanceService
import com.example.data.remote.availableRpcNetworks
import com.example.data.repository.FaucetRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FaucetItem(
    val id: String,
    val name: String,
    val coinSymbol: String,
    val coinIcon: String,
    val url: String = "https://beefaucet.org",
    val dailyClaims: Int = 0,
    val maxDailyClaims: Int = 10,
    val cooldownSecondsRemaining: Int = 0,
    val lastClaimEpochMs: Long = 0L
) {
    val isReady: Boolean get() = dailyClaims < maxDailyClaims && cooldownSecondsRemaining == 0
    val isLimitReached: Boolean get() = dailyClaims >= maxDailyClaims
}

val defaultBeeFaucets = listOf(
    FaucetItem(
        id = "beefaucet_main",
        name = "Bee Faucet (Main)",
        coinSymbol = "BEE",
        coinIcon = "🐝",
        url = "https://beefaucet.org"
    ),
    FaucetItem(
        id = "beefaucet_btc",
        name = "Bitcoin Faucet",
        coinSymbol = "BTC",
        coinIcon = "₿",
        url = "https://beefaucet.org/btc-faucet/"
    ),
    FaucetItem(
        id = "beefaucet_ltc",
        name = "Litecoin Faucet",
        coinSymbol = "LTC",
        coinIcon = "Ł",
        url = "https://beefaucet.org/ltc-faucet/"
    ),
    FaucetItem(
        id = "beefaucet_doge",
        name = "Dogecoin Faucet",
        coinSymbol = "DOGE",
        coinIcon = "Ð",
        url = "https://beefaucet.org/doge-faucet/"
    ),
    FaucetItem(
        id = "beefaucet_trx",
        name = "TRON Faucet",
        coinSymbol = "TRX",
        coinIcon = "⟠",
        url = "https://beefaucet.org/trx-faucet/"
    ),
    FaucetItem(
        id = "beefaucet_bnb",
        name = "BNB Chain Faucet",
        coinSymbol = "BNB",
        coinIcon = "🔶",
        url = "https://beefaucet.org/bnb-faucet/"
    ),
    FaucetItem(
        id = "beefaucet_sol",
        name = "Solana Faucet",
        coinSymbol = "SOL",
        coinIcon = "◎",
        url = "https://beefaucet.org/sol-faucet/"
    ),
    FaucetItem(
        id = "beefaucet_usdt",
        name = "Tether Faucet",
        coinSymbol = "USDT",
        coinIcon = "₮",
        url = "https://beefaucet.org/usdt-faucet/"
    )
)

data class FaucetUiState(
    val settings: FaucetSettingsEntity = FaucetSettingsEntity(),
    val faucets: List<FaucetItem> = defaultBeeFaucets,
    val selectedFaucet: FaucetItem = defaultBeeFaucets[0],
    val claims: List<FaucetClaimEntity> = emptyList(),
    val transactions: List<WalletTransactionEntity> = emptyList(),
    val timeRemainingMs: Long = 0L,
    val isFaucetReady: Boolean = true,
    val currentTab: String = "FAUCET",
    val isBalanceLoading: Boolean = false,
    val balanceErrorMessage: String? = null,
    val selectedRpcNetwork: RpcNetwork = availableRpcNetworks[0],
    val isPriceLoading: Boolean = false,
    val priceErrorMessage: String? = null,
    val coinGeckoPrices: Map<String, Double> = emptyMap(),
    val lastPriceUpdateEpochMs: Long = 0L
)

class FaucetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FaucetRepository
    private val web3Service = Web3BalanceService()
    private val coinGeckoService = CoinGeckoPriceService()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FaucetRepository(database.faucetDao())
        viewModelScope.launch {
            val s = repository.getOrInitSettings()
            if (s.walletAddress.isNotBlank()) {
                fetchOnChainBalance(s.walletAddress)
            }
        }
    }

    private val _uiState = MutableStateFlow(FaucetUiState())
    val uiState: StateFlow<FaucetUiState> = _uiState.asStateFlow()

    init {
        NotificationHelper.createNotificationChannel(application)
        fetchLiveCoinGeckoPrices()

        // Combine DB flows into UI State
        viewModelScope.launch {
            combine(
                repository.settingsFlow,
                repository.allClaims,
                repository.allTransactions
            ) { settings, claims, transactions ->
                val currentSettings = settings ?: FaucetSettingsEntity()

                _uiState.update { current ->
                    current.copy(
                        settings = currentSettings,
                        claims = claims,
                        transactions = transactions
                    )
                }
            }.collect {}
        }

        // 1-second countdown ticker for 60s cooldowns
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)

                val currentFaucets = _uiState.value.faucets
                var anyFinished = false
                val updatedFaucets = currentFaucets.map { faucet ->
                    if (faucet.cooldownSecondsRemaining > 0) {
                        val newCooldown = faucet.cooldownSecondsRemaining - 1
                        if (newCooldown == 0) {
                            anyFinished = true
                            if (_uiState.value.settings.pushNotificationEnabled) {
                                NotificationHelper.sendFaucetReadyNotification(
                                    getApplication(),
                                    faucet.name
                                )
                            }
                            if (_uiState.value.settings.vibrationEnabled) {
                                NotificationHelper.triggerVibration(getApplication())
                            }
                        }
                        faucet.copy(cooldownSecondsRemaining = newCooldown)
                    } else {
                        faucet
                    }
                }

                // Update selected faucet reference
                val selectedId = _uiState.value.selectedFaucet.id
                val updatedSelected = updatedFaucets.find { it.id == selectedId } ?: updatedFaucets.first()

                _uiState.update { current ->
                    current.copy(
                        faucets = updatedFaucets,
                        selectedFaucet = updatedSelected,
                        timeRemainingMs = (updatedSelected.cooldownSecondsRemaining * 1000).toLong(),
                        isFaucetReady = updatedSelected.isReady
                    )
                }
            }
        }
    }

    fun setTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectFaucet(faucet: FaucetItem) {
        _uiState.update {
            it.copy(
                selectedFaucet = faucet,
                currentTab = "CLAIM"
            )
        }
    }

    fun selectFaucetById(faucetId: String) {
        val found = _uiState.value.faucets.find { it.id == faucetId } ?: return
        selectFaucet(found)
    }

    /**
     * Called when the user clicks "I Have Claimed / Start 60s Timer" on the WebView claim screen.
     * Increments the daily claim counter for that faucet (up to 10/day).
     * Starts the 60-second cooldown timer.
     * Navigates back to the Home Faucet Grid.
     * No fake random reward or fake balance addition.
     */
    fun onUserClaimed(faucetId: String, context: Context) {
        val targetFaucet = _uiState.value.faucets.find { it.id == faucetId }
            ?: _uiState.value.selectedFaucet

        val updatedFaucets = _uiState.value.faucets.map { faucet ->
            if (faucet.id == targetFaucet.id) {
                val newCount = (faucet.dailyClaims + 1).coerceAtMost(faucet.maxDailyClaims)
                faucet.copy(
                    dailyClaims = newCount,
                    cooldownSecondsRemaining = 60, // 60-second cooldown timer!
                    lastClaimEpochMs = System.currentTimeMillis()
                )
            } else {
                faucet
            }
        }

        val updatedSelected = updatedFaucets.find { it.id == targetFaucet.id } ?: updatedFaucets.first()

        _uiState.update { current ->
            current.copy(
                faucets = updatedFaucets,
                selectedFaucet = updatedSelected,
                currentTab = "FAUCET" // Navigate back to the Home Faucet Grid!
            )
        }

        if (_uiState.value.settings.vibrationEnabled) {
            NotificationHelper.triggerVibration(context)
        }

        // Record real claim activity log in database for history without fake reward additions
        viewModelScope.launch {
            repository.insertClaimOnly(targetFaucet.name, targetFaucet.url)
        }
    }

    fun resetFaucetCooldown(faucetId: String) {
        val updated = _uiState.value.faucets.map { faucet ->
            if (faucet.id == faucetId) {
                faucet.copy(cooldownSecondsRemaining = 0)
            } else {
                faucet
            }
        }
        val updatedSelected = updated.find { it.id == _uiState.value.selectedFaucet.id } ?: updated.first()
        _uiState.update {
            it.copy(
                faucets = updated,
                selectedFaucet = updatedSelected,
                timeRemainingMs = 0L,
                isFaucetReady = updatedSelected.isReady
            )
        }
    }

    fun resetAllCooldowns() {
        val updated = _uiState.value.faucets.map { it.copy(cooldownSecondsRemaining = 0) }
        _uiState.update {
            it.copy(
                faucets = updated,
                selectedFaucet = it.selectedFaucet.copy(cooldownSecondsRemaining = 0),
                timeRemainingMs = 0L,
                isFaucetReady = true
            )
        }
    }

    fun resetDailyLimits() {
        val updated = _uiState.value.faucets.map { it.copy(dailyClaims = 0, cooldownSecondsRemaining = 0) }
        _uiState.update {
            it.copy(
                faucets = updated,
                selectedFaucet = it.selectedFaucet.copy(dailyClaims = 0, cooldownSecondsRemaining = 0)
            )
        }
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

    fun selectRpcNetwork(network: RpcNetwork) {
        _uiState.update { it.copy(selectedRpcNetwork = network) }
        fetchOnChainBalance()
    }

    fun updateWalletAddress(newAddress: String) {
        viewModelScope.launch {
            val clean = newAddress.trim()
            repository.updateWalletAddress(clean)
            fetchOnChainBalance(clean)
        }
    }

    fun fetchOnChainBalance(addressOverride: String? = null) {
        val address = addressOverride ?: _uiState.value.settings.walletAddress
        if (address.isBlank()) {
            _uiState.update {
                it.copy(
                    isBalanceLoading = false,
                    balanceErrorMessage = null
                )
            }
            return
        }

        val network = _uiState.value.selectedRpcNetwork
        _uiState.update { it.copy(isBalanceLoading = true, balanceErrorMessage = null) }

        viewModelScope.launch {
            val result = web3Service.getOnChainBalance(address, network.rpcUrl)
            result.fold(
                onSuccess = { onChainBalance ->
                    repository.updateWalletBalance(onChainBalance)
                    _uiState.update {
                        it.copy(
                            isBalanceLoading = false,
                            balanceErrorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isBalanceLoading = false,
                            balanceErrorMessage = error.message ?: "Failed to query RPC balance"
                        )
                    }
                }
            )
        }
    }

    fun fetchLiveCoinGeckoPrices() {
        _uiState.update { it.copy(isPriceLoading = true, priceErrorMessage = null) }
        viewModelScope.launch {
            val result = coinGeckoService.fetchLivePrices()
            result.fold(
                onSuccess = { prices ->
                    _uiState.update {
                        it.copy(
                            isPriceLoading = false,
                            coinGeckoPrices = prices,
                            lastPriceUpdateEpochMs = System.currentTimeMillis(),
                            priceErrorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isPriceLoading = false,
                            priceErrorMessage = error.message ?: "Failed to fetch CoinGecko rates"
                        )
                    }
                }
            )
        }
    }

    fun sendTestPushNotification(context: Context) {
        NotificationHelper.sendFaucetReadyNotification(context, _uiState.value.selectedFaucet.name)
    }
}
