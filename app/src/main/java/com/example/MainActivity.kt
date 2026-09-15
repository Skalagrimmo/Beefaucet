package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CaptchaClaimScreen
import com.example.ui.screens.FaucetHomeScreen
import com.example.ui.screens.ReminderSettingsScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FaucetItem
import com.example.ui.viewmodel.FaucetUiState
import com.example.ui.viewmodel.FaucetViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FaucetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                MainAppContent(
                    state = state,
                    onSelectTab = { viewModel.setTab(it) },
                    onSelectFaucet = { viewModel.selectFaucet(it) },
                    onResetCooldown = { viewModel.resetFaucetCooldown(it) },
                    onResetAllCooldowns = { viewModel.resetAllCooldowns() },
                    onUserClaimed = { id, ctx -> viewModel.onUserClaimed(id, ctx) },
                    onNavigateToWallet = { viewModel.setTab("WALLET") },
                    onToggleAutoWithdrawal = { viewModel.toggleAutoWithdrawal(it) },
                    onUpdateThreshold = { viewModel.updateAutoWithdrawalThreshold(it) },
                    onUpdateDestination = { viewModel.updateAutoWithdrawalDestination(it) },
                    onManualWithdraw = { amount, dest, cb ->
                        viewModel.executeManualWithdrawal(amount, dest, cb)
                    },
                    onIntervalSelected = { viewModel.updateReminderInterval(it) },
                    onTogglePushNotifications = { viewModel.togglePushNotifications(it) },
                    onSendTestNotification = { viewModel.sendTestPushNotification(it) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val targetTab = intent?.getStringExtra("OPEN_TAB")
        if (targetTab == "CLAIM") {
            viewModel.setTab("CLAIM")
        } else if (targetTab == "WALLET") {
            viewModel.setTab("WALLET")
        } else if (targetTab == "FAUCET") {
            viewModel.setTab("FAUCET")
        }
    }
}

data class NavItem(
    val id: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    state: FaucetUiState,
    onSelectTab: (String) -> Unit,
    onSelectFaucet: (FaucetItem) -> Unit,
    onResetCooldown: (String) -> Unit,
    onResetAllCooldowns: () -> Unit,
    onUserClaimed: (String, Context) -> Unit,
    onNavigateToWallet: () -> Unit,
    onToggleAutoWithdrawal: (Boolean) -> Unit,
    onUpdateThreshold: (Double) -> Unit,
    onUpdateDestination: (String) -> Unit,
    onManualWithdraw: (Double, String, (Boolean, String) -> Unit) -> Unit,
    onIntervalSelected: (Int) -> Unit,
    onTogglePushNotifications: (Boolean) -> Unit,
    onSendTestNotification: (Context) -> Unit
) {
    val navItems = listOf(
        NavItem("FAUCET", "Faucets", Icons.Filled.Language, Icons.Outlined.Language, "nav_faucet"),
        NavItem("CLAIM", "Claim", Icons.Filled.Security, Icons.Outlined.Security, "nav_claim"),
        NavItem("WALLET", "Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_wallet"),
        NavItem("REMINDERS", "Reminders", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_reminders")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (state.currentTab != "CLAIM") {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "🐝", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Bee Faucet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "beefaucet.org Hub • 60s Reminders",
                                    fontSize = 11.sp,
                                    color = HoneyGoldLight
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick balance pill on top right
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF20232C))
                                .clickable { onNavigateToWallet() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "%.4f".format(state.settings.walletBalance),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HoneyGoldPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "BEE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberAmberSecondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF101114)
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF13151A),
                contentColor = HoneyGoldPrimary,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = state.currentTab == item.id
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onSelectTab(item.id) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = HoneyGoldPrimary,
                            indicatorColor = HoneyGoldPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(targetState = state.currentTab, label = "TabSwitch") { currentTab ->
                when (currentTab) {
                    "FAUCET" -> FaucetHomeScreen(
                        state = state,
                        onSelectFaucet = onSelectFaucet,
                        onResetCooldown = onResetCooldown,
                        onResetAllCooldowns = onResetAllCooldowns,
                        onNavigateToWallet = onNavigateToWallet
                    )
                    "CLAIM" -> CaptchaClaimScreen(
                        state = state,
                        onClaimed = onUserClaimed,
                        onNavigateToHome = { onSelectTab("FAUCET") }
                    )
                    "WALLET" -> WalletScreen(
                        state = state,
                        onToggleAutoWithdrawal = onToggleAutoWithdrawal,
                        onUpdateThreshold = onUpdateThreshold,
                        onUpdateDestination = onUpdateDestination,
                        onManualWithdraw = onManualWithdraw
                    )
                    "REMINDERS" -> ReminderSettingsScreen(
                        state = state,
                        onIntervalSelected = onIntervalSelected,
                        onTogglePushNotifications = onTogglePushNotifications,
                        onSendTestNotification = onSendTestNotification,
                        onResetTimer = onResetAllCooldowns
                    )
                }
            }
        }
    }
}
